package com.library.services;

import com.library.dtos.book.BookRequestDTO;
import com.library.dtos.book.BookResponseDTO;
import com.library.models.entities.Book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface defining the book management operations.
 */
public interface IBookService {

    /**
     * Retrieves a book by its ID.
     *
     * @param id the book ID
     * @return the book data
     * @throws ResourceNotFoundException if the book is not found
     */
    BookResponseDTO findById(Long id);

    /**
     * Retrieves all books with pagination.
     *
     * @param pageable pagination parameters
     * @return paginated list of books
     */
    Page<BookResponseDTO> findAll(Pageable pageable);

    /**
     * Searches books by title or author (case insensitive).
     *
     * @param query search term
     * @param pageable pagination parameters
     * @return paginated list of matching books
     */
    Page<BookResponseDTO> search(String query, Pageable pageable);

    /**
     * Retrieves all books belonging to a specific category.
     *
     * @param categoryId the category ID
     * @param pageable pagination parameters
     * @return paginated list of books in the category
     */
    Page<BookResponseDTO> findByCategory(Long categoryId, Pageable pageable);

    /**
     * Retrieves all books that have at least one available copy.
     *
     * @param pageable pagination parameters
     * @return paginated list of available books
     */
    Page<BookResponseDTO> findAvailable(Pageable pageable);

    /**
     * Creates a new book, validates the ISBN for duplicates,
     * and uploads cover images to S3 if provided.
     *
     * @param dto the book data including optional media files
     * @return the created book
     * @throws BusinessException if the ISBN already exists
     * @throws ResourceNotFoundException if the category is not found
     */
    BookResponseDTO insert(BookRequestDTO dto);

    /**
     * Updates an existing book. If new media files are provided,
     * the old ones are deleted from S3 before uploading the new ones.
     * Available copies are adjusted proportionally if totalCopies changes.
     *
     * @param id the book ID
     * @param dto the updated book data
     * @return the updated book
     * @throws ResourceNotFoundException if the book is not found
     * @throws BusinessException if the ISBN already exists for another book
     */
    BookResponseDTO update(Long id, BookRequestDTO dto);

    /**
     * Deletes a book by ID.
     *
     * @param id the book ID
     * @throws ResourceNotFoundException if the book is not found
     * @throws DatabaseException if the book has active loans or reservations
     */
    void delete(Long id);

    /**
     * Returns the Book entity directly — used internally by other services
     * such as LoanService and ReservationService.
     *
     * @param id the book ID
     * @return the Book entity
     * @throws ResourceNotFoundException if the book is not found
     */
    Book getBook(Long id);
}
