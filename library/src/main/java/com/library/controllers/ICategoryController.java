package com.library.controllers;

import com.library.dtos.category.CategoryDTO;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Categories", description = "Category management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/categories", produces = "application/json")
public interface ICategoryController {

    @Operation(summary = "List all categories", description = "Returns a paginated list of all categories.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid token", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping
    ResponseEntity<Page<CategoryDTO>> findAll(Pageable pageable);

    @Operation(summary = "Find category by ID", description = "Returns a single category by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/{id}")
    ResponseEntity<CategoryDTO> findById(
            @Parameter(description = "Category identifier", example = "1") @PathVariable Long id);

    @Operation(summary = "Search categories by name",
            description = "Returns a paginated list of categories whose name contains the given term (case insensitive).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search executed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/search/{name}")
    ResponseEntity<Page<CategoryDTO>> findByName(
            @Parameter(description = "Term to search in the category name", example = "tech")
            @PathVariable String name,
            Pageable pageable);

    @Operation(summary = "Create a category", description = "Creates a new category. The name must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "422", description = "Category name already exists", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping
    ResponseEntity<CategoryDTO> insert(@Valid @RequestBody CategoryDTO dto);

    @Operation(summary = "Update a category", description = "Updates an existing category by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PutMapping("/{id}")
    ResponseEntity<CategoryDTO> update(
            @Parameter(description = "Category identifier", example = "1") @PathVariable Long id,
            @Valid @RequestBody CategoryDTO dto);

    @Operation(summary = "Delete a category",
            description = "Deletes a category by its identifier. Fails if the category has associated books.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Category has associated books", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "Category identifier", example = "1") @PathVariable Long id);
}
