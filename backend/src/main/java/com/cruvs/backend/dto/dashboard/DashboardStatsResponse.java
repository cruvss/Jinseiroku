package com.cruvs.backend.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class DashboardStatsResponse {
    private long unprocessedInboxCount;
    private long vaultDocumentCount;
}