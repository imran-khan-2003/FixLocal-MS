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
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private UserService userService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getUserDashboardAggregatesStats() {

        User user = User.builder()
                .id("user-1")
                .email("user@mail.com")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("user@mail.com")).thenReturn(java.util.Optional.of(user));

        AggregationResults<Document> aggregationResults =
                new AggregationResults<>(
                        List.of(
                                new Document("_id", BookingStatus.PENDING.name()).append("count", 2L),
                                new Document("_id", BookingStatus.ACCEPTED.name()).append("count", 1L),
                                new Document("_id", BookingStatus.COMPLETED.name()).append("count", 3L)
                        ),
                        new Document()
                );

        when(mongoTemplate.aggregate(any(), eq(Booking.class), eq(Document.class)))
                .thenReturn(aggregationResults);

        UserResponseDTO responseDTO = new UserResponseDTO();
        when(userService.mapToDTO(user)).thenReturn(responseDTO);

        UserDashboardDTO dashboard = dashboardService.getUserDashboard("user@mail.com");

        assertThat(dashboard.getUpcomingBookings()).isEqualTo(2L);
        assertThat(dashboard.getActiveBookings()).isEqualTo(1L);
        assertThat(dashboard.getCompletedBookings()).isEqualTo(3L);
        assertThat(dashboard.getBookingBreakdown())
                .containsAllEntriesOf(Map.of(
                        BookingStatus.PENDING, 2L,
                        BookingStatus.ACCEPTED, 1L,
                        BookingStatus.COMPLETED, 3L
                ));
        assertThat(dashboard.getProfile()).isSameAs(responseDTO);
    }

    @Test
    void getTradespersonDashboardValidatesRole() {

        User user = User.builder()
                .id("user-2")
                .email("not-trades@mail.com")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("not-trades@mail.com"))
                .thenReturn(java.util.Optional.of(user));

        assertThrows(ResourceNotFoundException.class,
                () -> dashboardService.getTradespersonDashboard("not-trades@mail.com"));
    }

    @Test
    void getTradespersonDashboardAggregatesStats() {

        User tradesperson = User.builder()
                .id("tp-1")
                .email("tp@mail.com")
                .role(Role.TRADESPERSON)
                .build();

        when(userRepository.findByEmail("tp@mail.com"))
                .thenReturn(java.util.Optional.of(tradesperson));

        AggregationResults<Document> aggregationResults =
                new AggregationResults<>(
                        List.of(
                                new Document("_id", BookingStatus.PENDING.name()).append("count", 5L),
                                new Document("_id", BookingStatus.EN_ROUTE.name()).append("count", 1L),
                                new Document("_id", BookingStatus.COMPLETED.name()).append("count", 7L)
                        ),
                        new Document()
                );

        when(mongoTemplate.aggregate(any(), eq(Booking.class), eq(Document.class)))
                .thenReturn(aggregationResults);

        UserResponseDTO profile = new UserResponseDTO();
        when(userService.mapToDTO(tradesperson)).thenReturn(profile);

        TradespersonDashboardDTO dashboard = dashboardService.getTradespersonDashboard("tp@mail.com");

        assertThat(dashboard.getPendingRequests()).isEqualTo(5L);
        assertThat(dashboard.getActiveBookings()).isEqualTo(1L);
        assertThat(dashboard.getCompletedBookings()).isEqualTo(7L);
        assertThat(dashboard.getProfile()).isSameAs(profile);
    }
}
