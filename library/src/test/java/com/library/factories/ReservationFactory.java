package com.library.factories;

import com.library.models.entities.Book;
import com.library.models.entities.Reservation;
import com.library.models.entities.User;
import com.library.models.entities.enums.ReservationStatus;

import java.time.LocalDateTime;

public class ReservationFactory {

    public static Reservation createReservation() {
        User user = UserFactory.createUser();
        Book book = BookFactory.createUnavailableBook();
        return new Reservation(
                1L,
                user,
                book,
                LocalDateTime.now(),
                ReservationStatus.ACTIVE
        );
    }

    public static Reservation createReservation(Long id, User user, Book book, ReservationStatus status) {
        return new Reservation(
                id,
                user,
                book,
                LocalDateTime.now(),
                status
        );
    }

    public static Reservation createNotifiedReservation() {
        Reservation reservation = createReservation();
        reservation.setStatus(ReservationStatus.NOTIFIED);
        return reservation;
    }

    public static Reservation createCancelledReservation() {
        Reservation reservation = createReservation();
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservation;
    }
}

