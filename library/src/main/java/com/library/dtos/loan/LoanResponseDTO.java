package com.library.dtos.loan;

import com.library.models.entities.Loan;
import com.library.models.entities.enums.LoanStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class LoanResponseDTO {

    @NotNull(message = "Loan id is required")
    private Long id;

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "User name is required")
    @Size(min = 1, max = 120, message = "User name must be between 1 and 120 characters")
    private String userName;

    @NotNull(message = "Book id is required")
    private Long bookId;

    @NotNull(message = "Book title is required")
    @Size(min = 1, max = 200, message = "Book title must be between 1 and 200 characters")
    private String bookTitle;

    @NotNull(message = "Borrowed date is required")
    private LocalDate borrowedAt;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private LocalDate returnedAt;

    @NotNull(message = "Loan status is required")
    private LoanStatus status;

    @Min(value = 0, message = "Days overdue cannot be negative")
    private Long daysOverdue;

    private Integer overdueNotificationCount;

    private BigDecimal fineAmount;

    private LocalDate fineCalculatedAt;

    public LoanResponseDTO() {
    }

    public LoanResponseDTO(Loan entity) {
        id = entity.getId();
        userId = entity.getUser().getId();
        userName = entity.getUser().getName();
        bookId = entity.getBook().getId();
        bookTitle = entity.getBook().getTitle();
        borrowedAt = entity.getBorrowedAt();
        dueDate = entity.getDueDate();
        returnedAt = entity.getReturnedAt();
        status = entity.getStatus();
        daysOverdue = entity.getDaysOverdue();
        overdueNotificationCount = entity.getOverdueNotificationCount();

        fineAmount = entity.getFineAmount();
        fineCalculatedAt = entity.getFineCalculatedAt();
    }
}