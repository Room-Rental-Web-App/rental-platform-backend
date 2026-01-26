package com.web.room.repository;

import com.web.room.interfaces.RevenueProjection;
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
            SELECT DATE(s.startDate) as date,
                   COUNT(s.id) as count,
                   SUM(
                     CASE
                       WHEN s.planCode LIKE '%7D%' THEN 199
                       WHEN s.planCode LIKE '%30D%' THEN 499
                       WHEN s.planCode LIKE '%180D%' THEN 2499
                       WHEN s.planCode LIKE '%365D%' THEN 4499
                       ELSE 0
                     END
                   ) as totalAmount
            FROM Subscription s
            WHERE
              (:role IS NULL OR s.role = :role)
              AND (:days IS NULL OR s.planCode LIKE CONCAT('%', :days, 'D%'))
              AND (:from IS NULL OR s.startDate >= :from)
              AND (:to IS NULL OR s.startDate <= :to)
            GROUP BY DATE(s.startDate)
            ORDER BY DATE(s.startDate)
            """)
    List<RevenueProjection> getRevenueReport(
            @Param("role") String role,
            @Param("days") Integer days,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );


}