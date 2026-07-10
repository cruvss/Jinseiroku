package com.cruvs.backend.controller;

import com.cruvs.backend.entity.SubscriptionPlan;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.SubscriptionPlanService;
import com.cruvs.backend.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {
    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlan>>> getPlans(){
        List<SubscriptionPlan> plans = subscriptionPlanService.getAllPlans();
        return ResponseEntity.ok(ApiResponseUtil.success("Subscription plans retrived sucessfully", plans));
    }
    @GetMapping("/{planId}/name")
    public ResponseEntity<ApiResponse<String>> getPlanName(@PathVariable UUID planId) {

        String planName = subscriptionPlanService.getPlanName(planId);

        return ResponseEntity.ok(
                ApiResponseUtil.success("Retrieved plan name", planName)
        );
    }
}
