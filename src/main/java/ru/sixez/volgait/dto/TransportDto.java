package ru.sixez.volgait.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.sixez.volgait.entity.TransportTypeEnum;

@Schema(name = "Transport")
public record TransportDto (
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        @NotNull
        long id,
        @NotNull
        long owner_id,
        String description,
        @Schema(description = "Is unchangeable")
        @NotNull
        TransportTypeEnum transportType,
        @NotBlank
        String model,
        @Schema(description = "Should be unique")
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
