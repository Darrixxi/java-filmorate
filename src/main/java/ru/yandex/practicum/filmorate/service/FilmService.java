package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.request.CreateFilmRequest;
import ru.yandex.practicum.filmorate.dto.request.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.response.FilmResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmMapper filmMapper;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public FilmResponse create(CreateFilmRequest request) {
        Film film = filmMapper.toModel(request);
        validateFilm(film);
        validateMpaAndGenres(film);
        Film saved = filmStorage.create(film);
        return filmMapper.toResponse(saved);
    }

    public FilmResponse update(UpdateFilmRequest request) {
        Film film = filmMapper.toModel(request);
        validateFilm(film);
        validateMpaAndGenres(film);

        Film saved = filmStorage.update(film);
        return filmMapper.toResponse(saved);
    }

    public Collection<FilmResponse> findAll() {
        return filmStorage.findAll().stream()
                .map(filmMapper::toResponse)
                .collect(Collectors.toList());
    }

    public FilmResponse findById(Integer id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
        return filmMapper.toResponse(film);
    }

    public void addLike(Integer filmId, Integer userId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<FilmResponse> getPopular(Integer count) {
        int actualCount = (count == null || count <= 0) ? 10 : count;
        return filmStorage.findPopular(actualCount).stream()
                .map(filmMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getId() != null && film.getId() <= 0) {
            throw new ValidationException("Id должен быть положительным числом");
        }

        String name = film.getName() != null ? film.getName().trim() : "";
        if (!StringUtils.hasText(name)) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        film.setName(name);

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaStorage.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException(
                            "Рейтинг с id=" + film.getMpa().getId() + " не найден"));
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (genre.getId() != null) {
                    genreStorage.findById(genre.getId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Жанр с id=" + genre.getId() + " не найден"));
                }
            }
        }
    }
}