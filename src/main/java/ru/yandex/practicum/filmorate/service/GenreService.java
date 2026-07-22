package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.response.GenreResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;
    private final GenreMapper genreMapper;

    public Collection<GenreResponse> findAll() {
        return genreStorage.findAll().stream()
                .map(genreMapper::toResponse)
                .collect(Collectors.toList());
    }

    public GenreResponse findById(Integer id) {
        Genre genre = genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
        return genreMapper.toResponse(genre);
    }
}