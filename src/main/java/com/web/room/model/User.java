package com.web.room.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    private String role; // ROLE_USER, ROLE_OWNER, ROLE_ADMIN
    private String phone; // New field
    private String aadharUrl; // Path to stored Aadhar image
    private String status; // PENDING, APPROVED, REJECTED (New field)

    private String otp;
    private LocalDateTime otpExpiry;

    private Boolean enabled;
    private Boolean isVerifiedUser;
    private String fullName;
}