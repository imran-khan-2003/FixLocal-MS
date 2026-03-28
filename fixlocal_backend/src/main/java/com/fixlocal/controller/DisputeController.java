
package com.fixlocal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixlocal.model.Dispute;
import com.fixlocal.service.DisputeService;

@RestController
@RequestMapping("/api/disputes")
public class DisputeController {

    @Autowired
    private DisputeService disputeService;

    @PostMapping
    public ResponseEntity<Dispute> createDispute(@RequestBody Dispute dispute) {
        return ResponseEntity.ok(disputeService.createDispute(dispute));
    }

    @GetMapping
    public ResponseEntity<List<Dispute>> getAllDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dispute> getDisputeById(@PathVariable String id) {
        Dispute dispute = disputeService.getDisputeById(id);
        if (dispute != null) {
            return ResponseEntity.ok(dispute);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Dispute>> getDisputesByBookingId(@PathVariable String bookingId) {
        return ResponseEntity.ok(disputeService.getDisputesByBookingId(bookingId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dispute> updateDispute(@PathVariable String id, @RequestBody Dispute updatedDispute) {
        Dispute dispute = disputeService.updateDispute(id, updatedDispute);
        if (dispute != null) {
            return ResponseEntity.ok(dispute);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
