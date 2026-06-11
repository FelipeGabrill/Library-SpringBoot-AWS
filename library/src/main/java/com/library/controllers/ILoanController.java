package com.library.controllers;

import com.library.dtos.loan.LoanResponseDTO;
import com.library.models.entities.enums.LoanStatus;

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

@Tag(name = "Loans", description = "Loan management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/loans", produces = "application/json")
public interface ILoanController {

    @Operation(summary = "Find loan by ID", description = "Returns a single loan by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Loan not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/{id}")
    ResponseEntity<LoanResponseDTO> findById(
            @Parameter(description = "Loan identifier", example = "1") @PathVariable Long id);

    @Operation(summary = "List loans by user", description = "Returns a paginated list of loans for a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loans returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/user/{userId}")
    ResponseEntity<Page<LoanResponseDTO>> findByUser(
            @Parameter(description = "User identifier", example = "2") @PathVariable Long userId,
            Pageable pageable);

    @Operation(summary = "List loans by user and status",
            description = "Returns a paginated list of loans for a specific user filtered by status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loans returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/user/{userId}/status/{status}")
    ResponseEntity<Page<LoanResponseDTO>> findByUserAndStatus(
            @Parameter(description = "User identifier", example = "2") @PathVariable Long userId,
            @Parameter(description = "Loan status", example = "ACTIVE") @PathVariable LoanStatus status,
            Pageable pageable);

    @Operation(summary = "List loans by status",
            description = "Returns a paginated list of loans filtered by status (ACTIVE, OVERDUE, RETURNED).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loans returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/status/{status}")
    ResponseEntity<Page<LoanResponseDTO>> findByStatus(
            @Parameter(description = "Loan status", example = "OVERDUE") @PathVariable LoanStatus status,
            Pageable pageable);

    @Operation(summary = "List overdue loans",
            description = "Returns a paginated list of loans that are currently overdue (ACTIVE and past due date).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overdue loans returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/overdue")
    ResponseEntity<Page<LoanResponseDTO>> findOverdue(Pageable pageable);

    @Operation(summary = "Count loans by status",
            description = "Returns the total number of loans with the given status — used for dashboard metrics.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Count returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/count/status/{status}")
    ResponseEntity<Long> countByStatus(
            @Parameter(description = "Loan status", example = "ACTIVE") @PathVariable LoanStatus status);

    @Operation(summary = "Register a loan",
            description = "Registers a new loan for the authenticated user. Validates availability, "
                    + "the max active loan limit (3) and duplicate loans. Decrements available copies.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Loan registered successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "No copies available, limit reached or duplicate loan", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping("/book/{bookId}")
    ResponseEntity<LoanResponseDTO> register(
            @Parameter(description = "Book identifier to borrow", example = "1") @PathVariable Long bookId);

    @Operation(summary = "Return a book",
            description = "Marks the loan as RETURNED, increments available copies and notifies users "
                    + "with active reservations for that book via SNS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Loan not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "Book was already returned", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PutMapping("/{id}/return")
    ResponseEntity<LoanResponseDTO> returnBook(
            @Parameter(description = "Loan identifier", example = "1") @PathVariable Long id);

    @Operation(summary = "Process overdue loans",
            description = "Batch operation that marks all overdue ACTIVE loans as OVERDUE and increments "
                    + "the notification count. Normally triggered by the Lambda batch via EventBridge.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overdue loans processed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping("/process-overdue")
    ResponseEntity<String> processOverdue();
}