package com.web.room.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Setter
@Getter
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;        // Owner or User email

    @Column(nullable = false)
    private String role;         // ROLE_OWNER / ROLE_USER

    @Column(nullable = false)
    private String planCode;     // e.g., ROLE_OWNER_30D

    @Column(name = "amount_paid", nullable = false)
    private Double amountPaid = 0.0;

    // Renamed to be more generic or Cashfree specific
    private String cashfreeOrderId;

    // Optional: Keep a reference to the specific payment ID if needed
    private String cashfreePaymentId;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Column(nullable = false)
    private boolean active;

    // Custom getter for boolean (Good practice for some JSON serializers)
    public boolean getActive(){
        return this.active;
    }
}