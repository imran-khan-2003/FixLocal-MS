package com.fixlocal.service;

import com.fixlocal.dto.ReviewRequest;
import com.fixlocal.dto.ReviewSummaryDTO;
import com.fixlocal.model.*;
import com.fixlocal.repository.*;
import com.fixlocal.exception.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    private User getLoggedInUser(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isBlocked()) {
            throw new UnauthorizedException("Your account is blocked");
        }

        return user;
    }

    @Transactional
    public Review addReview(String bookingId,
                            ReviewRequest request,
                            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        if (user.getRole() != Role.USER) {
            throw new UnauthorizedException("Only users can add reviews");
        }

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUserId().equals(user.getId())) {
            throw new UnauthorizedException("Booking does not belong to you");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ConflictException("Review allowed only after completion");
        }

        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new ConflictException("Review already exists");
        }

        Review review = Review.builder()
                .bookingId(bookingId)
                .userId(user.getId())
                .tradespersonId(booking.getTradespersonId())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);

        markBookingReviewed(booking, saved);

        updateTradespersonRating(booking.getTradespersonId(), request.getRating());

        log.info("Review added for booking {}", bookingId);

        return saved;
    }

    private void markBookingReviewed(Booking booking, Review review) {

        booking.setReviewSubmitted(true);
        booking.setUserRating(review.getRating());
        booking.setReviewedAt(review.getCreatedAt());
        booking.setReviewId(review.getId());

        bookingRepository.save(booking);
    }

    private void updateTradespersonRating(String tradespersonId, int newRating) {

        User tradesperson = userRepository.findById(tradespersonId)
                .orElseThrow(() -> new ResourceNotFoundException("Tradesperson not found"));

        int totalReviews = tradesperson.getTotalReviews() == null ? 0 : tradesperson.getTotalReviews();
        double currentAvg = tradesperson.getAverageRating() == null ? 0 : tradesperson.getAverageRating();

        double newAvg = ((currentAvg * totalReviews) + newRating) / (totalReviews + 1);

        tradesperson.setTotalReviews(totalReviews + 1);
        tradesperson.setAverageRating(newAvg);

        userRepository.save(tradesperson);
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

        List<User> users = userRepository.findAllById(userIds);
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        User tradesperson = userRepository.findById(tradespersonId)
                .orElse(null);

        return reviews.stream()
                .map(review -> ReviewSummaryDTO.builder()
                        .id(review.getId())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .bookingId(review.getBookingId())
                        .userId(review.getUserId())
                        .userName(userMap.getOrDefault(review.getUserId(), new User()).getName())
                        .tradespersonId(review.getTradespersonId())
                        .tradespersonName(tradesperson != null ? tradesperson.getName() : null)
                        .createdAt(review.getCreatedAt())
                        .build())
                .toList();
    }
}