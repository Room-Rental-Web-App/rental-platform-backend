package com.web.room.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double price;

    private String roomType;
    private String address;
    private String city;

    @Column(nullable = false)
    private String pincode;

    private Double latitude;
    private Double longitude;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<String> imageUrls;

    private String videoUrl;
    private String contactNumber;

    @Column(nullable = false)
    private String ownerEmail;

    // Standard naming: 'available' instead of 'isAvailable'
    // JsonProperty ensures frontend still sees "isAvailable"
    @JsonProperty("isAvailable")
    @Column(name = "is_available", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean available = true;

    @JsonProperty("isApprovedByAdmin")
    @Column(name = "is_approved_by_admin", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean approvedByAdmin = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.available == null) this.available = true;
        if (this.approvedByAdmin == null) this.approvedByAdmin = false;
        if (this.contactViewCount == null) this.contactViewCount = 0;
        if (this.featured == null) this.featured = false;
    }

    @ElementCollection
    private List<String> amenities;

    private String availableFor;
    private Long area;

    @JsonProperty("isFeatured")
    @Column(name = "is_featured", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean featured = false;

    private Integer priorityScore = 0;

    @Column(nullable = false)
    private Integer contactViewCount = 0;

    private String statusUpdatedBy;
}