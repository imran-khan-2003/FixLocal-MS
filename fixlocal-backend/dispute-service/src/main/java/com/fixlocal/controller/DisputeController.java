
package com.fixlocal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixlocal.dto.DisputeDetailsDTO;
import com.fixlocal.dto.DisputeMessageRequest;
import com.fixlocal.dto.DisputeRequest;
import com.fixlocal.model.Dispute;
import com.fixlocal.service.DisputeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/disputes")
@Validated
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    public ResponseEntity<Dispute> createDispute(
            @Validated @RequestBody DisputeRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(disputeService.createDispute(request, authentication));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DisputeDetailsDTO>> getAllDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputesWithDetails());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisputeDetailsDTO> getDisputeById(
            @PathVariable String id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(disputeService.getDisputeDetails(id, authentication));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Dispute>> getDisputesByBookingId(@PathVariable String bookingId) {
        return ResponseEntity.ok(disputeService.getDisputesByBookingId(bookingId));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<DisputeDetailsDTO>> getMyDisputes(Authentication authentication) {
        return ResponseEntity.ok(disputeService.getDisputesForReporter(authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisputeDetailsDTO> updateDispute(
            @PathVariable String id,
            @RequestBody Dispute updatedDispute,
            Authentication authentication
    ) {
        return ResponseEntity.ok(disputeService.updateDispute(id, updatedDispute, authentication));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Dispute> addMessage(
            @PathVariable String id,
            Authentication authentication,
            @Validated @RequestBody DisputeMessageRequest request
    ) {
        return ResponseEntity.ok(disputeService.addMessage(id, authentication, request));
    }
}
