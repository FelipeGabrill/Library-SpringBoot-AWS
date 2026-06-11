package com.library.controllers.impl;

import java.net.URI;

import com.library.controllers.IReservationController;
import com.library.dtos.reservation.ReservationResponseDTO;
import com.library.services.IReservationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class ReservationControllerImpl implements IReservationController {

    @Autowired
    private IReservationService service;

    @Override
    public ResponseEntity<ReservationResponseDTO> findById(Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Override
    public ResponseEntity<Page<ReservationResponseDTO>> findByUser(Long userId, Pageable pageable) {
        return ResponseEntity.ok(service.findByUser(userId, pageable));
    }

    @Override
    public ResponseEntity<Page<ReservationResponseDTO>> findByBook(Long bookId, Pageable pageable) {
        return ResponseEntity.ok(service.findByBook(bookId, pageable));
    }

    @Override
    public ResponseEntity<ReservationResponseDTO> create(Long bookId) {
        ReservationResponseDTO newDto = service.create(bookId);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newDto.getId())
                .toUri();
        return ResponseEntity.created(uri).body(newDto);
    }

    @Override
    public ResponseEntity<ReservationResponseDTO> cancel(Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }
}
