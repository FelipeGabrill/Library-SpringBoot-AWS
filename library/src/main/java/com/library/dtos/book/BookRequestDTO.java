package com.library.dtos.book;

import com.library.dtos.category.CategoryDTO;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class BookRequestDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
    private String author;

    @Size(max = 100, message = "Publisher must be at most 100 characters")
    private String publisher;

    private Integer publicationYear;

    @Size(max = 20, message = "ISBN must be at most 20 characters")
    private String isbn;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    private Integer totalCopies;

    private Integer availableCopies;

    private boolean available;

    @Size(
            max = 4,
            message = "A maximum of 4 images can be uploaded for a book"
    )
    private List<MultipartFile> media;

    @NotNull(message = "Category is required")
    private Long categoryId;

    public BookRequestDTO() {
    }
}