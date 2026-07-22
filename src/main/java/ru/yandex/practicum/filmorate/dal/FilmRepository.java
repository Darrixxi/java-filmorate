package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FilmRepository implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;

    private static final String FIND_ALL_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                    "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.mpa_id";

    private static final String FIND_BY_ID_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                    "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.mpa_id WHERE f.film_id = ?";

    private static final String EXISTS_BY_ID_QUERY = "SELECT COUNT(*) FROM films WHERE film_id = ?";

    private static final String INSERT_QUERY =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";

    private static final String FIND_POPULAR_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                    "ORDER BY likes_count DESC, f.film_id ASC " +
                    "LIMIT ?";

    private static final String ADD_LIKE_QUERY =
            "MERGE INTO likes (film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)";

    private static final String REMOVE_LIKE_QUERY =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String GET_GENRES_BY_FILM_IDS_QUERY =
            "SELECT fg.film_id, g.genre_id, g.name FROM genres g " +
                    "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                    "WHERE fg.film_id IN (?)";

    private static final String GET_LIKES_BY_FILM_IDS_QUERY =
            "SELECT film_id, user_id FROM likes WHERE film_id IN (?)";

    private static final String INSERT_GENRE_QUERY =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

    private static final String DELETE_GENRES_QUERY =
            "DELETE FROM film_genres WHERE film_id = ?";

    @Override
    @Transactional
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        Integer filmId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(filmId, film.getGenres());
        }

        System.out.println("Фильм создан с id: " + filmId);
        if (film.getGenres() != null) {
            System.out.println("Жанры после сохранения: " + film.getGenres().size());
        }

        return findById(filmId).orElseThrow(() ->
                new NotFoundException("Не удалось найти только что созданный фильм с id=" + filmId));
    }

    @Override
    @Transactional
    public Film update(Film film) {
        int rows = jdbc.update(UPDATE_QUERY,
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null, film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        jdbc.update(DELETE_GENRES_QUERY, film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }

        return findById(film.getId()).orElseThrow(() ->
                new NotFoundException("Не удалось найти только что обновлённый фильм с id=" + film.getId()));
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(FIND_ALL_QUERY, filmRowMapper);
        populateGenresForFilms(films);
        populateLikesForFilms(films);
        return films;
    }

    @Override
    public Optional<Film> findById(Integer id) {
        Optional<Film> filmOpt = jdbc.query(FIND_BY_ID_QUERY, filmRowMapper, id).stream().findFirst();

        filmOpt.ifPresent(film -> {
            populateGenresForFilms(List.of(film));
            populateLikesForFilms(List.of(film));
        });

        return filmOpt;
    }

    @Override
    public boolean existsById(Integer id) {
        Integer count = jdbc.queryForObject(EXISTS_BY_ID_QUERY, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Collection<Film> findPopular(int count) {
        List<Film> films = jdbc.query(FIND_POPULAR_QUERY, filmRowMapper, count);
        populateGenresForFilms(films);
        populateLikesForFilms(films);
        return films;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    private void populateGenresForFilms(Collection<Film> films) {
        if (films == null || films.isEmpty()) return;

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String query = GET_GENRES_BY_FILM_IDS_QUERY.replace("(?)", "(" + inSql + ")");

        // один запрос к БД → заполняем Map в памяти
        Map<Integer, Set<Genre>> genresByFilmId = new HashMap<>();
        jdbc.query(query, rs -> {
            Integer filmId = rs.getInt("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            genresByFilmId.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
        }, filmIds.toArray());

        // работаем только с памятью (БД не трогаем)
        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), new HashSet<>()));
        }
    }

    private void populateLikesForFilms(Collection<Film> films) {
        if (films == null || films.isEmpty()) return;

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String query = GET_LIKES_BY_FILM_IDS_QUERY.replace("(?)", "(" + inSql + ")");

        // один запрос к БД
        Map<Integer, Set<Integer>> likesByFilmId = new HashMap<>();
        jdbc.query(query, rs -> {
            Integer filmId = rs.getInt("film_id");
            Integer userId = rs.getInt("user_id");
            likesByFilmId.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }, filmIds.toArray());

        // работаем только с памятью
        for (Film film : films) {
            film.setLikes(likesByFilmId.getOrDefault(film.getId(), new HashSet<>()));
        }
    }

    private void saveGenres(Integer filmId, Collection<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        try {
            for (Genre genre : genres) {
                if (genre != null && genre.getId() != null) {
                    jdbc.update(INSERT_GENRE_QUERY, filmId, genre.getId());
                }
            }
        } catch (DataAccessException e) {
            System.err.println("Ошибка при сохранении жанров: " + e.getMessage());
            throw new RuntimeException("Не удалось сохранить жанры для фильма", e);
        }
    }
}