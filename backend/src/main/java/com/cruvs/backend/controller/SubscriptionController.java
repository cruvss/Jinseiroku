package com.cruvs.backend.controller;

import com.cruvs.backend.dto.subscription.Sub;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.SubscriptionService;
import com.cruvs.backend.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    private UUID getAuthenticatedUserId() {
        return (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Sub>>> getSubscriptions() {
        List<Sub> list = subscriptionService.getSubscriptionsByUserId(getAuthenticatedUserId());
        return ResponseEntity.ok(ApiResponseUtil.success("Subscriptions retrieved successfully", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Sub>> createSubscription(@RequestBody Sub dto) {
        Sub response = subscriptionService.createSubscription(getAuthenticatedUserId(), dto);
        return ResponseEntity.ok(ApiResponseUtil.success("Subscription created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Sub>> updateSubscription(@PathVariable("id") UUID id, @RequestBody Sub dto) {
        Sub response = subscriptionService.updateSubscription(getAuthenticatedUserId(), id, dto);
        return ResponseEntity.ok(ApiResponseUtil.success("Subscription updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscription(@PathVariable("id") UUID id) {
        subscriptionService.deleteSubscription(getAuthenticatedUserId(), id);
        return ResponseEntity.ok(ApiResponseUtil.success("Subscription deleted successfully", null));
    }
}
