package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.response.MpaResponse;
import ru.yandex.practicum.filmorate.model.Mpa;

@Component
public class MpaMapper {

    public MpaResponse toResponse(Mpa mpa) {
        if (mpa == null) return null;

        MpaResponse response = new MpaResponse();
        response.setId(mpa.getId());
        response.setName(mpa.getName());
        return response;
    }

    public Mpa toModelById(Integer id) {
        if (id == null) return null;

        Mpa mpa = new Mpa();
        mpa.setId(id);
        return mpa;
    }
}