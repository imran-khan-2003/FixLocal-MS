package com.fixlocal.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fixlocal.dto.TestimonialRequest;
import com.fixlocal.model.Testimonial;
import com.fixlocal.repository.TestimonialRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;

    public List<Testimonial> findLatest(int limit) {
        return testimonialRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }

    public Testimonial addTestimonial(TestimonialRequest request) {
        Testimonial testimonial = Testimonial.create(
                request.getName(),
                request.getCity(),
                request.getRole(),
                request.getQuote()
        );
        return testimonialRepository.save(testimonial);
    }
}