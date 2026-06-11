package com.library.controllers.impl;

import java.net.URI;

import com.library.controllers.ICategoryController;
import com.library.dtos.category.CategoryDTO;
import com.library.services.ICategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class CategoryControllerImpl implements ICategoryController {

    @Autowired
    private ICategoryService service;

    @Override
    public ResponseEntity<Page<CategoryDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @Override
    public ResponseEntity<CategoryDTO> findById(Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Override
    public ResponseEntity<Page<CategoryDTO>> findByName(String name, Pageable pageable) {
        return ResponseEntity.ok(service.findByName(name, pageable));
    }

    @Override
    public ResponseEntity<CategoryDTO> insert(CategoryDTO dto) {
        CategoryDTO newDto = service.insert(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newDto.getId())
                .toUri();
        return ResponseEntity.created(uri).body(newDto);
    }

    @Override
    public ResponseEntity<CategoryDTO> update(Long id, CategoryDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
