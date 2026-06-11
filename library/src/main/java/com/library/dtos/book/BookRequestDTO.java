package com.library.dtos.book;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Schema(description = "Payload to create or update a book (multipart/form-data)")
public class BookRequestDTO {

    @Schema(description = "Book identifier (ignored on creation)",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Schema(description = "Book title", example = "Clean Code",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
    @Schema(description = "Book author", example = "Robert C. Martin",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String author;

    @NotBlank(message = "Publisher is required")
    @Size(max = 100, message = "Publisher must be at most 100 characters")
    @Schema(description = "Publisher name", example = "Prentice Hall")
    private String publisher;

    @NotNull(message = "Publication Year is required")
    @Schema(description = "Year of publication", example = "2008")
    private Integer publicationYear;

    @NotNull(message = "isbn is required")
    @Size(max = 20, message = "ISBN must be at most 20 characters")
    @Schema(description = "International Standard Book Number (unique)",
            example = "9780132350884")
    private String isbn;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    @Schema(description = "Total number of copies owned by the library",
            example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalCopies;

    @Schema(description = "Number of copies currently available (managed by the system)",
            example = "3", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer availableCopies;

    @Schema(description = "Whether the book has at least one available copy",
            example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean available;

    @Size(max = 4, message = "A maximum of 4 images can be uploaded for a book")
    @Schema(description = "Cover images (max 4). Accepted types: JPG, PNG, WebP. Max 5MB each",
            type = "string", format = "binary")
    private List<MultipartFile> media;

    @NotNull(message = "Category is required")
    @Schema(description = "Identifier of the category this book belongs to",
            example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    public BookRequestDTO() {
    }
}