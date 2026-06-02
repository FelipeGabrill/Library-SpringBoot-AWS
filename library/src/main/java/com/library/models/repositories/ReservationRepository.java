package com.library.models.repositories;

import com.library.models.entities.Reservation;
import com.library.models.entities.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findByBookIdAndStatus(
            Long bookId,
            ReservationStatus status,
            Pageable pageable);

    List<Reservation> findByBookIdAndStatus(
            Long bookId,
            ReservationStatus status);

    boolean existsByUserIdAndBookIdAndStatus(
            Long userId,
            Long bookId,
            ReservationStatus status);
}