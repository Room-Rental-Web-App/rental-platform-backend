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
    private String email;        // owner OR user email

    @Column(nullable = false)
    private String role;         // ROLE_OWNER / ROLE_USER

    @Column(nullable = false)
    private String planCode;     // USER_PLAN, OWNER_PLAN, OWNER_FEATURED

    // NEW: Transaction details to fix the DB error
    @Column(name = "amount_paid", nullable = false)
    private Double amountPaid = 0.0;

    private String razorpayPaymentId;
    private String razorpayOrderId;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private boolean active;

    // Custom getter for boolean (Lombok sometimes handles it differently)
    public boolean getActive(){
        return this.active;
    }



}