package com.library.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Payload to set a new password using a valid recovery token")
public class NewPasswordDTO {

    @NotBlank(message = "Token is required")
    @Schema(description = "Recovery token received by email",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "New password (minimum 6 characters)",
            example = "newSecret123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}