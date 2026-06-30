package com.cruvs.backend.service;

import com.cruvs.backend.dto.dashboard.DashboardStatsResponse;
import com.cruvs.backend.dto.minio.VaultDocumentResponse;
import com.cruvs.backend.dto.reminder.ScheduledNotificationDto;
import com.cruvs.backend.dto.timeline.TimelineEventDto;
import com.cruvs.backend.entity.Subscription;
import com.cruvs.backend.entity.Task;
import com.cruvs.backend.entity.User;
import com.cruvs.backend.entity.VaultDocument;
import com.cruvs.backend.entity.TimelineEvent;
import com.cruvs.backend.entity.ScheduledNotification;
import com.cruvs.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final InboxItemRepository inboxRepo;
    private final VaultDocumentRepository vaultRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final TaskRepository taskRepo;
    private final UserRepository userRepo;
    private final TimelineEventRepository timelineRepo;
    private final ScheduledNotificationRepository notificationRepo;
    private final ReminderService reminderService;

    public DashboardStatsResponse getStats(UUID userId){
        User user = userRepo.findById(userId).orElseThrow();
        LocalDate today = LocalDate.now();

        // 1. Subscriptions Calc
        List<Subscription> subs = subscriptionRepo.findAllByUserIdOrderByNextBillingDateAsc(userId);
        BigDecimal monthlyCost = calculateTotalMonthly(subs);
        LocalDate nextRenewal = subs.stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .map(Subscription::getNextBillingDate)
                .findFirst()
                .orElse(null);

        // 2. Tasks Calc
        long overdueTasks = taskRepo.countByUserIdAndStatusAndDueDateBefore(userId, "pending", today);
        long pendingToDos = taskRepo.countByUserIdAndIsRecurringAndStatus(userId, false, "pending");

        LocalDate nextRecurringDue = taskRepo.findByUserIdAndIsRecurringAndStatusOrderByDueDateAsc(userId, true, "pending")
                .stream()
                .map(Task::getDueDate)
                .findFirst()
                .orElse(null);

        // 3. Next Expiring Vault Doc
        VaultDocumentResponse nextExpiring = vaultRepo.findFirstByUserAndExpiryDateIsNotNullAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(user, today)
                .map(this::mapToVaultResponse)
                .orElse(null);

        // 4. Recent Timeline Event
        TimelineEventDto recentEvent = timelineRepo.findAllByUserIdOrderByEventDateDesc(userId)
                .stream()
                .findFirst()
                .map(this::mapToTimelineDto)
                .orElse(null);

        // 5. Reminders: Upcoming (next 7 days) and Overdue
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfNext7Days = now.plusDays(7);
        // Fetch sent alerts
        List<ScheduledNotification> reminders = notificationRepo.findByUserIdAndStatusOrderByScheduledForAsc(userId, "pending");

        List<ScheduledNotificationDto> upcomingReminders = reminders.stream()
                .filter(r -> r.getScheduledFor().isAfter(now) && r.getScheduledFor().isBefore(endOfNext7Days))
                .map(reminderService::mapToDto)
                .collect(Collectors.toList());
        List<ScheduledNotificationDto> overdueReminders = reminders.stream()
                .filter(r -> r.getScheduledFor().isBefore(now))
                .map(reminderService::mapToDto)
                .collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .unprocessedInboxCount(inboxRepo.countByUserAndStatus(user,"unprocessed"))
                .vaultDocumentCount(vaultRepo.countByUser(user))
                .nextExpiringDocument(nextExpiring)
                .totalMonthlySubscriptionCost(monthlyCost)
                .nextSubscriptionRenewalDate(nextRenewal)
                .overdueTasksCount(overdueTasks)
                .pendingToDosCount(pendingToDos)
                .nextRecurringTaskDueDate(nextRecurringDue)
                .recentTimelineEvent(recentEvent)
                .upcomingReminders(upcomingReminders)
                .overdueReminders(overdueReminders)
                .build();
    }

    private VaultDocumentResponse mapToVaultResponse(VaultDocument doc) {
        return VaultDocumentResponse.builder()
                .id(doc.getId())
                .fileNameEncrypted(doc.getFileNameEncrypted())
                .category(doc.getCategory())
                .tagsEncrypted(doc.getTagsEncrypted())
                .notesEncrypted(doc.getNotesEncrypted())
                .encryptedDek(doc.getEncryptedDek())
                .fileSizeBytes(doc.getFileSizeBytes())
                .mimeType(doc.getMimeType())
                .expiryDate(doc.getExpiryDate())
                .build();
    }

    private TimelineEventDto mapToTimelineDto(TimelineEvent entity) {
        return TimelineEventDto.builder()
                .id(entity.getId())
                .titleEncrypted(entity.getTitleEncrypted())
                .descriptionEncrypted(entity.getDescriptionEncrypted())
                .eventDate(entity.getEventDate())
                .endDate(entity.getEndDate())
                .category(entity.getCategory())
                .linkedDocumentIds(entity.getLinkedDocumentIds())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private BigDecimal calculateTotalMonthly(List<Subscription> subs) {
        BigDecimal total = BigDecimal.ZERO;
        for (Subscription sub : subs) {
            if (!"ACTIVE".equalsIgnoreCase(sub.getStatus())) continue;

            BigDecimal rate = getExchangeRate(sub.getCurrency());
            BigDecimal costInNpr = sub.getCost().multiply(rate);

            BigDecimal monthlyCost = switch (sub.getBillingCycle().toUpperCase()) {
                case "WEEKLY" -> costInNpr.multiply(new BigDecimal("4.333"));
                case "FORTNIGHTLY" -> costInNpr.multiply(new BigDecimal("2.166"));
                case "MONTHLY" -> costInNpr;
                case "QUARTERLY" -> costInNpr.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
                case "SEMI-YEARLY" -> costInNpr.divide(new BigDecimal("6"), 2, RoundingMode.HALF_UP);
                case "YEARLY" -> costInNpr.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
                default -> BigDecimal.ZERO;
            };
            total = total.add(monthlyCost);
        }
        return total;
    }

    private BigDecimal getExchangeRate(String currency) {
        if (currency == null) return BigDecimal.ONE;
        return switch (currency.toUpperCase()) {
            case "USD" -> new BigDecimal("150.40");
            case "EUR" -> new BigDecimal("171.26");
            case "GBP" -> new BigDecimal("198.67");
            case "JPY" -> new BigDecimal("0.93");
            default -> BigDecimal.ONE;
        };
    }
}