package com.web.room.controller;

import com.web.room.enums.SupportIssueType;
import com.web.room.enums.SupportStatus;
import com.web.room.enums.SupportUrgency;
import com.web.room.model.SupportTicket;
import com.web.room.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService service;

    // CREATE TICKET (USER)
    @PostMapping("/create")
    public ResponseEntity<SupportTicket> createTicket(
            @RequestBody SupportTicket ticket
    ) {
        return ResponseEntity.ok(service.createTicket(ticket));
    }

    // ADMIN / INTERNAL (TEMP)
    @GetMapping("/all")
    public ResponseEntity<List<SupportTicket>> getAll(
            @RequestParam(required = false) SupportStatus status,
            @RequestParam(required = false) SupportUrgency urgency,
            @RequestParam(required = false) SupportIssueType issueType
    ) {
        return ResponseEntity.ok(
                service.getAllTickets (status, urgency, issueType)
        );
    }


    // FILTER BY STATUS (ENUM SAFE)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SupportTicket>> getByStatus(
            @PathVariable SupportStatus status
    ) {
        return ResponseEntity.ok(service.getTicketsByStatus(status));
    }

    // UPDATE STATUS (ENUM SAFE)
    @PutMapping("/{id}/status")
    public ResponseEntity<SupportTicket> updateStatus(
            @PathVariable Long id,
            @RequestParam SupportStatus status
    ) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    // USER: MY SUPPORT REQUESTS
    @GetMapping("/my")
    public ResponseEntity<List<SupportTicket>> getMyRequests(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(service.getTicketsByEmail(email));
    }

}
