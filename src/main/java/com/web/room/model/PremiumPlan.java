package com.web.room.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "premium_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // FEATURED, VERIFIED, BANNER

    private Double price;

    private Integer durationDays; // 7, 30, 90

    private String benefits; // JSON string
}
