package ru.sixez.volgait.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.sixez.volgait.entity.TransportTypeEnum;

public record TransportDto (
        @NotNull
        long id,
        @NotNull
        long owner_id,
        String description,
        @NotNull
        TransportTypeEnum transportType,
        @NotBlank
        String model,
        @NotBlank
        String identifier,
        @NotBlank
        String color,
        boolean canBeRented,
        @NotNull
        double longitude,
        @NotNull
        double latitude,
        double minutePrice,
        double dayPrice
) {}
