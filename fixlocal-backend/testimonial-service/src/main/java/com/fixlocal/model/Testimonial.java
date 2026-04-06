package com.fixlocal.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection = "testimonials")
public class Testimonial {

    @Id
    private String id;

    private String name;
    private String city;
    private String role; // customer or tradesperson
    private String quote;
    private LocalDateTime createdAt;

    public static Testimonial create(String name, String city, String role, String quote) {
        return Testimonial.builder()
                .name(name)
                .city(city)
                .role(role)
                .quote(quote)
                .createdAt(LocalDateTime.now())
                .build();
    }
}