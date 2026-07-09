package com.cruvs.backend.controller;

import com.cruvs.backend.entity.SubscriptionPlan;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.SubscriptionPlanService;
import com.cruvs.backend.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
