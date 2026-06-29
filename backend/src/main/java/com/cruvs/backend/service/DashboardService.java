package com.cruvs.backend.service;

import com.cruvs.backend.dto.dashboard.DashboardStatsResponse;
import com.cruvs.backend.entity.Subscription;
import com.cruvs.backend.entity.Task;
import com.cruvs.backend.entity.User;
import com.cruvs.backend.repository.InboxItemRepository;
import com.cruvs.backend.repository.SubscriptionRepository;
import com.cruvs.backend.repository.TaskRepository;
import com.cruvs.backend.repository.UserRepository;
import com.cruvs.backend.repository.VaultDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final InboxItemRepository inboxRepo;
    private final VaultDocumentRepository vaultRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    public DashboardStatsResponse getStats(UUID userId){
        User user = userRepo.findById(userId).orElseThrow();

        // 1. Subscriptions Calc
        List<Subscription> subs = subscriptionRepo.findAllByUserIdOrderByNextBillingDateAsc(userId);
        BigDecimal monthlyCost = calculateTotalMonthly(subs);
        LocalDate nextRenewal = subs.stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .map(Subscription::getNextBillingDate)
                .findFirst()
                .orElse(null);

        // 2. Tasks Calc
        LocalDate today = LocalDate.now();
        long overdueTasks = taskRepo.countByUserIdAndStatusAndDueDateBefore(userId, "pending", today);
        long pendingToDos = taskRepo.countByUserIdAndIsRecurringAndStatus(userId, false, "pending");

        LocalDate nextRecurringDue = taskRepo.findByUserIdAndIsRecurringAndStatusOrderByDueDateAsc(userId, true, "pending")
                .stream()
                .map(Task::getDueDate)
                .findFirst()
                .orElse(null);

        return DashboardStatsResponse.builder()
                .unprocessedInboxCount(inboxRepo.countByUserAndStatus(user,"unprocessed"))
                .vaultDocumentCount(vaultRepo.countByUser(user))
                .totalMonthlySubscriptionCost(monthlyCost)
                .nextSubscriptionRenewalDate(nextRenewal)
                .overdueTasksCount(overdueTasks)
                .pendingToDosCount(pendingToDos)
                .nextRecurringTaskDueDate(nextRecurringDue)
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