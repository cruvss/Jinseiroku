package com.cruvs.backend.dto.timeline;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimelineEventDto {
    private UUID id;
    private String titleEncrypted;
    private String descriptionEncrypted;
    private LocalDate eventDate;
    private LocalDate endDate;
    private String category;
    private List<UUID> linkedDocumentIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}