package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String EXISTS_BY_ID_QUERY = "SELECT COUNT(*) FROM users WHERE user_id = ?";

    private static final String INSERT_QUERY =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

    private static final String ADD_FRIEND_QUERY =
            "MERGE INTO friendships (user_id, friend_id, status) KEY(user_id, friend_id) VALUES (?, ?, 'CONFIRMED')";

    private static final String REMOVE_FRIEND_QUERY =
            "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";

    private static final String GET_FRIENDS_QUERY =
            "SELECT u.* FROM users u " +
                    "JOIN friendships f ON u.user_id = f.friend_id " +
                    "WHERE f.user_id = ?";

    private static final String GET_COMMON_FRIENDS_QUERY =
            "SELECT u.* FROM users u " +
                    "WHERE u.user_id IN (" +
                    "  SELECT friend_id FROM friendships WHERE user_id = ?" +
                    ") " +
                    "AND u.user_id IN (" +
                    "  SELECT friend_id FROM friendships WHERE user_id = ?" +
                    ")";


    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                ps.setObject(4, user.getBirthday());
                return ps;
            }, keyHolder);

            user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
            return user;
        } catch (DuplicateKeyException e) {
            throw new DuplicatedDataException("Пользователь с таким email или login уже существует");
        }
    }

    @Override
    public User update(User user) {
        int rows = jdbc.update(UPDATE_QUERY,
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (rows == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return jdbc.query(FIND_ALL_QUERY, userRowMapper);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return jdbc.query(FIND_BY_ID_QUERY, userRowMapper, id).stream().findFirst();
    }

    @Override
    public boolean existsById(Integer id) {
        Integer count = jdbc.queryForObject(EXISTS_BY_ID_QUERY, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        jdbc.update(ADD_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        jdbc.update(REMOVE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Integer userId) {
        return jdbc.query(GET_FRIENDS_QUERY, userRowMapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        return jdbc.query(GET_COMMON_FRIENDS_QUERY, userRowMapper, userId, otherId);
    }
}