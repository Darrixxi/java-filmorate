package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        validateUser(user);
        applyBusinessRules(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validateUser(user);
        applyBusinessRules(user);
        return userStorage.update(user);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Integer id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (!userStorage.existsById(userId) || !userStorage.existsById(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        User user = userStorage.findById(userId).get();
        User friend = userStorage.findById(friendId).get();

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        if (!userStorage.existsById(userId) || !userStorage.existsById(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        User user = userStorage.findById(userId).get();
        User friend = userStorage.findById(friendId).get();

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Collection<User> getFriends(Integer userId) {
        User user = findById(userId);
        return user.getFriends().stream()
                .map(this::findById)
                .toList();
    }

    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        if (!userStorage.existsById(userId) || !userStorage.existsById(otherId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        return userStorage.findCommonFriends(userId, otherId);
    }

    private void validateUser(User user) {
        if (user.getId() != null && user.getId() <= 0) {
            throw new ValidationException("Id должен быть положительным числом");
        }
        if (!StringUtils.hasText(user.getEmail()) || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (!StringUtils.hasText(user.getLogin()) || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void applyBusinessRules(User user) {
        if (!StringUtils.hasText(user.getName())) {
            user.setName(user.getLogin());
        }
    }
}
