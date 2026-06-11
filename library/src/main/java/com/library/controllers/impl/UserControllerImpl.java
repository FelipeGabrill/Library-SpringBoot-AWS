package com.library.controllers.impl;

import java.net.URI;

import com.library.controllers.IUserController;
import com.library.dtos.user.UserDTO;
import com.library.dtos.user.UserInsertDTO;
import com.library.dtos.user.UserUpdateDTO;
import com.library.services.IUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class UserControllerImpl implements IUserController {

    @Autowired
    private IUserService service;

    @Override
    public ResponseEntity<UserDTO> getMe() {
        return ResponseEntity.ok(service.getMe());
    }

    @Override
    public ResponseEntity<UserDTO> findById(Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Override
    public ResponseEntity<Page<UserDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @Override
    public ResponseEntity<UserDTO> insert(UserInsertDTO dto) {
        UserDTO newDto = service.insert(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newDto.getId())
                .toUri();
        return ResponseEntity.created(uri).body(newDto);
    }

    @Override
    public ResponseEntity<UserDTO> update(Long id, UserUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Page<UserDTO>> findByEmail(String email, Pageable pageable) {
        return ResponseEntity.ok(service.findByEmail(email, pageable));
    }
}