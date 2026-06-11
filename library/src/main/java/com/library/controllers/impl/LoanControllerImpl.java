package com.library.controllers.impl;

import java.net.URI;

import com.library.controllers.ILoanController;
import com.library.dtos.loan.LoanResponseDTO;
import com.library.models.entities.enums.LoanStatus;
import com.library.services.ILoanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class LoanControllerImpl implements ILoanController {

    @Autowired
    private ILoanService service;

    @Override
    public ResponseEntity<LoanResponseDTO> findById(Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Override
    public ResponseEntity<Page<LoanResponseDTO>> findByUser(Long userId, Pageable pageable) {
        return ResponseEntity.ok(service.findByUser(userId, pageable));
    }

    @Override
    public ResponseEntity<Page<LoanResponseDTO>> findByUserAndStatus(Long userId, LoanStatus status, Pageable pageable) {
        return ResponseEntity.ok(service.findByUserAndStatus(userId, status, pageable));
    }

    @Override
    public ResponseEntity<Page<LoanResponseDTO>> findByStatus(LoanStatus status, Pageable pageable) {
        return ResponseEntity.ok(service.findByStatus(status, pageable));
    }

    @Override
    public ResponseEntity<Page<LoanResponseDTO>> findOverdue(Pageable pageable) {
        return ResponseEntity.ok(service.findOverdue(pageable));
    }

    @Override
    public ResponseEntity<Long> countByStatus(LoanStatus status) {
        return ResponseEntity.ok(service.countByStatus(status));
    }

    @Override
    public ResponseEntity<LoanResponseDTO> register(Long bookId) {
        LoanResponseDTO newDto = service.register(bookId);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newDto.getId())
                .toUri();
        return ResponseEntity.created(uri).body(newDto);
    }

    @Override
    public ResponseEntity<LoanResponseDTO> returnBook(Long id) {
        return ResponseEntity.ok(service.returnBook(id));
    }

    @Override
    public ResponseEntity<String> processOverdue() {
        int processed = service.processOverdueLoans();
        return ResponseEntity.ok("Processed " + processed + " overdue loans");
    }
}