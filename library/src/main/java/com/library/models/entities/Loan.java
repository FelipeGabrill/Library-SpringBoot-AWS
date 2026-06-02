package com.library.models.entities;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.library.models.entities.enums.LoanStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_loan")
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Getter
    @Setter
    @Column(name = "borrowed_at", nullable = false)
    private LocalDate borrowedAt;

    @Getter
    @Setter
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Getter
    @Setter
    @Column(name = "returned_at")
    private LocalDate returnedAt;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Getter
    @Setter
    @Column(name = "overdue_since")
    private LocalDate overdueSince;

    @Getter
    @Setter
    @Column(name = "overdue_notification_count")
    private Integer overdueNotificationCount = 0;

    @Column(name = "fine_amount")
    @Getter
    @Setter
    private BigDecimal fineAmount;

    @Column(name = "fine_calculated_at")
    @Getter
    @Setter
    private LocalDate fineCalculatedAt;

    public Loan() {
    }

    public boolean isOverdue() {
        return status == LoanStatus.ACTIVE && LocalDate.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        if (overdueSince == null) return 0;
        return ChronoUnit.DAYS.between(overdueSince, LocalDate.now());
    }
}