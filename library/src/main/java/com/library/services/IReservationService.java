package com.library.services;

import com.library.dtos.reservation.ReservationResponseDTO;
import com.library.models.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface defining the reservation management operations.
 */
public interface IReservationService {

    /**
     * Retrieves a reservation by its ID.
     *
     * @param id the reservation ID
     * @return the reservation data
     * @throws ResourceNotFoundException if the reservation is not found
     */
    ReservationResponseDTO findById(Long id);

    /**
     * Retrieves all reservations for a specific user with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return paginated list of reservations
     */
    Page<ReservationResponseDTO> findByUser(Long userId, Pageable pageable);

    /**
     * Retrieves all active reservations for a specific book with pagination.
     *
     * @param bookId the book ID
     * @param pageable pagination parameters
     * @return paginated list of active reservations
     */
    Page<ReservationResponseDTO> findByBook(Long bookId, Pageable pageable);

    /**
     * Creates a reservation for the authenticated user.
     * Only allowed when the book has no available copies.
     *
     * @param bookId the book ID
     * @return the created reservation
     * @throws BusinessException if the book is available or the user already has
     *                           an active reservation for this book
     */
    ReservationResponseDTO create(Long bookId);

    /**
     * Cancels an active reservation.
     *
     * @param id the reservation ID
     * @return the updated reservation with CANCELLED status
     * @throws ResourceNotFoundException if the reservation is not found
     * @throws BusinessException if the reservation is not active
     */
    ReservationResponseDTO cancel(Long id);

    /**
     * Notifies all users with active reservations for a book that it is now available.
     * Changes reservation status to NOTIFIED and publishes a BookAvailable event
     * to SNS for each user — the Lambda worker sends the email via SES.
     *
     * @param book the book that became available
     */
    void notifyInterestedUsers(Book book);
}