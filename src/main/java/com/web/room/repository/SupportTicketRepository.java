package com.web.room.repository;

import com.web.room.enums.SupportIssueType;
import com.web.room.enums.SupportStatus;
import com.web.room.enums.SupportUrgency;
import com.web.room.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByStatus(SupportStatus status);

    List<SupportTicket> findByEmail(String email);

    @Query("""
        SELECT s FROM SupportTicket s
        WHERE (:status IS NULL OR s.status = :status)
          AND (:urgency IS NULL OR s.urgency = :urgency)
          AND (:issueType IS NULL OR s.issueType = :issueType)
    """)
    List<SupportTicket> filterTickets(
            @Param("status") SupportStatus status,
            @Param("urgency") SupportUrgency urgency,
            @Param("issueType") SupportIssueType issueType
    );

}
