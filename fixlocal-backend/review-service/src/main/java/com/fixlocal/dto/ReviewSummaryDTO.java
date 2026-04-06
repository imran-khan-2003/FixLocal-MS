package com.fixlocal.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewSummaryDTO {

    private String id;
    private int rating;
    private String comment;
    private String bookingId;
    private String userId;
    private String userName;
    private String tradespersonId;
    private String tradespersonName;
    private java.time.LocalDateTime createdAt;
}
