package com.library.dtos.loan;

import com.library.models.entities.Loan;
import com.library.models.entities.enums.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Loan data returned by the API")
public class LoanResponseDTO {

    @NotNull(message = "Loan id is required")
    @Schema(description = "Unique identifier of the loan", example = "1")
    private Long id;

    @NotNull(message = "User id is required")
    @Schema(description = "Identifier of the user who borrowed the book", example = "2")
    private Long userId;

    @NotNull(message = "User name is required")
    @Size(min = 1, max = 120, message = "User name must be between 1 and 120 characters")
    @Schema(description = "Name of the user who borrowed the book", example = "Felipe Gabriel")
    private String userName;

    @NotNull(message = "Book id is required")
    @Schema(description = "Identifier of the borrowed book", example = "1")
    private Long bookId;

    @NotNull(message = "Book title is required")
    @Size(min = 1, max = 200, message = "Book title must be between 1 and 200 characters")
    @Schema(description = "Title of the borrowed book", example = "Clean Code")
    private String bookTitle;

    @NotNull(message = "Borrowed date is required")
    @Schema(description = "Date the book was borrowed", example = "2026-06-01")
    private LocalDate borrowedAt;

    @NotNull(message = "Due date is required")
    @Schema(description = "Date the book must be returned", example = "2026-06-08")
    private LocalDate dueDate;

    @Schema(description = "Date the book was actually returned (null if still active)",
            example = "2026-06-05")
    private LocalDate returnedAt;

    @NotNull(message = "Loan status is required")
    @Schema(description = "Current status of the loan", example = "ACTIVE")
    private LoanStatus status;

    @Min(value = 0, message = "Days overdue cannot be negative")
    @Schema(description = "Number of days the loan is overdue", example = "0")
    private Long daysOverdue;

    @Schema(description = "How many overdue notifications were sent", example = "0")
    private Integer overdueNotificationCount;

    @Schema(description = "Accumulated fine amount", example = "0.00")
    private BigDecimal fineAmount;

    @Schema(description = "Date the fine was last calculated", example = "2026-06-09")
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