package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateFilm(film);
        return filmStorage.update(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Integer id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public void addLike(Integer filmId, Integer userId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        filmStorage.findById(filmId).get().getLikes().add(userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        filmStorage.findById(filmId).get().getLikes().remove(userId);
    }

    public Collection<Film> getPopular(Integer count) {
        int actualCount;
        if (count == null || count <= 0) {
            actualCount = 10;
        } else {
            actualCount = count;
        }
        return filmStorage.findPopular(actualCount);
    }

    private void validateFilm(Film film) {
        if (film.getId() != null && film.getId() <= 0) {
            throw new ValidationException("Id должен быть положительным числом");
        }
        if (!StringUtils.hasText(film.getName())) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
