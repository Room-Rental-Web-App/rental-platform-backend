package com.web.room.repository;

import com.web.room.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            SELECT s
            FROM Subscription s
            WHERE
              (:role IS NULL OR s.role = :role)
              AND (:days IS NULL OR s.planCode LIKE CONCAT('%', :days, 'D%'))
              AND (:from IS NULL OR s.startDate >= :from)
              AND (:to IS NULL OR s.startDate <= :to)
            GROUP BY DATE(s.startDate)
            ORDER BY DATE(s.startDate)
            """)
    List<Subscription> getRevenueReport(
            @Param("role") String role,
            @Param("days") Integer days,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    List<Subscription> findByActiveTrueAndEndDateBefore(LocalDateTime now);
}