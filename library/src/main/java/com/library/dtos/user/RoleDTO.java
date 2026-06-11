package com.library.dtos.user;

import com.library.models.entities.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Schema(description = "User role")
public class RoleDTO {

    @NotNull(message = "Role id is required")
    @Schema(description = "Role identifier (1 = ROLE_ADMIN, 2 = ROLE_USER)",
            example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Role authority name", example = "ROLE_USER",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String authority;

    public RoleDTO(Role role) {
        id = role.getId();
        authority = role.getAuthority();
    }
}