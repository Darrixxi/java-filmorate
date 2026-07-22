package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreRepository implements GenreStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreRowMapper;

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY genre_id";
    private static final String FIND_BY_IDS_QUERY = "SELECT * FROM genres WHERE genre_id IN (?)";

    @Override
    public Collection<Genre> findAll() {
        return jdbc.query(FIND_ALL_QUERY, genreRowMapper);
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_BY_ID_QUERY, genreRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Genre> findByIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String query = FIND_BY_IDS_QUERY.replace("(?)", "(" + inSql + ")");

        return jdbc.query(query, genreRowMapper, ids.toArray());
    }
}