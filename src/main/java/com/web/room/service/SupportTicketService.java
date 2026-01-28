package com.web.room.service;

import com.web.room.enums.SupportIssueType;
import com.web.room.enums.SupportStatus;
import com.web.room.enums.SupportUrgency;
import com.web.room.model.SupportTicket;
import com.web.room.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository repository;

    /* CREATE SUPPORT TICKET */
    public SupportTicket createTicket(SupportTicket ticket) {
        ticket.setStatus(SupportStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());
        return repository.save(ticket);
    }

    /* ADMIN / INTERNAL USE ONLY */
    public List<SupportTicket> getAllTickets(
            SupportStatus status,
            SupportUrgency urgency,
            SupportIssueType issueType
    ) {
        return repository.filterTickets(status, urgency, issueType);}

    /* UPDATE STATUS */
    public SupportTicket updateStatus(Long id, SupportStatus status) {
        SupportTicket ticket = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Support ticket not found with id: " + id
                        )
                );

        ticket.setStatus(status);
        return repository.save(ticket);
    }

    /* USER-SPECIFIC */
    public List<SupportTicket> getTicketsByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<SupportTicket> getTicketsByStatus(SupportStatus status) {
        return repository.findByStatus (status);
    }
}
