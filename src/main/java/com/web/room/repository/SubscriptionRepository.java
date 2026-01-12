package com.web.room.repository;

import com.web.room.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findTopByEmailAndRoleAndActiveTrueAndEndDateAfterOrderByEndDateDesc(
            String email,
            String role,
            LocalDateTime now
    );
    List<Subscription> findAllByEmail(String email);
}