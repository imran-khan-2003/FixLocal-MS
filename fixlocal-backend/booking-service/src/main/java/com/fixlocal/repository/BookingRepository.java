package com.fixlocal.repository;

import com.fixlocal.model.Booking;
import com.fixlocal.model.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {

    Page<Booking> findAll(Pageable pageable);

    Page<Booking> findByUserId(String userId, Pageable pageable);

    Page<Booking> findByTradespersonId(String tradespersonId, Pageable pageable);

    long countByUserId(String userId);

    long countByTradespersonId(String tradespersonId);

    Page<Booking> findByUserIdAndStatus(
            String userId,
            BookingStatus status,
            Pageable pageable
    );

    Page<Booking> findByTradespersonIdAndStatus(
            String tradespersonId,
            BookingStatus status,
            Pageable pageable
    );

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    Optional<Booking> findByTradespersonIdAndStatus(
            String tradespersonId,
            BookingStatus status
    );

    boolean existsByUserIdAndTradespersonIdAndStatus(
            String userId,
            String tradespersonId,
            BookingStatus status
    );

    long countByStatus(BookingStatus status);

    long countByUserIdAndStatus(String userId, BookingStatus status);

    long countByTradespersonIdAndStatus(String tradespersonId, BookingStatus status);
}