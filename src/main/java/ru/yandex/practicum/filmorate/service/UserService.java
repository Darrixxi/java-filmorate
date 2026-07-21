package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.request.CreateUserRequest;
import ru.yandex.practicum.filmorate.dto.request.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.response.UserResponse;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final UserMapper userMapper;

    public UserResponse create(CreateUserRequest request) {
        User user = userMapper.toModel(request);
        validateUser(user);
        applyBusinessRulesOnCreate(user);
        User saved = userStorage.create(user);
        return userMapper.toResponse(saved);
    }

    public UserResponse update(UpdateUserRequest request) {
        User user = userMapper.toModel(request);
        validateUser(user);
        applyBusinessRulesOnUpdate(user);
        User saved = userStorage.update(user);
        return userMapper.toResponse(saved);
    }

    public Collection<UserResponse> findAll() {
        return userStorage.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse findById(Integer id) {
        User user = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
        return userMapper.toResponse(user);
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (!userStorage.existsById(userId) || !userStorage.existsById(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя добавить себя в друзья");
        }
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        if (!userStorage.existsById(userId) || !userStorage.existsById(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<UserResponse> getFriends(Integer userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return userStorage.getFriends(userId).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Collection<UserResponse> getCommonFriends(Integer userId, Integer otherId) {
        if (!userStorage.existsById(userId) || !userStorage.existsById(otherId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        return userStorage.getCommonFriends(userId, otherId).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateUser(User user) {
        if (user.getId() != null && user.getId() <= 0) {
            throw new ValidationException("Id должен быть положительным числом");
        }

        String email = user.getEmail() != null ? user.getEmail().trim() : "";
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        user.setEmail(email);

        String login = user.getLogin() != null ? user.getLogin() : "";
        if (!StringUtils.hasText(login) || login.contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        user.setLogin(login.trim());

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void applyBusinessRulesOnCreate(User user) {
        if (!StringUtils.hasText(user.getName())) {
            user.setName(user.getLogin());
        }
    }

    private void applyBusinessRulesOnUpdate(User user) {
        if (!StringUtils.hasText(user.getName())) {
            User existingUser = userStorage.findById(user.getId())
                    .orElseThrow(() -> new NotFoundException("Пользователь с id=" + user.getId() + " не найден"));

            if (StringUtils.hasText(existingUser.getName())) {
                user.setName(existingUser.getName());
            } else {
                user.setName(existingUser.getLogin());
            }
        }
    }
}