    package com.web.room.model.reports;

    import com.web.room.enums.ReportStatus;
    import com.web.room.model.User;
    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;

    import java.time.LocalDateTime;

    @Entity
    @Table(name = "reports")
    @Inheritance(strategy = InheritanceType.JOINED)
    @Getter @Setter
    public abstract class Report {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // Who reported
        @ManyToOne(optional = false)
        private User reporter;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private ReportStatus status = ReportStatus.PENDING;

        @Column(nullable = false, length = 500)
        private String reason;

        @Column(updatable = false)
        private LocalDateTime createdAt;

        @PrePersist
        protected void onCreate() {
            this.createdAt = LocalDateTime.now();
        }
    }
