package com.SleepUp.SU.utils;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EntityMapperUtil {

    public <ENTITY, DTO> List<DTO> mapEntitiesToDTOs(Collection<ENTITY> entities, Function<ENTITY, DTO> mapper) {
        return entities.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
}