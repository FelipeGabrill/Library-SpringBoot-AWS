package com.library.controllers;

import com.library.dtos.reservation.ReservationResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservations", description = "Reservation management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/reservations", produces = "application/json")
public interface IReservationController {

    @Operation(summary = "Find reservation by ID", description = "Returns a single reservation by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/{id}")
    ResponseEntity<ReservationResponseDTO> findById(
            @Parameter(description = "Reservation identifier", example = "1") @PathVariable Long id);

    @Operation(summary = "List reservations by user",
            description = "Returns a paginated list of reservations for a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservations returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/user/{userId}")
    ResponseEntity<Page<ReservationResponseDTO>> findByUser(
            @Parameter(description = "User identifier", example = "2") @PathVariable Long userId,
            Pageable pageable);

    @Operation(summary = "List active reservations by book",
            description = "Returns a paginated list of active reservations for a specific book.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservations returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/book/{bookId}")
    ResponseEntity<Page<ReservationResponseDTO>> findByBook(
            @Parameter(description = "Book identifier", example = "1") @PathVariable Long bookId,
            Pageable pageable);

    @Operation(summary = "Create a reservation",
            description = "Creates a reservation for the authenticated user. Only allowed when the book "
                    + "has no available copies and the user has no active reservation for it.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "Book is available or duplicate reservation", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping("/book/{bookId}")
    ResponseEntity<ReservationResponseDTO> create(
            @Parameter(description = "Book identifier to reserve", example = "1") @PathVariable Long bookId);

    @Operation(summary = "Cancel a reservation",
            description = "Cancels an active reservation, setting its status to CANCELLED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation cancelled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "Reservation is not active", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PutMapping("/{id}/cancel")
    ResponseEntity<ReservationResponseDTO> cancel(
            @Parameter(description = "Reservation identifier", example = "1") @PathVariable Long id);
}
