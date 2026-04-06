package com.fixlocal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalBookingDTO {
    private String id;
    private String userId;
    private String tradespersonId;
    private String status;
    private String serviceDescription;
    private String serviceAddress;
    private Double price;
    private String userName;
    private String userPhone;
}
