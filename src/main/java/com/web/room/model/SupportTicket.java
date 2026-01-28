package com.web.room.model;

import com.web.room.enums.SupportIssueType;
import com.web.room.enums.SupportStatus;
import com.web.room.enums.SupportUrgency;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportIssueType issueType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportUrgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;
}
