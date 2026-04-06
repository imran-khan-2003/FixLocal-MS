package com.fixlocal.service;

import com.fixlocal.dto.ServiceOfferingDTO;
import com.fixlocal.dto.ServiceOfferingRequest;
import com.fixlocal.dto.UpdateUserRequest;
import com.fixlocal.dto.UserResponseDTO;
import com.fixlocal.exception.ResourceNotFoundException;
import com.fixlocal.exception.UnauthorizedException;
import com.fixlocal.model.Role;
import com.fixlocal.model.ServiceOffering;
import com.fixlocal.model.Status;
import com.fixlocal.model.User;
import com.fixlocal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDTO getMyProfile(String email) {
        User user = findByEmailOrThrow(email);

        return mapToDTO(user);
    }

    public UserResponseDTO updateMyProfile(String email, UpdateUserRequest request) {

        User user = findByEmailOrThrow(email);

        user.setName(request.getName());
        user.setWorkingCity(request.getWorkingCity());
        user.setBio(request.getBio());
        user.setPhone(request.getPhone());

        if (request.getSkillTags() != null && user.getRole() == Role.TRADESPERSON) {
            user.setSkillTags(new ArrayList<>(sanitizeSkillTags(request.getSkillTags())));
        }

        userRepository.save(user);

        return mapToDTO(user);
    }

    public UserResponseDTO toggleAvailability(String email, boolean available) {

        User user = findByEmailOrThrow(email);

        user.setAvailable(available);
        user.setStatus(available ? Status.AVAILABLE : Status.OFFLINE);

        userRepository.save(user);

        return mapToDTO(user);
    }

    public UserResponseDTO updateSkillTags(String email, List<String> tags) {

        User user = findByEmailOrThrow(email);
        ensureTradesperson(user);

        List<String> sanitized = sanitizeSkillTags(tags);
        user.setSkillTags(new ArrayList<>(sanitized));

        userRepository.save(user);
        return mapToDTO(user);
    }

    public UserResponseDTO addServiceOffering(String email, ServiceOfferingRequest request) {

        User user = findByEmailOrThrow(email);
        ensureTradesperson(user);

        ServiceOffering offering = ServiceOffering.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName().trim())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .durationMinutes(request.getDurationMinutes())
                .build();

        ensureServiceOfferings(user).add(offering);

        userRepository.save(user);
        return mapToDTO(user);
    }

    public UserResponseDTO updateServiceOffering(String email,
                                                 String serviceId,
                                                 ServiceOfferingRequest request) {

        User user = findByEmailOrThrow(email);
        ensureTradesperson(user);

        ServiceOffering offering = ensureServiceOfferings(user).stream()
                .filter(o -> o.getId().equals(serviceId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Service offering not found"));

        offering.setName(request.getName().trim());
        offering.setDescription(request.getDescription());
        offering.setBasePrice(request.getBasePrice());
        offering.setDurationMinutes(request.getDurationMinutes());

        userRepository.save(user);
        return mapToDTO(user);
    }

    public UserResponseDTO deleteServiceOffering(String email, String serviceId) {

        User user = findByEmailOrThrow(email);
        ensureTradesperson(user);

        boolean removed = ensureServiceOfferings(user)
                .removeIf(offering -> offering.getId().equals(serviceId));

        if (!removed) {
            throw new ResourceNotFoundException("Service offering not found");
        }

        userRepository.save(user);
        return mapToDTO(user);
    }

    @Transactional
    public void deleteMyAccount(String email) {
        User user = findByEmailOrThrow(email);
        userRepository.deleteById(user.getId());
        log.info("Deleted account for user {}", user.getId());
    }

    public UserResponseDTO mapToDTO(User user) {

        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        dto.setOccupation(user.getOccupation());
        dto.setWorkingCity(user.getWorkingCity());
        dto.setExperience(user.getExperience());

        dto.setAverageRating(user.getAverageRating());
        dto.setTotalReviews(user.getTotalReviews());

        dto.setStatus(user.getStatus());
        dto.setVerified(user.isVerified());
        dto.setAvailable(user.isAvailable());
        dto.setCurrentBookingId(user.getCurrentBookingId());

        dto.setProfileImage(user.getProfileImage());
        dto.setBio(user.getBio());
        dto.setPhone(user.getPhone());
        dto.setCompletedJobs(user.getCompletedJobs());
        dto.setLastKnownLatitude(user.getLastKnownLatitude());
        dto.setLastKnownLongitude(user.getLastKnownLongitude());

        List<String> skillTags = user.getSkillTags() == null
                ? Collections.emptyList()
                : user.getSkillTags();
        dto.setSkillTags(skillTags);

        List<ServiceOffering> offerings = user.getServiceOfferings() == null
                ? Collections.emptyList()
                : user.getServiceOfferings();
        dto.setServiceOfferings(
                offerings.stream()
                        .map(this::mapServiceOffering)
                        .toList()
        );

        return dto;
    }

    private ServiceOfferingDTO mapServiceOffering(com.fixlocal.model.ServiceOffering offering) {

        ServiceOfferingDTO dto = new ServiceOfferingDTO();
        dto.setId(offering.getId());
        dto.setName(offering.getName());
        dto.setDescription(offering.getDescription());
        dto.setBasePrice(offering.getBasePrice());
        dto.setDurationMinutes(offering.getDurationMinutes());

        return dto;
    }

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void ensureTradesperson(User user) {
        if (user.getRole() != Role.TRADESPERSON) {
            throw new UnauthorizedException("Only tradespersons can manage service offerings and skill tags");
        }
    }

    private List<String> sanitizeSkillTags(List<String> tags) {

        if (tags == null) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> deduped = tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .map(tag -> tag.length() > 50 ? tag.substring(0, 50) : tag)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return deduped.stream()
                .limit(15)
                .toList();
    }

    private List<ServiceOffering> ensureServiceOfferings(User user) {
        if (user.getServiceOfferings() == null) {
            user.setServiceOfferings(new ArrayList<>());
        }
        return user.getServiceOfferings();
    }
}