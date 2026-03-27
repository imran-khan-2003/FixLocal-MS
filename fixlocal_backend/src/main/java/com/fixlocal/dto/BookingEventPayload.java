package com.fixlocal.dto;

import com.fixlocal.model.BookingStatus;
import com.fixlocal.model.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingEventPayload {

    private String bookingId;
    private BookingStatus status;
    private NotificationType type;
    private String message;
    private LocalDateTime timestamp;
}
