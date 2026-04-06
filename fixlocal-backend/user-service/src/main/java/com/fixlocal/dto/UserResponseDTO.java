package com.fixlocal.dto;

import com.fixlocal.model.Role;
import com.fixlocal.model.Status;
import java.util.List;
import lombok.Data;

@Data
public class UserResponseDTO {

    private String id;
    private String name;
    private String email;
    private Role role;

    private String occupation;
    private String workingCity;
    private int experience;

    private double averageRating;
    private int totalReviews;

    private Status status;
    private boolean verified;

    private String profileImage;
    private String bio;
    private String phone;

    private Integer completedJobs;

    private Double lastKnownLatitude;
    private Double lastKnownLongitude;

    private boolean available;
    private String currentBookingId;

    private java.util.List<String> skillTags;

    private java.util.List<ServiceOfferingDTO> serviceOfferings;
}