package com.library.services.impl;

import com.library.dtos.loan.LoanResponseDTO;
import com.library.models.entities.Book;
import com.library.models.entities.Loan;
import com.library.models.entities.User;
import com.library.models.entities.enums.LoanStatus;
import com.library.models.repositories.LoanRepository;
import com.library.services.IBookService;
import com.library.services.IUserService;
import com.library.services.exceptions.BusinessException;
import com.library.services.exceptions.ResourceNotFoundException;
import com.library.factories.BookFactory;
import com.library.factories.LoanFactory;
import com.library.factories.UserFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @InjectMocks
    private LoanServiceImpl service;

    @Mock
    private LoanRepository repository;

    @Mock
    private ReservationServiceImpl reservationService;

    @Mock
    private IUserService userService;

    @Mock
    private IBookService bookService;

    private long existingId;
    private long nonExistingId;
    private long bookId;
    private User user;
    private Book book;
    private Loan loan;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 99L;
        bookId = 1L;
        user = UserFactory.createUser();
        book = BookFactory.createBook();
        loan = LoanFactory.createActiveLoan(existingId, user, book);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findByIdShouldReturnLoanResponseDTOWhenIdExists() {
        when(repository.findById(existingId)).thenReturn(Optional.of(loan));

        LoanResponseDTO result = service.findById(existingId);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
    }

    @Test
    void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(nonExistingId));
    }

    @Test
    void findByUserShouldReturnPageOfLoanResponseDTO() {
        Page<Loan> page = new PageImpl<>(List.of(loan));
        when(repository.findByUserId(1L, pageable)).thenReturn(page);

        Page<LoanResponseDTO> result = service.findByUser(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByUserAndStatusShouldReturnPageOfLoanResponseDTO() {
        Page<Loan> page = new PageImpl<>(List.of(loan));
        when(repository.findByUserIdAndStatus(1L, LoanStatus.ACTIVE, pageable)).thenReturn(page);

        Page<LoanResponseDTO> result = service.findByUserAndStatus(1L, LoanStatus.ACTIVE, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByStatusShouldReturnPageOfLoanResponseDTO() {
        Page<Loan> page = new PageImpl<>(List.of(loan));
        when(repository.findByStatus(LoanStatus.ACTIVE, pageable)).thenReturn(page);

        Page<LoanResponseDTO> result = service.findByStatus(LoanStatus.ACTIVE, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findOverdueShouldReturnPageOfLoanResponseDTO() {
        Page<Loan> page = new PageImpl<>(List.of(loan));
        when(repository.findOverdueLoans(any(), eq(pageable))).thenReturn(page);

        Page<LoanResponseDTO> result = service.findOverdue(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findOverdueListShouldReturnListOfLoans() {
        when(repository.findOverdueLoans(any())).thenReturn(List.of(loan));

        List<Loan> result = service.findOverdueList();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void countByStatusShouldReturnCount() {
        when(repository.countByStatus(LoanStatus.ACTIVE)).thenReturn(5L);

        long result = service.countByStatus(LoanStatus.ACTIVE);

        assertEquals(5L, result);
    }

    @Test
    void countActiveByUserShouldReturnCount() {
        when(repository.countByUserIdAndStatus(1L, LoanStatus.ACTIVE)).thenReturn(2L);

        long result = service.countActiveByUser(1L);

        assertEquals(2L, result);
    }

    @Test
    void hasActiveLoanShouldReturnTrueWhenExists() {
        when(repository.existsByUserIdAndBookIdAndStatus(1L, bookId, LoanStatus.ACTIVE)).thenReturn(true);

        assertTrue(service.hasActiveLoan(1L, bookId));
    }

    @Test
    void registerShouldReturnLoanResponseDTOWhenValid() {
        when(userService.authenticated()).thenReturn(user);
        when(bookService.getBook(bookId)).thenReturn(book);
        when(repository.countByUserIdAndStatus(user.getId(), LoanStatus.ACTIVE)).thenReturn(0L);
        when(repository.existsByUserIdAndBookIdAndStatus(user.getId(), bookId, LoanStatus.ACTIVE)).thenReturn(false);
        when(repository.save(any(Loan.class))).thenReturn(loan);

        LoanResponseDTO result = service.register(bookId);

        assertNotNull(result);
        assertEquals(2, book.getAvailableCopies()); // decrementou de 3 para 2
        verify(repository).save(any(Loan.class));
    }

    @Test
    void registerShouldThrowBusinessExceptionWhenNoCopiesAvailable() {
        Book unavailable = BookFactory.createUnavailableBook();
        when(userService.authenticated()).thenReturn(user);
        when(bookService.getBook(bookId)).thenReturn(unavailable);

        assertThrows(BusinessException.class, () -> service.register(bookId));
        verify(repository, never()).save(any(Loan.class));
    }

    @Test
    void registerShouldThrowBusinessExceptionWhenMaxActiveLoansReached() {
        when(userService.authenticated()).thenReturn(user);
        when(bookService.getBook(bookId)).thenReturn(book);
        when(repository.countByUserIdAndStatus(user.getId(), LoanStatus.ACTIVE)).thenReturn(3L);

        assertThrows(BusinessException.class, () -> service.register(bookId));
        verify(repository, never()).save(any(Loan.class));
    }

    @Test
    void registerShouldThrowBusinessExceptionWhenUserAlreadyHasBook() {
        when(userService.authenticated()).thenReturn(user);
        when(bookService.getBook(bookId)).thenReturn(book);
        when(repository.countByUserIdAndStatus(user.getId(), LoanStatus.ACTIVE)).thenReturn(1L);
        when(repository.existsByUserIdAndBookIdAndStatus(user.getId(), bookId, LoanStatus.ACTIVE)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.register(bookId));
        verify(repository, never()).save(any(Loan.class));
    }

    @Test
    void returnBookShouldReturnLoanResponseDTOWhenValid() {
        Book borrowedBook = BookFactory.createBook(1L, "Clean Code", 3, 2);
        Loan activeLoan = LoanFactory.createActiveLoan(existingId, user, borrowedBook);

        int initialAvailable = borrowedBook.getAvailableCopies();
        when(repository.findById(existingId)).thenReturn(Optional.of(activeLoan));
        when(repository.save(any(Loan.class))).thenReturn(activeLoan);

        LoanResponseDTO result = service.returnBook(existingId);

        assertNotNull(result);
        assertEquals(LoanStatus.RETURNED, activeLoan.getStatus());
        assertNotNull(activeLoan.getReturnedAt());
        assertEquals(initialAvailable + 1, borrowedBook.getAvailableCopies());
        verify(reservationService).notifyInterestedUsers(borrowedBook);
    }

    @Test
    void returnBookShouldThrowResourceNotFoundExceptionWhenLoanNotFound() {
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.returnBook(nonExistingId));
    }

    @Test
    void returnBookShouldThrowBusinessExceptionWhenAlreadyReturned() {
        Loan returned = LoanFactory.createReturnedLoan();
        when(repository.findById(existingId)).thenReturn(Optional.of(returned));

        assertThrows(BusinessException.class, () -> service.returnBook(existingId));
        verify(repository, never()).save(any(Loan.class));
    }

    @Test
    void processOverdueLoansShouldMarkLoansAsOverdueAndReturnCount() {
        Loan overdue1 = LoanFactory.createActiveLoan(1L, user, book);
        Loan overdue2 = LoanFactory.createActiveLoan(2L, user, book);
        when(repository.findOverdueLoans(any())).thenReturn(List.of(overdue1, overdue2));
        when(repository.save(any(Loan.class))).thenReturn(overdue1);

        int processed = service.processOverdueLoans();

        assertEquals(2, processed);
        assertEquals(LoanStatus.OVERDUE, overdue1.getStatus());
        assertEquals(LoanStatus.OVERDUE, overdue2.getStatus());
        verify(repository, times(2)).save(any(Loan.class));
    }

    @Test
    void processOverdueLoansShouldReturnZeroWhenNoOverdueLoans() {
        when(repository.findOverdueLoans(any())).thenReturn(List.of());

        int processed = service.processOverdueLoans();

        assertEquals(0, processed);
        verify(repository, never()).save(any(Loan.class));
    }
}