package com.fixlocal.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fixlocal.model.Testimonial;

public interface TestimonialRepository extends MongoRepository<Testimonial, String> {
}