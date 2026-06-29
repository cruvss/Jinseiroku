package com.cruvs.backend.dto.dashboard;

import com.cruvs.backend.dto.minio.VaultDocumentResponse;
import com.cruvs.backend.dto.reminder.ScheduledNotificationDto;
import com.cruvs.backend.dto.timeline.TimelineEventDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @Builder
public class DashboardStatsResponse {
    private long unprocessedInboxCount;
    private long vaultDocumentCount;
    private VaultDocumentResponse nextExpiringDocument;

    // Subscriptions stats
    private BigDecimal totalMonthlySubscriptionCost;
    private LocalDate nextSubscriptionRenewalDate;

    // Tasks stats
    private long overdueTasksCount;
    private long pendingToDosCount;
    private LocalDate nextRecurringTaskDueDate;

    // Timeline Stats
    private TimelineEventDto recentTimelineEvent;

    // Reminders stats
    private List<ScheduledNotificationDto> upcomingReminders;
    private List<ScheduledNotificationDto> overdueReminders;
}