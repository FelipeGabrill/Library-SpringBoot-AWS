package com.library.controllers;

import com.library.dtos.book.BookRequestDTO;
import com.library.dtos.book.BookResponseDTO;

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

@Tag(name = "Books", description = "Book management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/books", produces = "application/json")
public interface IBookController {

    @Operation(summary = "List all books", description = "Returns a paginated list of all books.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Books returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping
    ResponseEntity<Page<BookResponseDTO>> findAll(Pageable pageable);

    @Operation(summary = "Find book by ID", description = "Returns a single book by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/{id}")
    ResponseEntity<BookResponseDTO> findById(
            @Parameter(description = "Book identifier", example = "1") @PathVariable Long id);

    @Operation(summary = "Search books", description = "Searches books by title or author (case insensitive).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search executed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/search")
    ResponseEntity<Page<BookResponseDTO>> search(
            @Parameter(description = "Term to search in title or author", example = "clean")
            @RequestParam String query,
            Pageable pageable);

    @Operation(summary = "List available books",
            description = "Returns a paginated list of books that have at least one available copy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Available books returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/available")
    ResponseEntity<Page<BookResponseDTO>> findAvailable(Pageable pageable);

    @Operation(summary = "List books by category",
            description = "Returns a paginated list of books belonging to a specific category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Books returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/category/{categoryId}")
    ResponseEntity<Page<BookResponseDTO>> findByCategory(
            @Parameter(description = "Category identifier", example = "1") @PathVariable Long categoryId,
            Pageable pageable);

    @Operation(summary = "Create a book",
            description = "Creates a new book with optional cover images (multipart/form-data). "
                    + "ISBN must be unique. Accepted image types: JPG, PNG, WebP (max 5MB each, up to 4 images).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "ISBN already exists", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<BookResponseDTO> insert(@Valid @ModelAttribute BookRequestDTO dto);

    @Operation(summary = "Update a book",
            description = "Updates an existing book. If new images are provided, the old ones are "
                    + "deleted from S3 before uploading the new ones (multipart/form-data).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "ISBN already exists for another book", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<BookResponseDTO> update(
            @Parameter(description = "Book identifier", example = "1") @PathVariable Long id,
            @Valid @ModelAttribute BookRequestDTO dto);

    @Operation(summary = "Delete a book",
            description = "Deletes a book by its identifier. Fails if the book has active loans or reservations.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Book has active loans or reservations", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "Book identifier", example = "1") @PathVariable Long id);
}