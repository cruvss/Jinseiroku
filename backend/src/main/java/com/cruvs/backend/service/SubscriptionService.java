package com.cruvs.backend.service;

import com.cruvs.backend.dto.subscription.Sub;
import com.cruvs.backend.entity.Subscription;
import com.cruvs.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public List<Sub> getSubscriptionsByUserId(UUID userId) {
        return subscriptionRepository.findAllByUserIdOrderByNextBillingDateAsc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Sub createSubscription(UUID userId, Sub dto) {
        Subscription entity = Subscription.builder()
                .userId(userId)
                .name(dto.getName())
                .cost(dto.getCost())
                .currency(dto.getCurrency())
                .billingCycle(dto.getBillingCycle())
                .nextBillingDate(dto.getNextBillingDate())
                .status(dto.getStatus())
                .linkedDocumentId(dto.getLinkedDocumentId())
                .build();

        entity = subscriptionRepository.save(entity);
        return mapToDto(entity);
    }

    @Transactional
    public Sub updateSubscription(UUID userId, UUID subscriptionId, Sub dto) {
        Subscription entity = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        if (!entity.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        entity.setName(dto.getName());
        entity.setCost(dto.getCost());
        entity.setCurrency(dto.getCurrency());
        entity.setBillingCycle(dto.getBillingCycle());
        entity.setNextBillingDate(dto.getNextBillingDate());
        entity.setStatus(dto.getStatus());
        entity.setLinkedDocumentId(dto.getLinkedDocumentId());

        entity = subscriptionRepository.save(entity);
        return mapToDto(entity);
    }

    @Transactional
    public void deleteSubscription(UUID userId, UUID subscriptionId) {
        Subscription entity = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        if (!entity.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        subscriptionRepository.delete(entity);
    }

    private Sub mapToDto(Subscription entity) {
        Sub dto = new Sub();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCost(entity.getCost());
        dto.setCurrency(entity.getCurrency());
        dto.setBillingCycle(entity.getBillingCycle());
        dto.setNextBillingDate(entity.getNextBillingDate());
        dto.setStatus(entity.getStatus());
        dto.setLinkedDocumentId(entity.getLinkedDocumentId());
        return dto;
    }
}
