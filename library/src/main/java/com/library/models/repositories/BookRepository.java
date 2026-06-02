package com.library.models.repositories;

import com.library.models.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("""
            SELECT obj FROM Book obj
            WHERE LOWER(obj.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(obj.author) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Book> searchByTitleOrAuthor(String query, Pageable pageable);

    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Book> findByAvailableCopiesGreaterThan(int copies, Pageable pageable);

    Optional<Book> findByIsbn(String isbn);
}
