package com.SleepUp.SU.utils;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class EntityUtil {

    public <ENTITY, DTO> List<DTO> mapEntitiesToDTOs(Collection<ENTITY> entities, Function<ENTITY, DTO> mapper) {
        return entities.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    public <T> void updateField(T newValue, Supplier<T> currentValueSupplier, Consumer<T> setter) {
        if (newValue != null && !newValue.equals(currentValueSupplier.get())) {
            setter.accept(newValue);
        }
    }
}