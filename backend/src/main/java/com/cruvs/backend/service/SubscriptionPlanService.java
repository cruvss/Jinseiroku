package com.cruvs.backend.service;

import com.cruvs.backend.entity.SubscriptionPlan;
import com.cruvs.backend.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public List<SubscriptionPlan> getAllPlans() {
        return subscriptionPlanRepository.findAll();
    }

    public String getPlanName(UUID planId){
        return subscriptionPlanRepository.findById(planId)
                .map(SubscriptionPlan::getName)
                .orElseThrow();
    }
}
