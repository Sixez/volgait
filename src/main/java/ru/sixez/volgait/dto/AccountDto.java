package ru.sixez.volgait.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(name = "Account", description = "Use as input and output of \"Account\" and \"Admin account\" endpoint sets")
public record AccountDto(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        long id,

        @Schema(defaultValue = "user")
        @NotBlank
        String username,
        @Schema(defaultValue = "qwerty123")
        @NotBlank
        String password,
        @Schema(description = "Is account admin privileged", defaultValue = "false")
        @NotNull
        boolean admin,
        @PositiveOrZero
        double balance
) {}
