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
     * Smart Check: Returns detailed status with the NEW strictly reduced room limits.
     */
    public ResponseEntity<?> isPremium(String email, String role) {
        // 1. Fetch active subscription
        Optional<Subscription> subOpt = subscriptionRepo.findTopByEmailAndRoleAndActiveTrueAndEndDateAfterOrderByEndDateDesc(
                email, role, LocalDateTime.now()
        );

        // 2. Default values for Free Users
        boolean isPremium = subOpt.isPresent();
        int roomLimit = 2; // Fixed Free Limit
        String planCode = "FREE";
        LocalDateTime endDate = null;

        // 3. Determine limits based on the new reduced-count strategy
        if (isPremium) {
            Subscription sub = subOpt.get();
            planCode = sub.getPlanCode();
            endDate = sub.getEndDate();

            // Room limits mapped to your new request:
            if (planCode.contains("7D")) {
                roomLimit = 3;      // Trial (7 Days @ 99)
            } else if (planCode.contains("30D")) {
                roomLimit = 6;      // Monthly (1 Month @ 199)
            } else if (planCode.contains("180D")) {
                roomLimit = 15;     // Half-Yearly (6 Months @ 999)
            } else if (planCode.contains("365D")) {
                roomLimit = 40;     // Yearly (1 Year @ 1499)
            }
        }

        // 4. Calculate current room count
        long currentRooms = 0;
        if ("ROLE_OWNER".equals(role)) {
            currentRooms = roomRepo.countByOwnerEmail(email);
        }

        // 5. Response for Frontend
        return ResponseEntity.ok(Map.of(
                "isPremium", isPremium,
                "planCode", planCode,
                "endDate", endDate != null ? endDate : "N/A",
                "roomLimit", roomLimit,
                "currentRoomCount", currentRooms,
                "canAddMoreRooms", currentRooms < roomLimit
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