package com.fixlocal.service;

import com.fixlocal.dto.TradespersonDashboardDTO;
import com.fixlocal.dto.UserDashboardDTO;
import com.fixlocal.dto.UserResponseDTO;
import com.fixlocal.exception.ResourceNotFoundException;
import com.fixlocal.model.Booking;
import com.fixlocal.model.BookingStatus;
import com.fixlocal.model.Role;
import com.fixlocal.model.User;
import com.fixlocal.repository.BookingRepository;
import com.fixlocal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final MongoTemplate mongoTemplate;
    private final UserService userService;

    public UserDashboardDTO getUserDashboard(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.USER) {
            throw new ResourceNotFoundException("Dashboard only available for users");
        }

        Map<BookingStatus, Long> stats = aggregateStats("userId", user.getId());

        long upcoming = stats.getOrDefault(BookingStatus.PENDING, 0L);
        long active = stats.getOrDefault(BookingStatus.ACCEPTED, 0L) +
                stats.getOrDefault(BookingStatus.EN_ROUTE, 0L) +
                stats.getOrDefault(BookingStatus.ARRIVED, 0L);
        long completed = stats.getOrDefault(BookingStatus.COMPLETED, 0L);
        long total = stats.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        UserResponseDTO profile = userService.mapToDTO(user);

        return UserDashboardDTO.builder()
                .profile(profile)
                .upcomingBookings(upcoming)
                .activeBookings(active)
                .completedBookings(completed)
                .totalBookings(total)
                .bookingBreakdown(stats)
                .build();
    }

    public TradespersonDashboardDTO getTradespersonDashboard(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.TRADESPERSON) {
            throw new ResourceNotFoundException("Dashboard only available for tradespersons");
        }

        Map<BookingStatus, Long> stats = aggregateStats("tradespersonId", user.getId());

        long pending = stats.getOrDefault(BookingStatus.PENDING, 0L);
        long active = stats.getOrDefault(BookingStatus.ACCEPTED, 0L) +
                stats.getOrDefault(BookingStatus.EN_ROUTE, 0L) +
                stats.getOrDefault(BookingStatus.ARRIVED, 0L);
        long completed = stats.getOrDefault(BookingStatus.COMPLETED, 0L);
        long total = stats.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        UserResponseDTO profile = userService.mapToDTO(user);

        return TradespersonDashboardDTO.builder()
                .profile(profile)
                .pendingRequests(pending)
                .activeBookings(active)
                .completedBookings(completed)
                .totalBookings(total)
                .averageRating(user.getAverageRating() == null ? 0 : user.getAverageRating())
                .totalReviews(user.getTotalReviews() == null ? 0 : user.getTotalReviews())
                .bookingBreakdown(stats)
                .build();
    }

    private Map<BookingStatus, Long> aggregateStats(String field, String id) {

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(field).is(id)),
                Aggregation.group("status").count().as("count")
        );

        AggregationResults<org.bson.Document> results =
                mongoTemplate.aggregate(aggregation, Booking.class, org.bson.Document.class);

        Map<BookingStatus, Long> map = new EnumMap<>(BookingStatus.class);

        for (org.bson.Document doc : results.getMappedResults()) {
            BookingStatus status = BookingStatus.valueOf(doc.getString("_id"));
            map.put(status, ((Number) doc.get("count")).longValue());
        }

        return map;
    }
}
