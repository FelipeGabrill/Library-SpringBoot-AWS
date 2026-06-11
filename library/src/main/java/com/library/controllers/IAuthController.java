package com.library.controllers;

import com.library.dtos.auth.EmailDTO;
import com.library.dtos.auth.NewPasswordDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Password recovery and token validation endpoints (public)")
@RequestMapping(value = "/auth", produces = "application/json")
public interface IAuthController {

    @Operation(
            summary = "Request password recovery token",
            description = "Generates a recovery token for the given email and publishes an event "
                    + "to SNS so the Lambda worker sends the recovery email via SES. Public endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Recovery token created and email dispatched"),
            @ApiResponse(responseCode = "400", description = "Invalid email format", content = @Content),
            @ApiResponse(responseCode = "404", description = "Email not registered", content = @Content)
    })
    @PostMapping("/recover-token")
    ResponseEntity<Void> createRecoverToken(@Valid @RequestBody EmailDTO dto);

    @Operation(
            summary = "Set a new password",
            description = "Validates the recovery token and updates the user's password. Public endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token / validation error", content = @Content),
            @ApiResponse(responseCode = "404", description = "User associated with the token not found", content = @Content)
    })
    @PutMapping("/new-password")
    ResponseEntity<Void> saveNewPassword(@Valid @RequestBody NewPasswordDTO dto);

    @Operation(
            summary = "Validate a recovery token",
            description = "Checks whether a given recovery token exists and has not expired. Public endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token is valid",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "400", description = "Token does not exist", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token expired", content = @Content)
    })
    @GetMapping("/validate-token")
    ResponseEntity<Boolean> validateToken(
            @Parameter(description = "Recovery token to validate",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam String token);
}