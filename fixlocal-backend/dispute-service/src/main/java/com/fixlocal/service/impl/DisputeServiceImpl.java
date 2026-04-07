
package com.fixlocal.service.impl;

import com.fixlocal.service.DisputeService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fixlocal.dto.DisputeDetailsDTO;
import com.fixlocal.dto.DisputeMessageRequest;
import com.fixlocal.dto.DisputeRequest;
import com.fixlocal.exception.ResourceNotFoundException;
import com.fixlocal.exception.UnauthorizedException;
import com.fixlocal.entity.Dispute;
import com.fixlocal.repository.DisputeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final RestTemplate restTemplate;

    @Value("${internal.user-service.base-url:http://localhost:8082}")
    private String userServiceBaseUrl;

    @Value("${internal.booking-service.base-url:http://localhost:8084}")
    private String bookingServiceBaseUrl;

    public Dispute createDispute(DisputeRequest request, Authentication authentication) {
        Map<String, Object> reporter;
        if (request.getReporterId() != null) {
            reporter = getUserById(request.getReporterId());
            if (reporter == null) {
                throw new ResourceNotFoundException("Reporter not found");
            }
        } else {
            reporter = getAuthenticatedUser(authentication);
        }

        Map<String, Object> booking = getBookingById(request.getBookingId());
        if (booking == null) {
            throw new ResourceNotFoundException("Booking not found");
        }

        Dispute dispute = Dispute.builder()
                .bookingId(request.getBookingId())
                .reporterId(getString(reporter, "id"))
                .reason(request.getReason())
                .desiredOutcome(request.getDesiredOutcome())
                .build();

        return disputeRepository.save(dispute);
    }

    public List<DisputeDetailsDTO> getAllDisputesWithDetails() {
        Map<String, Map<String, Object>> userCache = new HashMap<>();
        Map<String, Map<String, Object>> bookingCache = new HashMap<>();

        return disputeRepository.findAll()
                .stream()
                .map(dispute -> mapToDetails(dispute, userCache, bookingCache))
                .collect(Collectors.toList());
    }

    public DisputeDetailsDTO getDisputeDetails(String id, Authentication authentication) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));

        Map<String, Object> requester = getAuthenticatedUser(authentication);

        if (!isAdmin(requester) && !isParticipant(getString(requester, "id"), dispute)) {
            throw new UnauthorizedException("Not authorized to view this dispute");
        }

        return mapToDetails(dispute, new HashMap<>(), new HashMap<>());
    }

    public List<Dispute> getDisputesByBookingId(String bookingId) {
        return disputeRepository.findByBookingId(bookingId);
    }

    public List<DisputeDetailsDTO> getDisputesForReporter(Authentication authentication) {
        Map<String, Object> reporter = getAuthenticatedUser(authentication);

        Map<String, Map<String, Object>> userCache = new HashMap<>();
        Map<String, Map<String, Object>> bookingCache = new HashMap<>();

        return disputeRepository.findByReporterId(getString(reporter, "id"))
                .stream()
                .map(dispute -> mapToDetails(dispute, userCache, bookingCache))
                .collect(Collectors.toList());
    }

    public DisputeDetailsDTO updateDispute(String id, Dispute updatedDispute, Authentication authentication) {
        Dispute existingDispute = disputeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));

        Map<String, Object> requester = getAuthenticatedUser(authentication);

        if (!isAdmin(requester) && !Objects.equals(getString(requester, "id"), existingDispute.getReporterId())) {
            throw new UnauthorizedException("Only admins or the reporter can update disputes");
        }

        if (updatedDispute.getStatus() != null) {
            existingDispute.setStatus(updatedDispute.getStatus());
        }

        if (updatedDispute.getDesiredOutcome() != null) {
            existingDispute.setDesiredOutcome(updatedDispute.getDesiredOutcome());
        }

        Dispute saved = disputeRepository.save(existingDispute);
        return mapToDetails(saved, new HashMap<>(), new HashMap<>());
    }

    public Dispute addMessage(String disputeId,
                              Authentication authentication,
                              DisputeMessageRequest request) {

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));

        Map<String, Object> sender = getAuthenticatedUser(authentication);

        if (!isAdmin(sender) && !isParticipant(getString(sender, "id"), dispute)) {
            throw new UnauthorizedException("Not authorized to add message to this dispute");
        }

        if (dispute.getMessages() == null) {
            dispute.setMessages(new ArrayList<>());
        }

        dispute.getMessages().add(
                Dispute.DisputeMessage.builder()
                        .senderId(getString(sender, "id"))
                        .message(request.getMessage())
                        .build()
        );

        return disputeRepository.save(dispute);
    }

    private Map<String, Object> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            throw new UnauthorizedException("Authentication required");
        }

        String email = authentication.getName();
        return getUserByEmail(email);
    }

    private boolean isAdmin(Map<String, Object> user) {
        return user != null && "ADMIN".equalsIgnoreCase(getString(user, "role"));
    }

    private boolean isParticipant(String userId, Dispute dispute) {
        if (userId == null || dispute == null) {
            return false;
        }
        if (userId.equals(dispute.getReporterId())) {
            return true;
        }
        Map<String, Object> booking = getBookingById(dispute.getBookingId());
        return booking != null &&
                (Objects.equals(userId, getString(booking, "userId"))
                        || Objects.equals(userId, getString(booking, "tradespersonId")));
    }

    private DisputeDetailsDTO mapToDetails(Dispute dispute,
                                           Map<String, Map<String, Object>> userCache,
                                           Map<String, Map<String, Object>> bookingCache) {

        Map<String, Object> reporter = resolveUser(dispute.getReporterId(), userCache);
        Map<String, Object> booking = resolveBooking(dispute.getBookingId(), bookingCache);
        Map<String, Object> respondent = resolveRespondent(dispute, booking, reporter, userCache);

        List<DisputeDetailsDTO.MessageDTO> messageDTOS = buildMessageDTOs(dispute, userCache);

        return DisputeDetailsDTO.builder()
                .id(dispute.getId())
                .bookingId(dispute.getBookingId())
                .reason(dispute.getReason())
                .desiredOutcome(dispute.getDesiredOutcome())
                .status(dispute.getStatus())
                .createdAt(dispute.getCreatedAt())
                .reporter(toUserSummary(reporter))
                .respondent(toUserSummary(respondent))
                .booking(toBookingSummary(booking, userCache))
                .messages(messageDTOS)
                .build();
    }

    private List<DisputeDetailsDTO.MessageDTO> buildMessageDTOs(Dispute dispute,
                                                                Map<String, Map<String, Object>> userCache) {
        List<Dispute.DisputeMessage> sourceMessages = dispute.getMessages();
        if (sourceMessages == null || sourceMessages.isEmpty()) {
            return new ArrayList<>();
        }

        return sourceMessages.stream()
                .map(msg -> {
                    Map<String, Object> sender = resolveUser(msg.getSenderId(), userCache);
                    return DisputeDetailsDTO.MessageDTO.builder()
                            .senderId(msg.getSenderId())
                            .senderName(getString(sender, "name") != null ? getString(sender, "name") : "Unknown")
                            .senderRole(getString(sender, "role") != null
                                    ? getString(sender, "role")
                                    : null)
                            .message(msg.getMessage())
                            .timestamp(msg.getTimestamp())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> resolveBooking(String bookingId, Map<String, Map<String, Object>> cache) {
        if (bookingId == null) {
            return null;
        }
        if (cache.containsKey(bookingId)) {
            return cache.get(bookingId);
        }
        Map<String, Object> booking = getBookingById(bookingId);
        cache.put(bookingId, booking);
        return booking;
    }

    private Map<String, Object> resolveUser(String userId, Map<String, Map<String, Object>> cache) {
        if (userId == null) {
            return null;
        }
        if (cache.containsKey(userId)) {
            return cache.get(userId);
        }
        Map<String, Object> user = getUserById(userId);
        cache.put(userId, user);
        return user;
    }

    private Map<String, Object> resolveRespondent(Dispute dispute,
                                                  Map<String, Object> booking,
                                                  Map<String, Object> reporter,
                                                  Map<String, Map<String, Object>> cache) {
        if (booking == null) {
            return null;
        }

        String reporterId = dispute.getReporterId();
        if (reporter != null) {
            if (Objects.equals(getString(reporter, "id"), getString(booking, "userId"))) {
                return resolveUser(getString(booking, "tradespersonId"), cache);
            }
            if (Objects.equals(getString(reporter, "id"), getString(booking, "tradespersonId"))) {
                return resolveUser(getString(booking, "userId"), cache);
            }
        }

        if (reporterId != null && Objects.equals(reporterId, getString(booking, "userId"))) {
            return resolveUser(getString(booking, "tradespersonId"), cache);
        }

        if (reporterId != null && Objects.equals(reporterId, getString(booking, "tradespersonId"))) {
            return resolveUser(getString(booking, "userId"), cache);
        }

        return null;
    }

    private DisputeDetailsDTO.UserSummary toUserSummary(Map<String, Object> user) {
        if (user == null) {
            return null;
        }

        return DisputeDetailsDTO.UserSummary.builder()
                .id(getString(user, "id"))
                .name(getString(user, "name"))
                .email(getString(user, "email"))
                .phone(getString(user, "phone"))
                .role(getString(user, "role"))
                .build();
    }

    private DisputeDetailsDTO.BookingSummary toBookingSummary(Map<String, Object> booking,
                                                              Map<String, Map<String, Object>> userCache) {
        if (booking == null) {
            return null;
        }

        Map<String, Object> tradesperson = resolveUser(getString(booking, "tradespersonId"), userCache);
        Map<String, Object> user = resolveUser(getString(booking, "userId"), userCache);

        return DisputeDetailsDTO.BookingSummary.builder()
                .id(getString(booking, "id"))
                .status(getString(booking, "status"))
                .serviceDescription(getString(booking, "serviceDescription"))
                .serviceAddress(getString(booking, "serviceAddress"))
                .price(getDouble(booking, "price"))
                .userName(getString(user, "name") != null ? getString(user, "name") : getString(booking, "userName"))
                .userPhone(getString(user, "phone"))
                .tradespersonName(getString(tradesperson, "name"))
                .tradespersonPhone(getString(tradesperson, "phone"))
                .build();
    }

    private Map<String, Object> getUserByEmail(String email) {
        try {
            return restTemplate.getForObject(
                    userServiceBaseUrl + "/internal/users/by-email?email={email}",
                    Map.class,
                    email
            );
        } catch (Exception e) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private Map<String, Object> getUserById(String id) {
        try {
            return restTemplate.getForObject(
                    userServiceBaseUrl + "/internal/users/{id}",
                    Map.class,
                    id
            );
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> getBookingById(String id) {
        try {
            return restTemplate.getForObject(
                    bookingServiceBaseUrl + "/internal/bookings/{id}",
                    Map.class,
                    id
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Double getDouble(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) return null;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
