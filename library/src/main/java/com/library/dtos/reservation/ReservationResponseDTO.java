package com.library.dtos.reservation;

import com.library.models.entities.Reservation;
import com.library.models.entities.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Reservation data returned by the API")
public class ReservationResponseDTO {

    @Schema(description = "Unique identifier of the reservation", example = "1")
    private Long id;

    @Schema(description = "Identifier of the user who made the reservation", example = "2")
    private Long userId;

    @Schema(description = "Name of the user who made the reservation", example = "Felipe Gabriel")
    private String userName;

    @Schema(description = "Identifier of the reserved book", example = "1")
    private Long bookId;

    @Schema(description = "Title of the reserved book", example = "Clean Code")
    private String bookTitle;

    @Schema(description = "Date and time the reservation was created", example = "2026-06-01T14:30:00")
    private LocalDateTime reservedAt;

    @Schema(description = "Current status of the reservation (ACTIVE, NOTIFIED, CANCELLED)", example = "ACTIVE")
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