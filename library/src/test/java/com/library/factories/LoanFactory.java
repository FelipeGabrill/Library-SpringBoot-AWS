package com.library.factories;

import com.library.models.entities.Book;
import com.library.models.entities.Loan;
import com.library.models.entities.User;
import com.library.models.entities.enums.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanFactory {

    public static Loan createLoan() {
        User user = UserFactory.createUser();
        Book book = BookFactory.createBook();
        return new Loan(
                1L,
                user,
                book,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                null,
                LoanStatus.ACTIVE,
                null,
                0,
                null,
                null
        );
    }

    public static Loan createActiveLoan(Long id, User user, Book book) {
        return new Loan(
                id,
                user,
                book,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                null,
                LoanStatus.ACTIVE,
                null,
                0,
                null,
                null
        );
    }

    public static Loan createOverdueLoan() {
        User user = UserFactory.createUser();
        Book book = BookFactory.createBook();
        return new Loan(
                2L,
                user,
                book,
                LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(7),
                null,
                LoanStatus.ACTIVE,
                LocalDate.now().minusDays(7),
                0,
                null,
                null
        );
    }

    public static Loan createReturnedLoan() {
        Loan loan = createLoan();
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnedAt(LocalDate.now());
        return loan;
    }
}
