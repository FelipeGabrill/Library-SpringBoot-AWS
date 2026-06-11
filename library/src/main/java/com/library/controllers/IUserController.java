package com.library.controllers;

import com.library.dtos.user.UserDTO;
import com.library.dtos.user.UserInsertDTO;
import com.library.dtos.user.UserUpdateDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "User management endpoints")
@RequestMapping(value = "/users", produces = "application/json")
public interface IUserController {

    @Operation(summary = "Get authenticated user",
            description = "Returns the data of the currently authenticated user, read from the JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated user returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/me")
    ResponseEntity<UserDTO> getMe();

    @Operation(summary = "Find user by ID", description = "Returns a single user by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/{id}")
    ResponseEntity<UserDTO> findById(
            @Parameter(description = "User identifier", example = "1") @PathVariable Long id);

    @Operation(summary = "List all users", description = "Returns a paginated list of all users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping
    ResponseEntity<Page<UserDTO>> findAll(Pageable pageable);

    @Operation(summary = "Register a new user",
            description = "Creates a new user with an optional profile picture (multipart/form-data). "
                    + "Public endpoint — no authentication required. Encodes the password, uploads the "
                    + "picture to S3 and publishes a UserCreated event to SNS so a welcome email is sent via SES.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "422", description = "Email already exists", content = @Content)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<UserDTO> insert(@Valid @ModelAttribute UserInsertDTO dto);

    @Operation(summary = "Update a user",
            description = "Updates an existing user. If a new profile picture is provided, the old one "
                    + "is deleted from S3 before uploading the new one (multipart/form-data).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<UserDTO> update(
            @Parameter(description = "User identifier", example = "1") @PathVariable Long id,
            @Valid @ModelAttribute UserUpdateDTO dto);

    @Operation(summary = "Delete a user",
            description = "Deletes a user by its identifier. Fails if the user has active loans or reservations.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "User has active loans or reservations", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "User identifier", example = "1") @PathVariable Long id);

    @Operation(summary = "Search users by email",
            description = "Returns a paginated list of users whose email contains the given term (case insensitive).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search executed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/email/{email}")
    ResponseEntity<Page<UserDTO>> findByEmail(
            @Parameter(description = "Term to search in the email", example = "felipe") @PathVariable String email,
            Pageable pageable);
}
