
package com.fixlocal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fixlocal.model.Dispute;
import com.fixlocal.repository.DisputeRepository;

@Service
public class DisputeService {

    @Autowired
    private DisputeRepository disputeRepository;

    public Dispute createDispute(Dispute dispute) {
        return disputeRepository.save(dispute);
    }

    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }

    public Dispute getDisputeById(String id) {
        return disputeRepository.findById(id).orElse(null);
    }

    public List<Dispute> getDisputesByBookingId(String bookingId) {
        return disputeRepository.findByBookingId(bookingId);
    }

    public Dispute updateDispute(String id, Dispute updatedDispute) {
        Dispute existingDispute = disputeRepository.findById(id).orElse(null);
        if (existingDispute != null) {
            existingDispute.setStatus(updatedDispute.getStatus());
            existingDispute.setMessages(updatedDispute.getMessages());
            return disputeRepository.save(existingDispute);
        }
        return null;
    }
}
