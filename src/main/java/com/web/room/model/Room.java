    package com.web.room.model;

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
        @OnDelete(action = OnDeleteAction.CASCADE) // Database constraint fix
        private List<String> imageUrls;

        private String videoUrl;
        private String contactNumber;

        @Column(nullable = false)
        private String ownerEmail;

        private boolean isAvailable = true;
        private boolean isApprovedByAdmin = false;

        @Column(updatable = false)
        private LocalDateTime createdAt;

        @PrePersist
        protected void onCreate() {
            this.createdAt = LocalDateTime.now ();
        }

        @ElementCollection
        private List<String> amenities;

        private String availableFor; // family, couples, solo, group, girls, boys, etc.
        private Long area; // in square feet

        private boolean isFeatured;
        private Integer priorityScore;
    }