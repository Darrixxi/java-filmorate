package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserRepository.class, UserRowMapper.class})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateAndFindUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userRepository.create(user);

        assertThat(created.getId()).isNotNull();

        Optional<User> found = userRepository.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@mail.ru");
    }

    @Test
    void shouldReturnEmpty_whenUserNotFound() {
        Optional<User> found = userRepository.findById(999);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckUserExists() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userRepository.create(user);

        assertThat(userRepository.existsById(created.getId())).isTrue();

        assertThat(userRepository.existsById(999)).isFalse();
    }
}