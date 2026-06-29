package com.cruvs.backend.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskCompletionDto {
    private UUID id;
    private UUID taskId;
    private LocalDateTime completedAt;
    private String notesEncrypted;
}