package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmRepository.class,
        UserRepository.class,
        FilmRowMapper.class,
        GenreRowMapper.class,
        UserRowMapper.class,
        MpaRowMapper.class
})
class FilmoRateApplicationTests {

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateAndFindFilm() {
        Mpa mpa = new Mpa();
        mpa.setId(1);

        Genre genre = new Genre();
        genre.setId(1);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        film.setGenres(Set.of(genre));

        Film created = filmRepository.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Film");

        Optional<Film> found = filmRepository.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Film");
        assertThat(found.get().getGenres()).hasSize(1);
    }

    @Test
    void testCreateAndFindUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userRepository.create(user);

        assertThat(created.getId()).isNotNull();

        Optional<User> found = userRepository.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo("testuser");
    }

    @Test
    void testAddAndRemoveLike() {
        User user = new User();
        user.setEmail("like@mail.ru");
        user.setLogin("likeuser");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userRepository.create(user);

        Film film = new Film();
        film.setName("Like Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        Film createdFilm = filmRepository.create(film);

        filmRepository.addLike(createdFilm.getId(), createdUser.getId());

        Collection<Film> popular = filmRepository.findPopular(10);
        assertThat(popular).isNotEmpty();

        filmRepository.removeLike(createdFilm.getId(), createdUser.getId());
    }

    @Test
    void testFriendship() {
        User user1 = userRepository.create(createTestUser("friend1@mail.ru", "friend1", 1990));
        User user2 = userRepository.create(createTestUser("friend2@mail.ru", "friend2", 1991));
        User user3 = userRepository.create(createTestUser("friend3@mail.ru", "friend3", 1992));

        // user1 подписывается на user3
        userRepository.addFriend(user1.getId(), user3.getId());

        // Проверяем: у user1 есть подписка на user3
        assertThat(userRepository.getFriends(user1.getId()))
                .hasSize(1)
                .extracting(User::getLogin)
                .containsExactly("friend3");

        // У user3 пока нет подписок
        assertThat(userRepository.getFriends(user3.getId())).isEmpty();

        // user3 подписывается на user1
        userRepository.addFriend(user3.getId(), user1.getId());

        // Теперь у обоих по 1 другу
        assertThat(userRepository.getFriends(user1.getId())).hasSize(1);
        assertThat(userRepository.getFriends(user3.getId())).hasSize(1);

        // Добавляем взаимные подписки user2 и user3
        userRepository.addFriend(user2.getId(), user3.getId());
        userRepository.addFriend(user3.getId(), user2.getId());

        // Проверяем общих друзей
        Collection<User> common = userRepository.getCommonFriends(user1.getId(), user2.getId());

        assertThat(common).hasSize(1);
        assertThat(common.iterator().next().getLogin()).isEqualTo("friend3");
    }

    private User createTestUser(String email, String login, int birthYear) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login); // Имя = логин (чтобы не было null)
        user.setBirthday(LocalDate.of(birthYear, 1, 1));
        return user;
    }
}