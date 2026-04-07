package com.fixlocal.service.impl;

import com.fixlocal.service.ReviewService;
import com.fixlocal.dto.ReviewRequest;
import com.fixlocal.dto.ReviewSummaryDTO;
import com.fixlocal.entity.Review;
import com.fixlocal.repository.ReviewRepository;
import com.fixlocal.exception.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestTemplate restTemplate;

    @Value("${internal.user-service.base-url:http://localhost:8082}")
    private String userServiceBaseUrl;

    @Value("${internal.booking-service.base-url:http://localhost:8084}")
    private String bookingServiceBaseUrl;

    private Map<String, Object> getLoggedInUser(Authentication authentication) {

        String email = authentication.getName();

        Map<String, Object> user = restTemplate.getForObject(
                userServiceBaseUrl + "/internal/users/by-email?email={email}",
                Map.class,
                email
        );

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        if (getBoolean(user, "blocked")) {
            throw new UnauthorizedException("Your account is blocked");
        }

        return user;
    }

    @Transactional
    public Review addReview(String bookingId,
                            ReviewRequest request,
                            Authentication authentication) {

        Map<String, Object> user = getLoggedInUser(authentication);

        if (!"USER".equalsIgnoreCase(getString(user, "role"))) {
            throw new UnauthorizedException("Only users can add reviews");
        }

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        Map<String, Object> booking = restTemplate.getForObject(
                bookingServiceBaseUrl + "/internal/bookings/{bookingId}",
                Map.class,
                bookingId
        );

        if (booking == null) {
            throw new ResourceNotFoundException("Booking not found");
        }

        if (!Objects.equals(getString(booking, "userId"), getString(user, "id"))) {
            throw new UnauthorizedException("Booking does not belong to you");
        }

        if (!"COMPLETED".equalsIgnoreCase(getString(booking, "status"))) {
            throw new ConflictException("Review allowed only after completion");
        }

        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new ConflictException("Review already exists");
        }

        Review review = Review.builder()
                .bookingId(bookingId)
                .userId(getString(user, "id"))
                .tradespersonId(getString(booking, "tradespersonId"))
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);

        markBookingReviewed(getString(booking, "id"), saved);

        updateTradespersonRating(getString(booking, "tradespersonId"), request.getRating());

        log.info("Review added for booking {}", bookingId);

        return saved;
    }

    private void markBookingReviewed(String bookingId, Review review) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("rating", review.getRating());
        payload.put("reviewId", review.getId());

        restTemplate.exchange(
                bookingServiceBaseUrl + "/internal/bookings/{bookingId}/review",
                HttpMethod.PUT,
                new HttpEntity<>(payload),
                Void.class,
                bookingId
        );
    }

    private void updateTradespersonRating(String tradespersonId, int newRating) {
        restTemplate.exchange(
                userServiceBaseUrl + "/internal/users/{id}/ratings/{rating}",
                HttpMethod.PUT,
                null,
                Void.class,
                tradespersonId,
                newRating
        );
    }

    public List<ReviewSummaryDTO> getTradespersonReviews(String tradespersonId) {
        List<Review> reviews = reviewRepository.findByTradespersonId(tradespersonId);

        if (reviews.isEmpty()) {
            return List.of();
        }

        List<String> userIds = reviews.stream()
                .map(Review::getUserId)
                .distinct()
                .toList();

        Map<String, Map<String, Object>> userMap = userIds.stream()
                .map(id -> {
                    try {
                        return restTemplate.getForObject(
                                userServiceBaseUrl + "/internal/users/{id}",
                                Map.class,
                                id
                        );
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(u -> u != null && getString(u, "id") != null)
                .collect(Collectors.toMap(u -> getString(u, "id"), u -> u, (a, b) -> a));

        Map<String, Object> tradesperson = null;
        try {
            tradesperson = restTemplate.getForObject(
                    userServiceBaseUrl + "/internal/users/{id}",
                    Map.class,
                    tradespersonId
            );
        } catch (Exception ignored) {
        }

        final String tradespersonName = getString(tradesperson, "name");

        return reviews.stream()
                .map(review -> ReviewSummaryDTO.builder()
                        .id(review.getId())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .bookingId(review.getBookingId())
                        .userId(review.getUserId())
                        .userName(getString(userMap.get(review.getUserId()), "name"))
                        .tradespersonId(review.getTradespersonId())
                        .tradespersonName(tradespersonName)
                        .createdAt(review.getCreatedAt())
                        .build())
                .toList();
    }

    private String getString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private boolean getBoolean(Map<String, Object> map, String key) {
        if (map == null) return false;
        Object value = map.get(key);
        if (value instanceof Boolean b) return b;
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }
}