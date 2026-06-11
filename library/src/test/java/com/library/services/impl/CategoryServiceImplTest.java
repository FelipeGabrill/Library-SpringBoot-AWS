package com.library.services.impl;

import com.library.dtos.category.CategoryDTO;
import com.library.models.entities.Category;
import com.library.models.repositories.CategoryRepository;
import com.library.services.exceptions.BusinessException;
import com.library.services.exceptions.DatabaseException;
import com.library.services.exceptions.ResourceNotFoundException;
import com.library.factories.CategoryFactory;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl service;

    @Mock
    private CategoryRepository repository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private Category category;
    private CategoryDTO categoryDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 99L;
        dependentId = 4L;
        category = CategoryFactory.createCategory();
        categoryDTO = CategoryFactory.createCategoryDTO();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findByIdShouldReturnCategoryDTOWhenIdExists() {
        when(repository.findById(existingId)).thenReturn(Optional.of(category));

        CategoryDTO result = service.findById(existingId);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
        assertEquals(category.getName(), result.getName());
        verify(repository).findById(existingId);
    }

    @Test
    void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(nonExistingId));
        verify(repository).findById(nonExistingId);
    }

    @Test
    void findAllShouldReturnPageOfCategoryDTO() {
        Page<Category> page = new PageImpl<>(List.of(category));
        when(repository.findAll(pageable)).thenReturn(page);

        Page<CategoryDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(pageable);
    }

    @Test
    void findByNameShouldReturnPageOfCategoryDTO() {
        Page<Category> page = new PageImpl<>(List.of(category));
        when(repository.findByNameIgnoreCaseContaining("tech", pageable)).thenReturn(page);

        Page<CategoryDTO> result = service.findByName("tech", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findByNameIgnoreCaseContaining("tech", pageable);
    }

    @Test
    void insertShouldReturnCategoryDTOWhenNameIsUnique() {
        when(repository.findByNameIgnoreCase(categoryDTO.getName())).thenReturn(Optional.empty());
        when(repository.save(any(Category.class))).thenReturn(category);

        CategoryDTO result = service.insert(categoryDTO);

        assertNotNull(result);
        assertEquals(category.getName(), result.getName());
        verify(repository).save(any(Category.class));
    }

    @Test
    void insertShouldThrowBusinessExceptionWhenNameAlreadyExists() {
        Category existing = CategoryFactory.createCategory(5L, categoryDTO.getName());
        when(repository.findByNameIgnoreCase(categoryDTO.getName())).thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> service.insert(categoryDTO));
        verify(repository, never()).save(any(Category.class));
    }

    @Test
    void updateShouldReturnCategoryDTOWhenIdExists() {
        when(repository.getReferenceById(existingId)).thenReturn(category);
        when(repository.save(any(Category.class))).thenReturn(category);

        CategoryDTO result = service.update(existingId, categoryDTO);

        assertNotNull(result);
        assertEquals(category.getName(), result.getName());
        verify(repository).save(any(Category.class));
    }

    @Test
    void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(nonExistingId, categoryDTO));
    }

    @Test
    void deleteShouldDoNothingWhenIdExists() {
        when(repository.existsById(existingId)).thenReturn(true);
        doNothing().when(repository).deleteById(existingId);

        assertDoesNotThrow(() -> service.delete(existingId));
        verify(repository).deleteById(existingId);
    }

    @Test
    void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.existsById(nonExistingId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(nonExistingId));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void deleteShouldThrowDatabaseExceptionWhenIntegrityViolation() {
        when(repository.existsById(dependentId)).thenReturn(true);
        doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);

        assertThrows(DatabaseException.class, () -> service.delete(dependentId));
    }
}
