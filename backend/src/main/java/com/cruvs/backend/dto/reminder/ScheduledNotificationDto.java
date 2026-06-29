package com.cruvs.backend.dto.reminder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledNotificationDto {
    private UUID id;
    private UUID reminderRuleId;
    private UUID userId;
    private LocalDateTime scheduledFor;
    private String channel;
    private String title;
    private String body;
    private String status;
    private LocalDateTime sentAt;
}
