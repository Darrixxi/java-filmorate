package ru.yandex.practicum.filmorate.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {

    @NotNull(message = "Id пользователя обязателен для обновления")
    private Integer id;

    @Email(message = "Некорректный формат электронной почты")
    private String email;

    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}