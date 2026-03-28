package com.fixlocal.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fixlocal.model.Dispute;

public interface DisputeRepository extends MongoRepository<Dispute, String> {
    List<Dispute> findByBookingId(String bookingId);
    List<Dispute> findByReporterId(String reporterId);
}
