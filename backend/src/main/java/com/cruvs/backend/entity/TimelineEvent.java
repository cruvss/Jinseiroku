package com.cruvs.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "timeline_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "title_encrypted", nullable = false, columnDefinition = "TEXT")
    private String titleEncrypted;
    @Column(name = "description_encrypted", columnDefinition = "TEXT")
    private String descriptionEncrypted;
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(nullable = false, length = 20)
    private String category;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "linked_document_ids", columnDefinition = "uuid[]")
    private List<UUID> linkedDocumentIds;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}