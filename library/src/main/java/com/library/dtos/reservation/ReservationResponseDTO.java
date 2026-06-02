package com.library.dtos.reservation;

import com.library.models.entities.Reservation;
import com.library.models.entities.enums.ReservationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationResponseDTO {

    private Long id;
    private Long userId;
    private String userName;
    private Long bookId;
    private String bookTitle;
    private LocalDateTime reservedAt;
    private ReservationStatus status;

    public ReservationResponseDTO() {
    }

    public ReservationResponseDTO(Reservation entity) {
        id = entity.getId();
        userId = entity.getUser().getId();
        userName = entity.getUser().getName();
        bookId = entity.getBook().getId();
        bookTitle = entity.getBook().getTitle();
        reservedAt = entity.getReservedAt();
        status = entity.getStatus();
    }
}
