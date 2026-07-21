package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.response.GenreResponse;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public Collection<GenreResponse> findAll() {
        return genreService.findAll();
    }

    @GetMapping("/{id}")
    public GenreResponse findById(@PathVariable Integer id) {
        return genreService.findById(id);
    }
}