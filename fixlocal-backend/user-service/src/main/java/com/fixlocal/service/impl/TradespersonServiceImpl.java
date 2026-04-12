package com.fixlocal.service.impl;

import com.fixlocal.service.TradespersonService;
import com.fixlocal.dto.ServiceOfferingDTO;
import com.fixlocal.dto.TradespersonDTO;
import com.fixlocal.exception.UserException;
import com.fixlocal.exception.ErrorCode;
import com.fixlocal.entity.*;
import com.fixlocal.enums.*;
import com.fixlocal.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradespersonServiceImpl implements TradespersonService {

    private final UserRepository userRepository;

    // ======================================================
    // SEARCH TRADESPERSONS
    // ======================================================

    public Page<TradespersonDTO> searchTradespersons(
            String city,
            String occupation,
            Double minRating,
            String tag,
            Double latitude,
            Double longitude,
            Double radiusKm,
            int page,
            int size
    ) {

        if (city == null || city.isBlank()) {
            throw new UserException(ErrorCode.CITY_REQUIRED);
        }

        city = city.trim();
        if (occupation != null) {
            occupation = occupation.trim();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<User> users;

        // ⭐ SEARCH WITH OCCUPATION
        if (occupation != null && !occupation.isBlank()) {

            users = userRepository
                    .findByRoleAndWorkingCityIgnoreCaseAndOccupationIgnoreCaseAndStatusAndVerifiedTrueAndBlockedFalse(
                            Role.TRADESPERSON,
                            city,
                            occupation,
                            Status.AVAILABLE,
                            pageable
                    );

        }

        // ⭐ SEARCH WITHOUT OCCUPATION
        else {

            users = userRepository
                    .findByRoleAndWorkingCityIgnoreCaseAndStatusAndVerifiedTrueAndBlockedFalse(
                            Role.TRADESPERSON,
                            city,
                            Status.AVAILABLE,
                            pageable
                    );
        }

        List<TradespersonDTO> dtoList = users.getContent()
                .stream()
                .filter(user ->
                        (minRating == null || user.getAverageRating() >= minRating) &&
                                (tag == null || (user.getSkillTags() != null && user.getSkillTags().contains(tag))) &&
                                withinRadius(user, latitude, longitude, radiusKm)
                )
                .map(user -> mapToDTOWithDistance(user, latitude, longitude))
                .sorted((a, b) -> Double.compare(
                        a.getDistanceKm() == null ? Double.MAX_VALUE : a.getDistanceKm(),
                        b.getDistanceKm() == null ? Double.MAX_VALUE : b.getDistanceKm()
                ))
                .toList();

        return new PageImpl<>(dtoList, pageable, users.getTotalElements());
    }

    // ======================================================
    // GET SINGLE TRADESPERSON
    // ======================================================

    public TradespersonDTO getTradespersonById(String id) {

        User tradesperson = userRepository.findById(id)
                .orElseThrow(() -> new UserException(ErrorCode.TRADESPERSON_NOT_FOUND));

        if (tradesperson.getRole() != Role.TRADESPERSON) {
            throw new UserException(ErrorCode.TARGET_NOT_TRADESPERSON);
        }

        return mapToDTO(tradesperson);
    }

    // ======================================================
    // DTO MAPPER
    // ======================================================

    private TradespersonDTO mapToDTO(User user) {

        TradespersonDTO dto = new TradespersonDTO();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setOccupation(user.getOccupation());
        dto.setWorkingCity(user.getWorkingCity());
        dto.setExperience(user.getExperience());

        dto.setAverageRating(user.getAverageRating());
        dto.setTotalReviews(user.getTotalReviews());

        dto.setVerified(user.isVerified());
        dto.setStatus(user.getStatus());
        dto.setAvailable(user.isAvailable());

        // ⭐ Safe UI fields
        dto.setProfileImage(user.getProfileImage());
        dto.setBio(user.getBio());
        dto.setCompletedJobs(user.getCompletedJobs());

        dto.setLastKnownLatitude(user.getLastKnownLatitude());
        dto.setLastKnownLongitude(user.getLastKnownLongitude());

        dto.setSkillTags(user.getSkillTags());
        List<ServiceOffering> offerings = user.getServiceOfferings() == null
                ? Collections.emptyList()
                : user.getServiceOfferings();
        dto.setServiceOfferings(
                offerings.stream()
                        .map(offering -> this.mapServiceOffering(offering))
                        .collect(Collectors.toList())
        );

        return dto;
    }

    private ServiceOfferingDTO mapServiceOffering(ServiceOffering offering) {

        ServiceOfferingDTO dto = new ServiceOfferingDTO();
        dto.setId(offering.getId());
        dto.setName(offering.getName());
        dto.setDescription(offering.getDescription());
        dto.setBasePrice(offering.getBasePrice());
        dto.setDurationMinutes(offering.getDurationMinutes());

        return dto;
    }

    private TradespersonDTO mapToDTOWithDistance(User user,
                                                 Double latitude,
                                                 Double longitude) {

        TradespersonDTO dto = mapToDTO(user);
        if (latitude != null && longitude != null && user.getLastKnownLatitude() != null && user.getLastKnownLongitude() != null) {
            dto.setDistanceKm(haversine(latitude, longitude,
                    user.getLastKnownLatitude(), user.getLastKnownLongitude()));
        }
        return dto;
    }

    private boolean withinRadius(User user,
                                 Double latitude,
                                 Double longitude,
                                 Double radiusKm) {

        if (latitude == null || longitude == null || radiusKm == null) {
            return true;
        }

        if (user.getLastKnownLatitude() == null || user.getLastKnownLongitude() == null) {
            return false;
        }

        double distance = haversine(latitude, longitude,
                user.getLastKnownLatitude(), user.getLastKnownLongitude());
        return distance <= radiusKm;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {

        double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }
}