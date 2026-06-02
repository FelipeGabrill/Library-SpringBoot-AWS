package com.library.dtos.user;

import com.library.services.validation.UserUpdateValid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@UserUpdateValid
public class UserUpdateDTO extends UserDTO {

    private MultipartFile profilePicture;

}
