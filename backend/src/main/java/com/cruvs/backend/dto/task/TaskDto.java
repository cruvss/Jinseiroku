package com.cruvs.backend.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {
    private UUID id;
    private String titleEncrypted;
    private String descriptionEncrypted;
    private String category;

    @lombok.Getter(lombok.AccessLevel.NONE)
    @lombok.Setter(lombok.AccessLevel.NONE)
    private boolean isRecurring;
    
    private String cycleType;

    @com.fasterxml.jackson.annotation.JsonProperty("isRecurring")
    public boolean getIsRecurring() {
        return this.isRecurring;
    }

    public void setIsRecurring(boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
    private Integer cycleInterval;
    private LocalDate dueDate;
    private Integer leadTimeDays;
    private String status;
    private UUID linkedDocumentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}