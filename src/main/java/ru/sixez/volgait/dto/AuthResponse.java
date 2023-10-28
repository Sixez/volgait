package ru.sixez.volgait.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthResponse(
        @NotBlank(message = "Username can not be blank!")
        String username,
        @NotBlank(message = "token can not be blank!")
        String token
) {}
