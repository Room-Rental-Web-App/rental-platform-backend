package com.web.room.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razorpayOrderId;
    private String razorpayPaymentId;

    @ManyToOne
    private User user;

    @ManyToOne
    private PremiumPlan plan;

    private Double amount;
    private String status; // SUCCESS, FAILED, REFUNDED

    private LocalDateTime paymentDate;
}
