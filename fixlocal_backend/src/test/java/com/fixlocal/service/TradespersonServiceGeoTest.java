package com.fixlocal.service;

import com.fixlocal.dto.TradespersonDTO;
import com.fixlocal.exception.BadRequestException;
import com.fixlocal.model.Role;
import com.fixlocal.model.Status;
import com.fixlocal.model.User;
import com.fixlocal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradespersonServiceGeoTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TradespersonService tradespersonService;

    @Test
    void searchTradespersons_filtersByTagAndRadiusAndSortsByDistance() {

        User near = baseTradesperson("tp-near", 12.0, 77.0,
                List.of("plumbing", "water"), 4.9);
        User far = baseTradesperson("tp-far", 13.0, 78.0,
                List.of("plumbing"), 4.5);

        Page<User> page = new PageImpl<>(List.of(far, near), PageRequest.of(0, 10), 2);

        when(userRepository
                .findByRoleAndWorkingCityIgnoreCaseAndStatusAndVerifiedTrueAndBlockedFalse(
                        eq(Role.TRADESPERSON), eq("Bangalore"), eq(Status.AVAILABLE), any()))
                .thenReturn(page);

        Page<TradespersonDTO> result = tradespersonService.searchTradespersons(
                "Bangalore",
                null,
                4.0,
                "water",
                12.01,
                77.01,
                5.0,
                0,
                10
        );

        assertThat(result.getContent()).hasSize(1);
        TradespersonDTO dto = result.getContent().get(0);
        assertThat(dto.getId()).isEqualTo("tp-near");
        assertThat(dto.getDistanceKm()).isNotNull().isLessThan(5.0);
    }

    @Test
    void searchTradespersons_requiresCity() {
        assertThrows(BadRequestException.class,
                () -> tradespersonService.searchTradespersons(
                        " ", null, null,
                        null, null, null, null,
                        0, 10));
    }

    private User baseTradesperson(String id,
                                  Double lat,
                                  Double lon,
                                  List<String> tags,
                                  double rating) {

        return User.builder()
                .id(id)
                .name(id)
                .email(id + "@mail.com")
                .role(Role.TRADESPERSON)
                .status(Status.AVAILABLE)
                .workingCity("Bangalore")
                .verified(true)
                .skillTags(tags)
                .averageRating(rating)
                .lastKnownLatitude(lat)
                .lastKnownLongitude(lon)
                .build();
    }
}
