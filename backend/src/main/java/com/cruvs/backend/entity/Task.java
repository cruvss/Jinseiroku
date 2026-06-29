package com.cruvs.backend.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name="tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "title_encrypted", nullable = false, columnDefinition = "TEXT")
    private String titleEncrypted;
    @Column(name = "description_encrypted", columnDefinition = "TEXT")
    private String descriptionEncrypted;
    @Column(nullable = false, length = 50)
    private String category;
    @Column(name = "is_recurring")
    private boolean isRecurring;
    @Column(name = "cycle_type", length = 20)
    private String cycleType;
    @Column(name = "cycle_interval")
    private Integer cycleInterval;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "lead_time_days")
    private Integer leadTimeDays;
    @Column(length = 20)
    private String status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_document_id")
    private VaultDocument linkedDocument;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
