package com.cruvs.backend.service;

import com.cruvs.backend.dto.subscription.Sub;
import com.cruvs.backend.entity.Subscription;
import com.cruvs.backend.exception.AccessDeniedException;
import com.cruvs.backend.exception.ResourceNotFoundException;
import com.cruvs.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final ReminderService reminderService;

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
        log.info("Subscription created: id: {}, name: {}, userId: {}", entity.getId(), dto.getName(), userId);
        if (entity.getNextBillingDate() != null) {
            reminderService.createOrUpdateReminders(
                    userId,
                    "SUBSCRIPTION",
                    entity.getId(),
                    entity.getNextBillingDate(),
                    null,
//                    List.of(-7, -3, -1),
                    List.of(0),
                    "Subscription Renewal",
                    "Subscription " + entity.getName() + " is renewing soon on " + entity.getNextBillingDate()
            );
        }
        return mapToDto(entity);
    }

    @Transactional
    public Sub updateSubscription(UUID userId, UUID subscriptionId, Sub dto) {
        Subscription entity = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription",subscriptionId));

        if (!entity.getUserId().equals(userId)) {
            log.warn("Unauthorized subscription update: userId: {}, subscriptionId: {}", userId, entity.getId());
            throw new AccessDeniedException("Access denied");
        }

        entity.setName(dto.getName());
        entity.setCost(dto.getCost());
        entity.setCurrency(dto.getCurrency());
        entity.setBillingCycle(dto.getBillingCycle());
        entity.setNextBillingDate(dto.getNextBillingDate());
        entity.setStatus(dto.getStatus());
        entity.setLinkedDocumentId(dto.getLinkedDocumentId());

        entity = subscriptionRepository.save(entity);

        if (entity.getNextBillingDate() != null) {
            reminderService.createOrUpdateReminders(
                    userId,
                    "SUBSCRIPTION",
                    entity.getId(),
                    entity.getNextBillingDate(),
                    null,
                    List.of(-7, -3, -1),
                    "Subscription Renewal",
                    "Subscription " + entity.getName() + " is renewing soon on " + entity.getNextBillingDate()
            );
        }
        return mapToDto(entity);
    }

    @Transactional
    public void deleteSubscription(UUID userId, UUID subscriptionId) {
        Subscription entity = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription",subscriptionId));

        if (!entity.getUserId().equals(userId)) {
            log.warn("Unauthorized subscription delete: userId: {}, subscriptionId: {}", userId, entity.getId());
            throw new AccessDeniedException("Access denied");
        }
        reminderService.deleteRemindersForSource("SUBSCRIPTION", entity.getId());
        log.info("Subscription deleted: id: {}, userId: {}",entity.getId(), userId);
        subscriptionRepository.delete(entity);
    }

    private Sub mapToDto(Subscription sub){
        return Sub.builder()
                .id(sub.getId())
                .name(sub.getName())
                .cost(sub.getCost())
                .currency(sub.getCurrency())
                .billingCycle(sub.getBillingCycle())
                .status(sub.getStatus())
                .nextBillingDate(sub.getNextBillingDate())
                .linkedDocumentId(sub.getLinkedDocumentId())
                .build();
    }
}
