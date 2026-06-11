package com.library.services.impl;

import com.library.dtos.auth.EmailDTO;
import com.library.dtos.auth.NewPasswordDTO;
import com.library.models.entities.PasswordRecover;
import com.library.models.entities.User;
import com.library.models.repositories.PasswordRecoverRepository;
import com.library.models.repositories.UserRepository;
import com.library.publisher.PasswordRecoveryPublisher;
import com.library.services.exceptions.InvalidTokenException;
import com.library.services.exceptions.ResourceNotFoundException;
import com.library.services.exceptions.TokenExpiredException;
import com.library.factories.UserFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl service;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordRecoverRepository passwordRecoverRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordRecoveryPublisher passwordRecoveryPublisher;

    private String existingEmail;
    private String nonExistingEmail;
    private String validToken;
    private String invalidToken;
    private String expiredToken;
    private User user;

    @BeforeEach
    void setUp() {
        existingEmail = "felipe@biblioteca.com";
        nonExistingEmail = "notfound@biblioteca.com";
        validToken = "valid-token-uuid";
        invalidToken = "invalid-token-uuid";
        expiredToken = "expired-token-uuid";
        user = UserFactory.createUser();
        ReflectionTestUtils.setField(service, "tokenMinutes", 30L);
    }

    @Test
    void createRecoverTokenShouldSaveAndPublishWhenEmailExists() {
        EmailDTO dto = new EmailDTO();
        dto.setEmail(existingEmail);

        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(user));
        when(passwordRecoverRepository.save(any(PasswordRecover.class))).thenReturn(new PasswordRecover());

        service.createRecoverToken(dto);

        verify(passwordRecoverRepository).save(any(PasswordRecover.class));
        verify(passwordRecoveryPublisher).publishPasswordRecovery(eq(existingEmail), anyString());
    }

    @Test
    void createRecoverTokenShouldThrowResourceNotFoundExceptionWhenEmailNotFound() {
        EmailDTO dto = new EmailDTO();
        dto.setEmail(nonExistingEmail);

        when(userRepository.findByEmail(nonExistingEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createRecoverToken(dto));
        verify(passwordRecoverRepository, never()).save(any(PasswordRecover.class));
        verify(passwordRecoveryPublisher, never()).publishPasswordRecovery(anyString(), anyString());
    }

    @Test
    void saveNewPasswordShouldUpdatePasswordWhenTokenIsValid() {
        NewPasswordDTO dto = new NewPasswordDTO();
        dto.setToken(validToken);
        dto.setPassword("newPassword123");

        PasswordRecover recover = new PasswordRecover();
        recover.setEmail(existingEmail);
        recover.setToken(validToken);

        when(passwordRecoverRepository.searchValidTokens(eq(validToken), any(Instant.class)))
                .thenReturn(List.of(recover));
        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newEncoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        service.saveNewPassword(dto);

        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(user);
    }

    @Test
    void saveNewPasswordShouldThrowInvalidTokenExceptionWhenTokenIsInvalid() {
        NewPasswordDTO dto = new NewPasswordDTO();
        dto.setToken(invalidToken);
        dto.setPassword("newPassword123");

        when(passwordRecoverRepository.searchValidTokens(eq(invalidToken), any(Instant.class)))
                .thenReturn(List.of());

        assertThrows(InvalidTokenException.class, () -> service.saveNewPassword(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void saveNewPasswordShouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        NewPasswordDTO dto = new NewPasswordDTO();
        dto.setToken(validToken);
        dto.setPassword("newPassword123");

        PasswordRecover recover = new PasswordRecover();
        recover.setEmail(existingEmail);

        when(passwordRecoverRepository.searchValidTokens(eq(validToken), any(Instant.class)))
                .thenReturn(List.of(recover));
        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.saveNewPassword(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void isValidTokenShouldReturnTrueWhenTokenIsValidAndNotExpired() {
        PasswordRecover recover = new PasswordRecover();
        recover.setToken(validToken);
        recover.setExpiration(Instant.now().plusSeconds(600));

        when(passwordRecoverRepository.findByToken(validToken)).thenReturn(Optional.of(recover));

        assertTrue(service.isValidToken(validToken));
    }

    @Test
    void isValidTokenShouldThrowInvalidTokenExceptionWhenTokenNotFound() {
        when(passwordRecoverRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> service.isValidToken(invalidToken));
    }

    @Test
    void isValidTokenShouldThrowTokenExpiredExceptionWhenTokenExpired() {
        PasswordRecover recover = new PasswordRecover();
        recover.setToken(expiredToken);
        recover.setExpiration(Instant.now().minusSeconds(600));

        when(passwordRecoverRepository.findByToken(expiredToken)).thenReturn(Optional.of(recover));

        assertThrows(TokenExpiredException.class, () -> service.isValidToken(expiredToken));
    }
}
