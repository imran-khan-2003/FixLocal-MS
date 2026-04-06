
package com.fixlocal.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection = "disputes")
public class Dispute {

    @Id
    private String id;

    private String bookingId;
    private String reporterId; // User who reported the dispute
    private String reason;
    private String desiredOutcome;

    @Builder.Default
    private DisputeStatus status = DisputeStatus.OPEN;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private List<DisputeMessage> messages = new ArrayList<>();

    @Data
    @Builder
    public static class DisputeMessage {
        private String senderId;
        private String message;
        @Builder.Default
        private LocalDateTime timestamp = LocalDateTime.now();
    }
}
