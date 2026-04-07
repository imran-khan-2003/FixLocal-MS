package com.fixlocal.dto;

import com.fixlocal.enums.Status;
import java.util.List;
import lombok.Data;

@Data
public class TradespersonDTO {

    private String id;
    private String name;

    private String occupation;
    private String workingCity;
    private int experience;

    private double averageRating;
    private int totalReviews;

    private boolean verified;

    private Status status;

    // ⭐ UI improvements
    private String profileImage;
    private String bio;
    private int completedJobs;

    private boolean available;

    private Double lastKnownLatitude;
    private Double lastKnownLongitude;

    private List<String> skillTags;

    private List<ServiceOfferingDTO> serviceOfferings;

    private Double distanceKm;
}