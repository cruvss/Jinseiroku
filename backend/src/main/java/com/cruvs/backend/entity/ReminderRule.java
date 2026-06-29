package com.cruvs.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reminder_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "reminder_offsets", columnDefinition = "jsonb")
    private String reminderOffsets;

    @Column(length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}