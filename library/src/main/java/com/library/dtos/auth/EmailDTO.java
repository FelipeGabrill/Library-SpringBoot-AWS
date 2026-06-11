package com.library.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Payload to request a password recovery token")
public class EmailDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Registered email address that will receive the recovery link",
            example = "felipe@biblioteca.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}