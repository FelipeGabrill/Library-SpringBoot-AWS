package com.library.factories;

import com.library.dtos.category.CategoryDTO;
import com.library.models.entities.Category;

public class CategoryFactory {

    public static Category createCategory() {
        return new Category(1L, "Technology", "Programming and software books", null);
    }

    public static Category createCategory(Long id, String name) {
        return new Category(id, name, "Description for " + name, null);
    }

    public static CategoryDTO createCategoryDTO() {
        Category category = createCategory();
        return new CategoryDTO(category);
    }

    public static CategoryDTO createCategoryDTO(Long id, String name) {
        Category category = createCategory(id, name);
        return new CategoryDTO(category);
    }
}

