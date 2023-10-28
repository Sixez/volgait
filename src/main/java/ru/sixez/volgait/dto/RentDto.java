package ru.sixez.volgait.dto;

import jakarta.validation.constraints.NotNull;
import ru.sixez.volgait.entity.RentTypeEnum;

import java.util.Date;

public record RentDto(
        @NotNull
        long id,
        @NotNull
        long transportId,
        @NotNull
        long userId,

        @NotNull
        Date timeStart,
        Date timeEnd,

        @NotNull
        double priceOfUnit,
        @NotNull
        RentTypeEnum priceType,
        double finalPrice
) {}
