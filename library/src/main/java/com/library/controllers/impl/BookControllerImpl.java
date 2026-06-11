package com.library.controllers.impl;

import java.net.URI;

import com.library.controllers.IBookController;
import com.library.dtos.book.BookRequestDTO;
import com.library.dtos.book.BookResponseDTO;
import com.library.services.IBookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class BookControllerImpl implements IBookController {

    @Autowired
    private IBookService service;

    @Override
    public ResponseEntity<Page<BookResponseDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @Override
    public ResponseEntity<BookResponseDTO> findById(Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Override
    public ResponseEntity<Page<BookResponseDTO>> search(String query, Pageable pageable) {
        return ResponseEntity.ok(service.search(query, pageable));
    }

    @Override
    public ResponseEntity<Page<BookResponseDTO>> findAvailable(Pageable pageable) {
        return ResponseEntity.ok(service.findAvailable(pageable));
    }

    @Override
    public ResponseEntity<Page<BookResponseDTO>> findByCategory(Long categoryId, Pageable pageable) {
        return ResponseEntity.ok(service.findByCategory(categoryId, pageable));
    }

    @Override
    public ResponseEntity<BookResponseDTO> insert(BookRequestDTO dto) {
        BookResponseDTO newDto = service.insert(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newDto.getId())
                .toUri();
        return ResponseEntity.created(uri).body(newDto);
    }

    @Override
    public ResponseEntity<BookResponseDTO> update(Long id, BookRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
