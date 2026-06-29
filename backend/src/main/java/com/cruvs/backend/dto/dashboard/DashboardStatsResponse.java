package com.cruvs.backend.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class DashboardStatsResponse {
    private long unprocessedInboxCount;
    private long vaultDocumentCount;

    // Subscriptions stats
    private BigDecimal totalMonthlySubscriptionCost;
    private LocalDate nextSubscriptionRenewalDate;

    // Tasks stats
    private long overdueTasksCount;
    private long pendingToDosCount;
    private LocalDate nextRecurringTaskDueDate;
}