package com.fixlocal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fixlocal.model.Testimonial;
import com.fixlocal.dto.TestimonialRequest;
import com.fixlocal.service.TestimonialService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/testimonials")
@RequiredArgsConstructor
public class TestimonialController {

    private final TestimonialService testimonialService;

    @GetMapping
    public ResponseEntity<List<Testimonial>> getTestimonials(
            @RequestParam(defaultValue = "6") int limit
    ) {
        return ResponseEntity.ok(testimonialService.findLatest(limit));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'TRADESPERSON', 'ADMIN')")
    public ResponseEntity<Testimonial> addTestimonial(
            @Valid @RequestBody TestimonialRequest request
    ) {
        return ResponseEntity.ok(testimonialService.addTestimonial(request));
    }
}