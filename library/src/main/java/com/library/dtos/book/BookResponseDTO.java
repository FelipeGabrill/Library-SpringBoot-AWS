package com.library.dtos.book;

import com.library.dtos.category.CategoryDTO;
import com.library.models.entities.Book;
import com.library.models.entities.Media;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BookResponseDTO {

    private Long id;

    private String title;

    private String author;

    private String publisher;

    private Integer publicationYear;

    private String isbn;

    private Integer totalCopies;

    private Integer availableCopies;

    private boolean available;

    private List<Media> media = new ArrayList<>();

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