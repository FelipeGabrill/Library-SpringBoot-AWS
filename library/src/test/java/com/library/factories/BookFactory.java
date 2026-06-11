package com.library.factories;

import com.library.dtos.book.BookRequestDTO;
import com.library.models.entities.Book;
import com.library.models.entities.Category;

import java.util.ArrayList;

public class BookFactory {

    public static Book createBook() {
        Category category = CategoryFactory.createCategory();
        Book book = new Book(
                1L,
                "Clean Code",
                "Robert C. Martin",
                "Prentice Hall",
                2008,
                "9780132350884",
                3,
                3,
                new ArrayList<>(),
                category,
                new ArrayList<>(),
                new ArrayList<>()
        );
        return book;
    }

    public static Book createBook(Long id, String title, int totalCopies, int availableCopies) {
        Category category = CategoryFactory.createCategory();
        return new Book(
                id,
                title,
                "Author",
                "Publisher",
                2020,
                "ISBN-" + id,
                totalCopies,
                availableCopies,
                new ArrayList<>(),
                category,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public static Book createUnavailableBook() {
        Book book = createBook();
        book.setAvailableCopies(0);
        return book;
    }

    public static BookRequestDTO createBookRequestDTO() {
        BookRequestDTO dto = new BookRequestDTO();
        dto.setTitle("Clean Code");
        dto.setAuthor("Robert C. Martin");
        dto.setPublisher("Prentice Hall");
        dto.setPublicationYear(2008);
        dto.setIsbn("9780132350884");
        dto.setTotalCopies(3);
        dto.setCategoryId(1L);
        return dto;
    }
}
