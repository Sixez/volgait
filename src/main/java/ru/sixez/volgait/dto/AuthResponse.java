package ru.sixez.volgait.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "Authentication response", description = "Response body of authentication endpoint")
public record AuthResponse(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "user")
        @NotBlank
        String username,
        @Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "JWToken")
        @NotBlank
        String token
) {}
