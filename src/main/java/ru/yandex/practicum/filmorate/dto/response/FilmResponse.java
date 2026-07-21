package ru.yandex.practicum.filmorate.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmResponse {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaResponse mpa;
    private Set<GenreResponse> genres;
    private Integer likesCount;
}