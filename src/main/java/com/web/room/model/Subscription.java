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

    private String email;        // owner OR user email
    private String role;         // ROLE_OWNER / ROLE_USER

    private String planCode;     // USER_PLAN, OWNER_PLAN, OWNER_FEATURED
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    public  boolean getActive(){
        return  this.active;
    }
}
