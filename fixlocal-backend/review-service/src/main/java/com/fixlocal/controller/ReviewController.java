package com.fixlocal.controller;

import com.fixlocal.dto.ReviewRequest;
import com.fixlocal.dto.ReviewSummaryDTO;
import com.fixlocal.entity.Review;
import com.fixlocal.service.ReviewService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{bookingId}")
    public ResponseEntity<Review> addReview(
            @PathVariable String bookingId,
            @RequestBody ReviewRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                reviewService.addReview(bookingId, request, authentication)
        );
    }

    @GetMapping("/tradesperson/{tradespersonId}")
    public ResponseEntity<List<ReviewSummaryDTO>> getTradespersonReviews(
            @PathVariable String tradespersonId
    ) {
        return ResponseEntity.ok(
                reviewService.getTradespersonReviews(tradespersonId)
        );
    }
}