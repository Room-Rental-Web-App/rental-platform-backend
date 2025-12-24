package com.web.room.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Room Information
    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    private String roomType; // e.g., "Single Room", "Apartment", "Studio"

    // Location and Address Details
    private String address;
    private String city;

    @Column(nullable = false)
    private String pincode;

    // Exact Geolocation for Map Integration
    private Double latitude;
    private Double longitude;

    // Media Links (Cloudinary URLs)
    @ElementCollection
    private List<String> imageUrls;

    private String videoUrl;

    // Contact Information (Restricted to Premium Users on Frontend)
    private String contactNumber;

    // Ownership and Security
    @Column(nullable = false)
    private String ownerEmail;

    private boolean isAvailable = true;

    // Admin Control Feature
    private boolean isApprovedByAdmin = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}