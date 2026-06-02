package com.library.services;

import com.library.dtos.loan.LoanResponseDTO;
import com.library.models.entities.Loan;
import com.library.models.entities.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface defining the loan management operations.
 */
public interface ILoanService {

    /**
     * Retrieves a loan by its ID.
     *
     * @param id the loan ID
     * @return the loan data
     * @throws ResourceNotFoundException if the loan is not found
     */
    LoanResponseDTO findById(Long id);

    /**
     * Retrieves all loans for a specific user with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return paginated list of loans
     */
    Page<LoanResponseDTO> findByUser(Long userId, Pageable pageable);

    /**
     * Retrieves all loans for a specific user filtered by status.
     *
     * @param userId the user ID
     * @param status the loan status filter
     * @param pageable pagination parameters
     * @return paginated list of loans
     */
    Page<LoanResponseDTO> findByUserAndStatus(Long userId, LoanStatus status, Pageable pageable);

    /**
     * Retrieves all loans filtered by status.
     *
     * @param status the loan status filter
     * @param pageable pagination parameters
     * @return paginated list of loans
     */
    Page<LoanResponseDTO> findByStatus(LoanStatus status, Pageable pageable);

    /**
     * Retrieves all loans that are currently overdue (ACTIVE and past due date).
     *
     * @param pageable pagination parameters
     * @return paginated list of overdue loans
     */
    Page<LoanResponseDTO> findOverdue(Pageable pageable);

    /**
     * Counts loans by status — used for dashboard metrics.
     *
     * @param status the loan status
     * @return total count
     */
    long countByStatus(LoanStatus status);

    /**
     * Counts active loans for a specific user — used to enforce the max loan limit.
     *
     * @param userId the user ID
     * @return count of active loans
     */
    long countActiveByUser(Long userId);

    /**
     * Checks if a user already has an active loan for a specific book.
     *
     * @param userId the user ID
     * @param bookId the book ID
     * @return true if the user has an active loan for the book
     */
    boolean hasActiveLoan(Long userId, Long bookId);

    /**
     * Returns all overdue loans as a list — used internally by the batch process.
     *
     * @return list of overdue loans
     */
    List<Loan> findOverdueList();

    /**
     * Registers a new loan for the authenticated user.
     * Validates availability, max loan limit, and duplicate loans.
     * Decrements available copies on the book.
     *
     * @param bookId the book ID
     * @return the registered loan
     * @throws BusinessException if the book is unavailable, limit is reached,
     *                           or the user already has this book on loan
     */
    LoanResponseDTO register(Long bookId);

    /**
     * Returns a book, marks the loan as RETURNED, increments available copies,
     * and notifies users with active reservations for that book via SNS.
     *
     * @param loanId the loan ID
     * @return the updated loan
     * @throws ResourceNotFoundException if the loan is not found
     * @throws BusinessException if the book was already returned
     */
    LoanResponseDTO returnBook(Long loanId);

    /**
     * Batch process that marks all overdue ACTIVE loans as OVERDUE
     * and increments the notification count.
     * Called manually via endpoint or automatically by the Lambda batch.
     *
     * @return number of loans processed
     */
    int processOverdueLoans();
}