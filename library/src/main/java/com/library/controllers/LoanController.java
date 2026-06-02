package com.library.controllers;

import java.net.URI;

import com.library.dtos.loan.LoanResponseDTO;
import com.library.models.entities.enums.LoanStatus;
import com.library.services.ILoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "/loans", produces = "application/json")
public class LoanController {

    @Autowired
    private ILoanService service;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> findById(@PathVariable Long id) {
        LoanResponseDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LoanResponseDTO>> findByUser(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<LoanResponseDTO> dto = service.findByUser(userId, pageable);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<Page<LoanResponseDTO>> findByUserAndStatus(
            @PathVariable Long userId,
            @PathVariable LoanStatus status,
            Pageable pageable) {
        Page<LoanResponseDTO> dto = service.findByUserAndStatus(userId, status, pageable);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<LoanResponseDTO>> findByStatus(
            @PathVariable LoanStatus status,
            Pageable pageable) {
        Page<LoanResponseDTO> dto = service.findByStatus(status, pageable);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/overdue")
    public ResponseEntity<Page<LoanResponseDTO>> findOverdue(
            Pageable pageable) {
        Page<LoanResponseDTO> dto = service.findOverdue(pageable);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countByStatus(@PathVariable LoanStatus status) {
        return ResponseEntity.ok(service.countByStatus(status));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping("/book/{bookId}")
    public ResponseEntity<LoanResponseDTO> register(@PathVariable Long bookId) {
        LoanResponseDTO newDto = service.register(bookId);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newDto.getId())
                .toUri();
        return ResponseEntity.created(uri).body(newDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponseDTO> returnBook(@PathVariable Long id) {
        LoanResponseDTO dto = service.returnBook(id);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping("/process-overdue")
    public ResponseEntity<String> processOverdue() {
        int processed = service.processOverdueLoans();
        return ResponseEntity.ok("Processed " + processed + " overdue loans");
    }
}