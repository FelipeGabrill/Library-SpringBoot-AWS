package com.library.dtos.user;

import com.library.services.validation.UserUpdateValid;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@UserUpdateValid
@Schema(description = "Payload to update an existing user (multipart/form-data)")
public class UserUpdateDTO extends UserDTO {

    @Schema(description = "New profile picture. If provided, the old one is deleted from S3. "
            + "Accepted types: JPG, PNG, WebP. Max 5MB",
            type = "string", format = "binary")
    private MultipartFile profilePicture;
}
