package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {

    private UserController userController;
    private FilmController filmController;
    private UserService userService;
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        FilmStorage filmStorage = new InMemoryFilmStorage();

        userService = new UserService(userStorage);
        filmService = new FilmService(filmStorage, userStorage);

        userController = new UserController(userService);
        filmController = new FilmController(filmService);
    }

    private User validUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void whenDataIsValid() {
        User user = validUser();
        User created = userController.create(user);
        assertNotNull(created.getId());
        assertEquals("test@mail.ru", created.getEmail());
    }

    @Test
    void whenEmailIsNull() {
        User user = validUser();
        user.setEmail(null);
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void whenEmailIsEmpty() {
        User user = validUser();
        user.setEmail("");
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void whenEmailIsBlank() {
        User user = validUser();
        user.setEmail("   ");
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void whenEmailHasNoAtSymbol() {
        User user = validUser();
        user.setEmail("invalidemail.ru");
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void whenEmailHasAtSymbol() {
        User user = validUser();
        user.setEmail("a@b");
        assertDoesNotThrow(() -> userController.create(user));
    }

    @Test
    void whenLoginIsNull() {
        User user = validUser();
        user.setLogin(null);
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void whenLoginIsEmpty() {
        User user = validUser();
        user.setLogin("");
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void whenLoginIsBlank() {
        User user = validUser();
        user.setLogin("   ");
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void whenLoginContainsSpace() {
        User user = validUser();
        user.setLogin("test user");
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void whenBirthdayIsInFuture() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void whenBirthdayIsToday() {
        User user = validUser();
        user.setBirthday(LocalDate.now());
        assertDoesNotThrow(() -> userController.create(user));
    }

    @Test
    void whenBirthdayIsNull() {
        User user = validUser();
        user.setBirthday(null);
        assertDoesNotThrow(() -> userController.create(user));
    }

    @Test
    void shouldSetNameToLogin_whenNameIsNull() {
        User user = validUser();
        user.setName(null);
        User created = userController.create(user);
        assertEquals("testuser", created.getName());
    }

    @Test
    void shouldSetNameToLogin_whenNameIsBlank() {
        User user = validUser();
        user.setName("   ");
        User created = userController.create(user);
        assertEquals("testuser", created.getName());
    }

    @Test
    void whenUserIsCompletelyEmpty() {
        User user = new User();
        assertThrows(ValidationException.class, () -> userController.create(user));
    }


    private Film validFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    @Test
    void shouldCreateFilm_whenDataIsValid() {
        Film film = validFilm();
        Film created = filmController.create(film);
        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
    }

    @Test
    void shouldRejectFilm_whenNameIsNull() {
        Film film = validFilm();
        film.setName(null);
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldRejectFilm_whenNameIsEmpty() {
        Film film = validFilm();
        film.setName("");
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldRejectFilm_whenNameIsBlank() {
        Film film = validFilm();
        film.setName("   ");
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldRejectFilm_whenDescriptionExceeds200Chars() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldAcceptFilm_whenDescriptionExactly200Chars() {
        Film film = validFilm();
        film.setDescription("a".repeat(200));
        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void shouldAcceptFilm_whenDescriptionIsNull() {
        Film film = validFilm();
        film.setDescription(null);
        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void shouldRejectFilm_whenReleaseDateBeforeMinDate() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldAcceptFilm_whenReleaseDateIsMinDate() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void shouldRejectFilm_whenReleaseDateIsNull() {
        Film film = validFilm();
        film.setReleaseDate(null);
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldRejectFilm_whenDurationIsZero() {
        Film film = validFilm();
        film.setDuration(0);
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldRejectFilm_whenDurationIsNegative() {
        Film film = validFilm();
        film.setDuration(-10);
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldAcceptFilm_whenDurationIsOne() {
        Film film = validFilm();
        film.setDuration(1);
        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void shouldRejectFilm_whenFilmIsCompletelyEmpty() {
        Film film = new Film();
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldRejectFilmUpdate_whenIdIsZero() {
        Film film = validFilm();
        film.setId(0);
        assertThrows(ValidationException.class, () -> filmController.update(film));
    }

    @Test
    void shouldRejectFilmUpdate_whenIdIsNegative() {
        Film film = validFilm();
        film.setId(-5);
        assertThrows(ValidationException.class, () -> filmController.update(film));
    }
}
