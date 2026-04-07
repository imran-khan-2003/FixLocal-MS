package com.fixlocal.repository;

import com.fixlocal.entity.LiveLocation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LiveLocationRepository extends MongoRepository<LiveLocation, String> {

    Optional<LiveLocation> findByBookingId(String bookingId);

    void deleteByBookingId(String bookingId);
}