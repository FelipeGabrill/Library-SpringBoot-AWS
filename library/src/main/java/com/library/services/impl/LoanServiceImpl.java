package com.library.services.impl;

import com.library.dtos.loan.LoanResponseDTO;
import com.library.models.entities.Book;
import com.library.models.entities.Loan;
import com.library.models.entities.User;
import com.library.models.entities.enums.LoanStatus;
import com.library.models.repositories.LoanRepository;
import com.library.services.IBookService;
import com.library.services.ILoanService;
import com.library.services.IUserService;
import com.library.services.exceptions.BusinessException;
import com.library.services.exceptions.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanServiceImpl implements ILoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanServiceImpl.class);

    private static final int MAX_ACTIVE_LOANS = 3;
    private static final int LOAN_DURATION_DAYS = 7;

    @Autowired
    private LoanRepository repository;

    @Autowired
    private ReservationServiceImpl reservationService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IBookService bookService;

    @Override
    @Transactional(readOnly = true)
    public LoanResponseDTO findById(Long id) {
        Loan entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Loan not found | id={}", id);
                    return new ResourceNotFoundException("Resource not found");
                });
        return new LoanResponseDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanResponseDTO> findByUser(Long userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(LoanResponseDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanResponseDTO> findByUserAndStatus(Long userId, LoanStatus status, Pageable pageable) {
        return repository.findByUserIdAndStatus(userId, status, pageable).map(LoanResponseDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanResponseDTO> findByStatus(LoanStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(LoanResponseDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanResponseDTO> findOverdue(Pageable pageable) {
        return repository.findOverdueLoans(LocalDate.now(), pageable).map(LoanResponseDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> findOverdueList() {
        return repository.findOverdueLoans(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(LoanStatus status) {
        return repository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByUser(Long userId) {
        return repository.countByUserIdAndStatus(userId, LoanStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveLoan(Long userId, Long bookId) {
        return repository.existsByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.ACTIVE);
    }

    @Override
    @Transactional
    public LoanResponseDTO register(Long bookId) {
        User user = userService.authenticated();
        Book book = bookService.getBook(bookId);

        log.info("Registering loan | userId={} | bookId={} | book={}",
                user.getId(), bookId, book.getTitle());

        validateLoanRegistration(user, book, bookId);

        book.decrementAvailable();

        Loan loan = buildLoan(user, book);
        loan = repository.save(loan);

        log.info("Loan registered successfully | loanId={} | userId={} | bookId={} | dueDate={}",
                loan.getId(), user.getId(), bookId, loan.getDueDate());

        return new LoanResponseDTO(loan);
    }

    @Override
    @Transactional
    public LoanResponseDTO returnBook(Long loanId) {
        log.info("Returning book | loanId={}", loanId);

        Loan loan = findLoanOrThrow(loanId);

        validateReturn(loan);

        markAsReturned(loan);

        Book book = loan.getBook();
        book.incrementAvailable();

        reservationService.notifyInterestedUsers(book);

        loan = repository.save(loan);

        log.info("Book returned successfully | loanId={} | bookId={} | book={}",
                loanId, book.getId(), book.getTitle());

        return new LoanResponseDTO(loan);
    }

    @Override
    @Transactional
    public int processOverdueLoans() {
        List<Loan> overdueLoans = findOverdueList();
        log.info("Processing overdue loans | found={}", overdueLoans.size());

        int processed = 0;
        for (Loan loan : overdueLoans) {
            markAsOverdue(loan);
            repository.save(loan);
            processed++;
        }

        log.info("Overdue processing completed | processed={}", processed);
        return processed;
    }

    private void validateLoanRegistration(User user, Book book, Long bookId) {
        if (!book.isAvailable()) {
            log.warn("Loan rejected — no copies available | bookId={} | book={}",
                    bookId, book.getTitle());
            throw new BusinessException("No copies available for: " + book.getTitle());
        }

        long activeLoans = countActiveByUser(user.getId());
        if (activeLoans >= MAX_ACTIVE_LOANS) {
            log.warn("Loan rejected — max active loans reached | userId={} | activeLoans={}",
                    user.getId(), activeLoans);
            throw new BusinessException(
                    "User reached the max of " + MAX_ACTIVE_LOANS + " active loans");
        }

        if (hasActiveLoan(user.getId(), bookId)) {
            log.warn("Loan rejected — user already has this book | userId={} | bookId={}",
                    user.getId(), bookId);
            throw new BusinessException("User already has this book on loan");
        }
    }

    private void validateReturn(Loan loan) {
        if (loan.getStatus() == LoanStatus.RETURNED) {
            log.warn("Return rejected — book already returned | loanId={}", loan.getId());
            throw new BusinessException("Book already returned");
        }
    }

    private Loan buildLoan(User user, Book book) {
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setBorrowedAt(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(LOAN_DURATION_DAYS));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setOverdueNotificationCount(0);
        return loan;
    }

    private void markAsReturned(Loan loan) {
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnedAt(LocalDate.now());
    }

    private void markAsOverdue(Loan loan) {
        if (loan.getOverdueSince() == null) {
            loan.setOverdueSince(LocalDate.now());
        }
        loan.setStatus(LoanStatus.OVERDUE);
        loan.setOverdueNotificationCount(loan.getOverdueNotificationCount() + 1);

        log.info("Loan marked as overdue | loanId={} | userId={} | bookId={} | overdueSince={}",
                loan.getId(), loan.getUser().getId(),
                loan.getBook().getId(), loan.getOverdueSince());
    }

    private Loan findLoanOrThrow(Long loanId) {
        return repository.findById(loanId)
                .orElseThrow(() -> {
                    log.warn("Loan not found | loanId={}", loanId);
                    return new ResourceNotFoundException("Resource not found");
                });
    }
}