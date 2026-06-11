package com.library.dtos.book;

import com.library.dtos.category.CategoryDTO;
import com.library.models.entities.Book;
import com.library.models.entities.Media;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "Book data returned by the API")
public class BookResponseDTO {

    @Schema(description = "Unique identifier of the book", example = "1")
    private Long id;

    @Schema(description = "Book title", example = "Clean Code")
    private String title;

    @Schema(description = "Book author", example = "Robert C. Martin")
    private String author;

    @Schema(description = "Publisher name", example = "Prentice Hall")
    private String publisher;

    @Schema(description = "Year of publication", example = "2008")
    private Integer publicationYear;

    @Schema(description = "International Standard Book Number", example = "9780132350884")
    private String isbn;

    @Schema(description = "Total number of copies", example = "3")
    private Integer totalCopies;

    @Schema(description = "Number of copies currently available", example = "2")
    private Integer availableCopies;

    @Schema(description = "Whether the book has at least one available copy", example = "true")
    private boolean available;

    @Schema(description = "Cover images associated with the book")
    private List<Media> media = new ArrayList<>();

    @Schema(description = "Category this book belongs to")
    private CategoryDTO category;

    public BookResponseDTO() {
    }

    public BookResponseDTO(Book entity) {
        id = entity.getId();
        title = entity.getTitle();
        author = entity.getAuthor();
        publisher = entity.getPublisher();
        publicationYear = entity.getPublicationYear();
        isbn = entity.getIsbn();
        totalCopies = entity.getTotalCopies();
        availableCopies = entity.getAvailableCopies();
        available = entity.isAvailable();

        if (entity.getMedia() != null) {
            media.addAll(entity.getMedia());
        }

        category = entity.getCategory() != null
                ? new CategoryDTO(entity.getCategory())
                : null;
    }
}