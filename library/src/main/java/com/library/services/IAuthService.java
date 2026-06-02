package com.library.services;

import com.library.dtos.auth.EmailDTO;
import com.library.dtos.auth.NewPasswordDTO;

/**
 * Interface defining the authentication and password recovery operations.
 */
public interface IAuthService {

    /**
     * Generates a password recovery token for the given email and publishes
     * a recovery event via SNS so the Lambda worker can send the email.
     *
     * @param body DTO containing the user's email
     * @throws ResourceNotFoundException if the email is not registered
     */
    void createRecoverToken(EmailDTO body);

    /**
     * Validates the recovery token and updates the user's password.
     *
     * @param body DTO containing the token and the new password
     * @throws InvalidTokenException if the token does not exist or is expired
     * @throws ResourceNotFoundException if the user associated with the token is not found
     */
    void saveNewPassword(NewPasswordDTO body);

    /**
     * Checks whether a given recovery token is still valid.
     *
     * @param token the recovery token to validate
     * @return true if the token exists and has not expired
     * @throws InvalidTokenException if the token does not exist
     * @throws TokenExpiredException if the token has expired
     */
    boolean isValidToken(String token);
}