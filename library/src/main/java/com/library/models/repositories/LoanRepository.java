package com.library.models.repositories;

import com.library.models.entities.Loan;
import com.library.models.entities.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Page<Loan> findByUserId(Long userId, Pageable pageable);

    Page<Loan> findByUserIdAndStatus(Long userId, LoanStatus status, Pageable pageable);

    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    @Query("SELECT obj FROM Loan obj WHERE obj.status = 'ACTIVE' AND obj.dueDate < :today")
    List<Loan> findOverdueLoans(LocalDate today);

    @Query("SELECT obj FROM Loan obj WHERE obj.status = 'ACTIVE' AND obj.dueDate < :today")
    Page<Loan> findOverdueLoans(LocalDate today, Pageable pageable);

    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, LoanStatus status);

    long countByUserIdAndStatus(Long userId, LoanStatus status);

    long countByStatus(LoanStatus status);
}
