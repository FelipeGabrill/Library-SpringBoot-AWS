package com.library.controllers.impl;

import com.library.controllers.IAuthController;
import com.library.dtos.auth.EmailDTO;
import com.library.dtos.auth.NewPasswordDTO;
import com.library.services.IAuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthControllerImpl implements IAuthController {

    @Autowired
    private IAuthService authService;

    @Override
    public ResponseEntity<Void> createRecoverToken(EmailDTO dto) {
        authService.createRecoverToken(dto);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> saveNewPassword(NewPasswordDTO dto) {
        authService.saveNewPassword(dto);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Boolean> validateToken(String token) {
        return ResponseEntity.ok(authService.isValidToken(token));
    }
}
