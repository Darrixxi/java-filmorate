package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.request.CreateUserRequest;
import ru.yandex.practicum.filmorate.dto.request.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.response.UserResponse;
import ru.yandex.practicum.filmorate.model.User;

@Component
public class UserMapper {

    public User toModel(CreateUserRequest request) {
        if (request == null) return null;

        User user = new User();
        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setName(request.getName());
        user.setBirthday(request.getBirthday());
        return user;
    }

    public User toModel(UpdateUserRequest request) {
        if (request == null) return null;

        User user = new User();
        user.setId(request.getId());
        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setName(request.getName());
        user.setBirthday(request.getBirthday());
        return user;
    }

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setLogin(user.getLogin());
        response.setName(user.getName());
        response.setBirthday(user.getBirthday());
        return response;
    }
}