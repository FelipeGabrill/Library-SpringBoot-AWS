package com.library.controllers;

import java.net.URI;

import com.library.dtos.reservation.ReservationResponseDTO;
import com.library.services.IReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "/reservations", produces = "application/json")
public class ReservationController {

    @Autowired
    private IReservationService service;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> findById(@PathVariable Long id) {
        ReservationResponseDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReservationResponseDTO>> findByUser(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<ReservationResponseDTO> dto = service.findByUser(userId, pageable);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<Page<ReservationResponseDTO>> findByBook(
            @PathVariable Long bookId,
            Pageable pageable) {
        Page<ReservationResponseDTO> dto = service.findByBook(bookId, pageable);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping("/book/{bookId}")
    public ResponseEntity<ReservationResponseDTO> create(@PathVariable Long bookId) {
        ReservationResponseDTO newDto = service.create(bookId);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newDto.getId())
                .toUri();
        return ResponseEntity.created(uri).body(newDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancel(@PathVariable Long id) {
        ReservationResponseDTO dto = service.cancel(id);
        return ResponseEntity.ok(dto);
    }
}
