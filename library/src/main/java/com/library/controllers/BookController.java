package com.library.controllers;

import java.net.URI;

import com.library.dtos.book.BookRequestDTO;
import com.library.dtos.book.BookResponseDTO;
import com.library.services.IBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/books", produces = "application/json")
public class BookController {

    @Autowired
    private IBookService service;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping
    public ResponseEntity<Page<BookResponseDTO>> findAll(
            Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/search")
    public ResponseEntity<Page<BookResponseDTO>> search(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(service.search(query, pageable));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/available")
    public ResponseEntity<Page<BookResponseDTO>> findAvailable(
            Pageable pageable) {
        return ResponseEntity.ok(service.findAvailable(pageable));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<BookResponseDTO>> findByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(service.findByCategory(categoryId, pageable));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponseDTO> insert(
            @Valid @ModelAttribute BookRequestDTO dto) {
        BookResponseDTO newDto = service.insert(dto);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newDto.getId())
                .toUri();
        return ResponseEntity.created(uri).body(newDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BookResponseDTO> update(
            @PathVariable Long id,
            @Valid @ModelAttribute BookRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));

    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
