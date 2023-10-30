package ru.sixez.volgait.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.sixez.volgait.entity.RentTypeEnum;

import java.util.Date;

@Schema(name = "Rent", description = "Rent object with all data specified")
public record RentDto(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        @NotNull
        long id,

        @NotNull
        long transportId,
        @NotNull
        long userId,

        @Schema(description = "Is unchangeable, auto value.")
        @NotNull
        Date timeStart,
        Date timeEnd,

        @NotNull
        double priceOfUnit,
        @NotNull
        RentTypeEnum priceType,
        double finalPrice
) {}
