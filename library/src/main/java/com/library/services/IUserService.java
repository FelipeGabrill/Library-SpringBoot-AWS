package com.library.services;

import com.library.dtos.user.UserDTO;
import com.library.dtos.user.UserInsertDTO;
import com.library.dtos.user.UserUpdateDTO;
import com.library.models.entities.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface defining the user management operations.
 */
public interface IUserService {

    /**
     * Retrieves a user by their ID.
     *
     * @param id the user ID
     * @return the user data
     * @throws ResourceNotFoundException if the user is not found
     */
    UserDTO findById(Long id);

    /**
     * Retrieves all users with pagination.
     *
     * @param pageable pagination parameters
     * @return paginated list of users
     */
    Page<UserDTO> findAll(Pageable pageable);

    /**
     * Searches users by email (case insensitive, partial match).
     *
     * @param email search term
     * @param pageable pagination parameters
     * @return paginated list of matching users
     */
    Page<UserDTO> findByEmail(String email, Pageable pageable);

    /**
     * Creates a new user, encodes the password, uploads the profile picture
     * to S3 if provided, and publishes a UserCreated event to SNS so the
     * Lambda worker can send a welcome email via SES.
     *
     * @param dto the user data including optional profile picture
     * @return the created user
     */
    UserDTO insert(UserInsertDTO dto);

    /**
     * Updates an existing user. If a new profile picture is provided,
     * the old one is deleted from S3 before uploading the new one.
     *
     * @param id the user ID
     * @param dto the updated user data
     * @return the updated user
     * @throws ResourceNotFoundException if the user is not found
     */
    UserDTO update(Long id, UserUpdateDTO dto);

    /**
     * Deletes a user by ID.
     *
     * @param id the user ID
     * @throws ResourceNotFoundException if the user is not found
     * @throws DatabaseException if the user has active loans or reservations
     */
    void delete(Long id);

    /**
     * Returns the data of the currently authenticated user
     * by reading the email claim from the JWT token.
     *
     * @return the authenticated user data
     * @throws UsernameNotFoundException if the token is invalid or user not found
     */
    UserDTO getMe();

    /**
     * Returns the authenticated User entity — used internally by
     * LoanService and ReservationService to get the current user.
     *
     * @return the authenticated User entity
     * @throws UsernameNotFoundException if the token is invalid or user not found
     */
    User authenticated();
}