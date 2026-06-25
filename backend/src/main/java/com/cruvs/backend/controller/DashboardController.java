package com.cruvs.backend.controller;

import com.cruvs.backend.dto.dashboard.DashboardStatsResponse;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.DashboardService;
import com.cruvs.backend.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        UUID userId = (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        return ResponseEntity.ok(ApiResponseUtil.success("Stats", dashService.getStats(userId)));
    }
}
