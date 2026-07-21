package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.request.CreateFilmRequest;
import ru.yandex.practicum.filmorate.dto.request.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.response.FilmResponse;
import ru.yandex.practicum.filmorate.dto.response.GenreResponse;
import ru.yandex.practicum.filmorate.dto.response.MpaResponse;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class FilmMapper {

    public Film toModel(CreateFilmRequest request) {
        if (request == null) return null;

        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.getMpa() != null && request.getMpa().getId() != null) {
            film.setMpa(request.getMpa());
        }

        if (request.getGenres() != null) {
            film.setGenres(new HashSet<>(request.getGenres()));
        }

        return film;
    }

    public Film toModel(UpdateFilmRequest request) {
        if (request == null) return null;

        Film film = new Film();
        film.setId(request.getId());
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.getMpa() != null && request.getMpa().getId() != null) {
            film.setMpa(request.getMpa());
        }

        if (request.getGenres() != null) {
            film.setGenres(new HashSet<>(request.getGenres()));
        }

        return film;
    }

    public FilmResponse toResponse(Film film) {
        FilmResponse response = new FilmResponse();
        response.setId(film.getId());
        response.setName(film.getName());
        response.setDescription(film.getDescription());
        response.setReleaseDate(film.getReleaseDate());
        response.setDuration(film.getDuration());

        if (film.getGenres() != null) {
            Set<GenreResponse> sortedGenres = new TreeSet<>(
                    Comparator.comparingInt(GenreResponse::getId)
            );
            for (Genre genre : film.getGenres()) {
                GenreResponse genreResponse = new GenreResponse();
                genreResponse.setId(genre.getId());
                genreResponse.setName(genre.getName());
                sortedGenres.add(genreResponse);
            }
            response.setGenres(sortedGenres);
        }

        response.setLikesCount(film.getLikes() != null ? film.getLikes().size() : 0);

        if (film.getMpa() != null) {
            MpaResponse mpaResponse = new MpaResponse();
            mpaResponse.setId(film.getMpa().getId());
            mpaResponse.setName(film.getMpa().getName());
            response.setMpa(mpaResponse);
        }

        return response;
    }
}