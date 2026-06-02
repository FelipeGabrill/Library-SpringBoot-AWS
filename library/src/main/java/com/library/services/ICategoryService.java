package com.library.services;

import com.library.dtos.category.CategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface defining the category management operations.
 */
public interface ICategoryService {

    /**
     * Retrieves a category by its ID.
     *
     * @param id the category ID
     * @return the category data
     * @throws ResourceNotFoundException if the category is not found
     */
    CategoryDTO findById(Long id);

    /**
     * Retrieves all categories with pagination.
     *
     * @param pageable pagination parameters
     * @return paginated list of categories
     */
    Page<CategoryDTO> findAll(Pageable pageable);

    /**
     * Searches categories by name (case insensitive, partial match).
     *
     * @param name search term
     * @param pageable pagination parameters
     * @return paginated list of matching categories
     */
    Page<CategoryDTO> findByName(String name, Pageable pageable);

    /**
     * Creates a new category, validating that the name is not already taken.
     *
     * @param dto the category data
     * @return the created category
     * @throws BusinessException if the name already exists
     */
    CategoryDTO insert(CategoryDTO dto);

    /**
     * Updates an existing category.
     *
     * @param id the category ID
     * @param dto the updated category data
     * @return the updated category
     * @throws ResourceNotFoundException if the category is not found
     */
    CategoryDTO update(Long id, CategoryDTO dto);

    /**
     * Deletes a category by ID.
     *
     * @param id the category ID
     * @throws ResourceNotFoundException if the category is not found
     * @throws DatabaseException if the category has books associated
     */
    void delete(Long id);
}
