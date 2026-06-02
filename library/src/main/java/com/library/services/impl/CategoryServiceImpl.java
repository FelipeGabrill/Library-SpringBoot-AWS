package com.library.services.impl;

import com.library.dtos.category.CategoryDTO;
import com.library.models.entities.Category;
import com.library.models.repositories.CategoryRepository;
import com.library.services.ICategoryService;
import com.library.services.exceptions.BusinessException;
import com.library.services.exceptions.DatabaseException;
import com.library.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements ICategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        Category entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found | id={}", id);
                    return new ResourceNotFoundException("Resource not found");
                });
        return new CategoryDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(CategoryDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> findByName(String name, Pageable pageable) {
        log.info("Searching categories | name={}", name);
        return repository.findByNameIgnoreCaseContaining(name, pageable).map(CategoryDTO::new);
    }

    @Override
    @Transactional
    public CategoryDTO insert(CategoryDTO dto) {
        log.info("Inserting category | name={}", dto.getName());

        checkDuplicateName(dto.getName(), null);

        Category entity = new Category();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);

        log.info("Category created successfully | id={} | name={}", entity.getId(), entity.getName());
        return new CategoryDTO(entity);
    }

    @Override
    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        log.info("Updating category | id={} | name={}", id, dto.getName());
        try {
            Category entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);

            log.info("Category updated successfully | id={}", id);
            return new CategoryDTO(entity);
        } catch (EntityNotFoundException e) {
            log.warn("Category not found for update | id={}", id);
            throw new ResourceNotFoundException("Resource not found");
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        log.info("Deleting category | id={}", id);

        if (!repository.existsById(id)) {
            log.warn("Category not found for delete | id={}", id);
            throw new ResourceNotFoundException("Resource not found");
        }
        try {
            repository.deleteById(id);
            log.info("Category deleted successfully | id={}", id);
        } catch (DataIntegrityViolationException e) {
            log.error("Delete failed — category has associated books | id={}", id);
            throw new DatabaseException("Referential integrity violation");
        }
    }

    private void checkDuplicateName(String name, Long currentId) {
        repository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                log.warn("Duplicate category name | name={} | existingId={}", name, existing.getId());
                throw new BusinessException("Category name already exists: " + name);
            }
        });
    }

    private void copyDtoToEntity(CategoryDTO dto, Category entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
    }
}