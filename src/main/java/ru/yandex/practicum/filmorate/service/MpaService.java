package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.response.MpaResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaStorage mpaStorage;
    private final MpaMapper mpaMapper;

    public Collection<MpaResponse> findAll() {
        return mpaStorage.findAll().stream()
                .map(mpaMapper::toResponse)
                .collect(Collectors.toList());
    }

    public MpaResponse findById(Integer id) {
        Mpa mpa = mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id=" + id + " не найден"));
        return mpaMapper.toResponse(mpa);
    }
}