package com.fixlocal.service;

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

public interface DisputeService {
    public Dispute createDispute(DisputeRequest request, Authentication authentication);
    public List<DisputeDetailsDTO> getAllDisputesWithDetails();
    public DisputeDetailsDTO getDisputeDetails(String id, Authentication authentication);
    public List<Dispute> getDisputesByBookingId(String bookingId);
    public List<DisputeDetailsDTO> getDisputesForReporter(Authentication authentication);
    public DisputeDetailsDTO updateDispute(String id, Dispute updatedDispute, Authentication authentication);
    public Dispute addMessage(String disputeId, Authentication authentication, DisputeMessageRequest request);
}
