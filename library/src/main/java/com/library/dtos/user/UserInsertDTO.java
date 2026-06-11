package com.library.dtos.user;

import com.library.services.validation.UserInsertValid;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@UserInsertValid
@Schema(description = "Payload to create a new user (multipart/form-data)")
public class UserInsertDTO extends UserDTO {

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "User password (minimum 6 characters, stored as BCrypt hash)",
            example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Profile picture file. Accepted types: JPG, PNG, WebP. Max 5MB",
            type = "string", format = "binary")
    private MultipartFile profilePicture;
}
