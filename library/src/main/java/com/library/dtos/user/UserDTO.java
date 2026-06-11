package com.library.dtos.user;

import com.library.models.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "User data returned by the API")
public class UserDTO {

    @Schema(description = "Unique identifier of the user",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Full name of the user", example = "Felipe Gabriel",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address (unique, used as login)",
            example = "felipe@biblioteca.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "URL of the user's profile picture stored in S3",
            example = "https://biblioteca-imagens-356666487529-prod.s3.amazonaws.com/users/1_profile.jpg",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String avatarUrl;

    @Schema(description = "Roles assigned to the user")
    private List<RoleDTO> roles = new ArrayList<>();

    public UserDTO() {
    }

    public UserDTO(User user) {
        id = user.getId();
        name = user.getName();
        email = user.getUsername();
        avatarUrl = user.getAvatarUrl();
        user.getRoles().forEach(role -> roles.add(new RoleDTO(role)));
    }
}