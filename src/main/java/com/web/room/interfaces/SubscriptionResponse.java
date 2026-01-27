package com.web.room.interfaces;

import com.web.room.model.Subscription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Setter @Getter
public class SubscriptionResponse {
    private Long id;
    private String email;        // owner OR user email
    private String role;         // ROLE_OWNER / ROLE_USER
    private String planCode;     // USER_PLAN, OWNER_PLAN, OWNER_FEATURED
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    public SubscriptionResponse(Subscription subscription){
        this.id=subscription.getId ();
        this.email=subscription.getEmail ();
        this.role=subscription.getRole ();
        this.planCode=subscription.getPlanCode ();
        this.startDate=subscription.getStartDate ();
        this.endDate=subscription.getEndDate ();
        this.active=subscription.getActive();
    }
}
