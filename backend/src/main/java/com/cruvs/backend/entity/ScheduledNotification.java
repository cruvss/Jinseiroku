package com.cruvs.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scheduled_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reminder_rule_id", nullable = false)
    private UUID reminderRuleId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "scheduled_for", nullable = false)
    private LocalDateTime scheduledFor;

    @Column(length = 20)
    private String channel;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(length = 20)
    private String status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}