package com.fixlocal.repository;

import com.fixlocal.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {

    Optional<Review> findByBookingId(String bookingId);

    boolean existsByBookingId(String bookingId);

    List<Review> findByTradespersonId(String tradespersonId);

    long countByTradespersonId(String tradespersonId);
}