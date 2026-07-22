package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.response.GenreResponse;
import ru.yandex.practicum.filmorate.model.Genre;

@Component
public class GenreMapper {

    public GenreResponse toResponse(Genre genre) {
        if (genre == null) return null;

        GenreResponse response = new GenreResponse();
        response.setId(genre.getId());
        response.setName(genre.getName());
        return response;
    }

    public Genre toModelById(Integer id) {
        if (id == null) return null;

        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }
}