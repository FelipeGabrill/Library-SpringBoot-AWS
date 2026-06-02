package com.library.dtos.category;

import com.library.models.entities.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 80, message = "Name must be between 1 and 80 characters")
    private String name;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    public CategoryDTO() {
    }

    public CategoryDTO(Category entity) {
        id = entity.getId();
        name = entity.getName();
        description = entity.getDescription();
    }
}
