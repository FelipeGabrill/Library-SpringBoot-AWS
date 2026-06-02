package com.library.services.impl;

import com.library.dtos.auth.EmailDTO;
import com.library.dtos.auth.NewPasswordDTO;
import com.library.models.entities.PasswordRecover;
import com.library.models.entities.User;
import com.library.publisher.PasswordRecoveryPublisher;
import com.library.models.repositories.PasswordRecoverRepository;
import com.library.models.repositories.UserRepository;
import com.library.services.IAuthService;
import com.library.services.exceptions.InvalidTokenException;
import com.library.services.exceptions.ResourceNotFoundException;
import com.library.services.exceptions.TokenExpiredException;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements IAuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Value("${email.password-recover.token.minutes}")
    private Long tokenMinutes;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordRecoverRepository passwordRecoverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordRecoveryPublisher passwordRecoveryPublisher;

    @Override
    @Transactional
    public void createRecoverToken(EmailDTO body) {
        log.info("Password recovery requested for email={}", body.getEmail());

        userRepository.findByEmail(body.getEmail())
                .orElseThrow(() -> {
                    log.warn("Password recovery failed — email not found: {}", body.getEmail());
                    return new ResourceNotFoundException("Email not found");
                });

        String token = UUID.randomUUID().toString();

        PasswordRecover entity = new PasswordRecover();
        entity.setToken(token);
        entity.setEmail(body.getEmail());
        entity.setExpiration(Instant.now().plusSeconds(tokenMinutes * 60L));

        passwordRecoverRepository.save(entity);

        passwordRecoveryPublisher.publishPasswordRecovery(body.getEmail(), token);

        log.info("Recovery token created and published to SNS | email={} | expiresIn={}min",
                body.getEmail(), tokenMinutes);
    }

    @Override
    @Transactional
    public void saveNewPassword(NewPasswordDTO body) {
        log.info("Attempting to save new password with token={}", body.getToken());

        List<PasswordRecover> list = passwordRecoverRepository
                .searchValidTokens(body.getToken(), Instant.now());

        if (list.isEmpty()) {
            log.warn("Save password failed — invalid or expired token={}", body.getToken());
            throw new InvalidTokenException("Invalid or expired token");
        }

        String email = list.get(0).getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Save password failed — user not found for email={}", email);
                    return new ResourceNotFoundException("User not found: " + email);
                });

        user.setPassword(passwordEncoder.encode(body.getPassword()));
        userRepository.save(user);

        log.info("Password updated successfully for email={}", email);
    }

    @Override
    @Transactional
    public boolean isValidToken(String token) {
        log.info("Validating recovery token={}", token);

        PasswordRecover recover = passwordRecoverRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Token validation failed — token not found: {}", token);
                    return new InvalidTokenException("Token not found");
                });

        if (recover.getExpiration().isBefore(Instant.now())) {
            log.warn("Token validation failed — token expired: {} | expiredAt={}",
                    token, recover.getExpiration());
            throw new TokenExpiredException("Token expired");
        }

        log.info("Token is valid | token={} | expiresAt={}", token, recover.getExpiration());
        return true;
    }
}