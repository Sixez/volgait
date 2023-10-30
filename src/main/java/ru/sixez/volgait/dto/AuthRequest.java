package ru.sixez.volgait.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "Authentication request", description = "Request body for authentication and standard registration")
public record AuthRequest(
        @Schema(defaultValue = "user")
        @NotBlank
        String username,
        @Schema(defaultValue = "user")
        @NotBlank
        String password
) {}
