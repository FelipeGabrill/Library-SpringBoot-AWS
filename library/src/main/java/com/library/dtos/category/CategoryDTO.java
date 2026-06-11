package com.library.dtos.category;

import com.library.models.entities.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Category of books")
public class CategoryDTO {

    @Schema(description = "Unique identifier of the category",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 80, message = "Name must be between 1 and 80 characters")
    @Schema(description = "Category name (unique)", example = "Technology",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must be at most 255 characters")
    @Schema(description = "Short description of the category",
            example = "Programming and software books")
    private String description;

    public CategoryDTO() {
    }

    public CategoryDTO(Category entity) {
        id = entity.getId();
        name = entity.getName();
        description = entity.getDescription();
    }
}