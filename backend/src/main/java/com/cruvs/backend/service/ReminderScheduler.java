package com.cruvs.backend.service;

import com.cruvs.backend.entity.ScheduledNotification;
import com.cruvs.backend.entity.User;
import com.cruvs.backend.repository.ScheduledNotificationRepository;
import com.cruvs.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ScheduledNotificationRepository notificationRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void processScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledNotification> due = notificationRepo
                .findByStatusAndScheduledForLessThanEqual("pending", now);

        if (due.isEmpty()) return;

        log.info("Processing {} due reminder(s)", due.size());

        for (ScheduledNotification notification : due) {
            try {
                UUID userId = notification.getUserId();
                Optional<User> userOpt = userRepo.findById(userId);

                if (userOpt.isEmpty()) {
                    log.warn("User {} not found for notification {}, marking as failed",
                            userId, notification.getId());
                    notification.setStatus("failed");
                    notification.setSentAt(now);
                    notificationRepo.save(notification);
                    continue;
                }

                String email = userOpt.get().getEmail();
                String title = notification.getTitle() != null
                        ? notification.getTitle()
                        : "Jinseiroku Reminder";
                String body = notification.getBody() != null
                        ? notification.getBody()
                        : "You have a reminder due. Check your dashboard for details.";

                boolean sent = emailService.sendReminderEmail(email, title, body);

                if (sent) {
                    notification.setStatus("sent");
                    notification.setSentAt(now);
                    log.info(" Reminder sent to {} — {}", email, title);
                } else {
                    notification.setStatus("failed");
                    notification.setSentAt(now);
                    log.warn(" Email delivery failed for {} — {}", email, title);
                }

                notificationRepo.save(notification);

            } catch (Exception e) {
                log.error("Error processing notification {}: {}",
                        notification.getId(), e.getMessage());
                notification.setStatus("failed");
                notification.setSentAt(now);
                notificationRepo.save(notification);
            }
        }
    }
}