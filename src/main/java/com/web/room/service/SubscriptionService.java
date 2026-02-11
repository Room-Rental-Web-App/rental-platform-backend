package com.web.room.service;

import com.web.room.interfaces.SubscriptionResponse;
import com.web.room.model.Subscription;
import com.web.room.repository.SubscriptionRepository;
import com.web.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepo;
    private final RoomRepository roomRepo;

    /**
     * Smart Check: Returns detailed status based on CURRENT plan usage only.
     */
    public ResponseEntity<?> isPremium(String email, String role) {
        // 1. Fetch latest active subscription
        Optional<Subscription> subOpt = subscriptionRepo.findTopByEmailAndRoleAndActiveTrueAndEndDateAfterOrderByEndDateDesc(
                email, role, LocalDateTime.now()
        );

        boolean isPremium = subOpt.isPresent();
        int roomLimit = 2;
        String planCode = "FREE";
        LocalDateTime endDate = null;
        long currentPlanCount = 0; // Standardized variable name

        if (isPremium) {
            Subscription sub = subOpt.get();
            planCode = sub.getPlanCode();
            endDate = sub.getEndDate();

            // Set limits based on plan
            if (planCode.contains("7D")) roomLimit = 3;
            else if (planCode.contains("30D")) roomLimit = 6;
            else if (planCode.contains("180D")) roomLimit = 15;
            else if (planCode.contains("365D")) roomLimit = 40;

            // Logic: Count only rooms linked to THIS subscription ID (Fresh Start)
            currentPlanCount = roomRepo.countByOwnerEmailAndSubscriptionId(email, sub.getId());
        } else {
            // Count rooms without any subscription ID (Free users)
            currentPlanCount = roomRepo.countByOwnerEmailAndSubscriptionIdIsNull(email);
        }

        // 5. Response for Frontend
        return ResponseEntity.ok(Map.of(
                "isPremium", isPremium,
                "planCode", planCode,
                "endDate", endDate != null ? endDate.toString() : "N/A",
                "roomLimit", roomLimit,
                "currentRoomCount", currentPlanCount,
                "canAddMoreRooms", currentPlanCount < roomLimit
        ));
    }

    public ResponseEntity<?> getAllSubscriptions(String email) {
        return ResponseEntity.ok(subscriptionRepo.findAllByEmail(email));
    }

    public List<SubscriptionResponse> getRevenueReport(String role, Integer days, String from, String to) {
        List<Subscription> subscriptions = subscriptionRepo.getRevenueReport(
                (role == null || role.isBlank()) ? null : role,
                days,
                (from == null || from.isBlank()) ? null : LocalDateTime.parse(from),
                (to == null || to.isBlank()) ? null : LocalDateTime.parse(to)
        );

        return subscriptions.stream().map(SubscriptionResponse::new).toList();
    }
}