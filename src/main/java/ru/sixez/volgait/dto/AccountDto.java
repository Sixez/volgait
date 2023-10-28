package ru.sixez.volgait.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import ru.sixez.volgait.entity.Account;

public record AccountDto(
        long id,
        @NotBlank(message = "Username can not be blank!")
        String username,
        @NotBlank(message = "Password can not be blank!")
        String password,
        @NotNull
        boolean admin,
        @PositiveOrZero
        double balance
) {}
