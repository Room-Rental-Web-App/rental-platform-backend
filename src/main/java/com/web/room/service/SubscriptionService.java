package com.web.room.service;

import com.web.room.interfaces.RevenueProjection;
import com.web.room.model.Subscription;
import com.web.room.repository.SubscriptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repo;

    public SubscriptionService(SubscriptionRepository repo) {
        this.repo = repo;
    }

    public ResponseEntity<?> isPremium(String email, String role) {
        boolean isPremium = repo.findTopByEmailAndRoleAndActiveTrueAndEndDateAfterOrderByEndDateDesc (
                email, role, LocalDateTime.now ()
        ).isPresent ();
        return ResponseEntity.ok (Map.of ("isPremium", isPremium));
    }

    public ResponseEntity<?> getAllSubscriptions(String email) {
        return ResponseEntity.ok (repo.findAllByEmail (email));
    }

    public List<RevenueProjection> getRevenueReport(String role, Integer days, String from, String to) {
        return repo.getRevenueReport ( (role == null || role.isBlank()) ? null : role,
                days,
                (from == null || from.isBlank()) ? null : LocalDateTime.parse(from),
                (to == null || to.isBlank()) ? null : LocalDateTime.parse(to));
    }
}
