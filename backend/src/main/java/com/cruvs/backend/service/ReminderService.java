package com.cruvs.backend.service;

import com.cruvs.backend.dto.reminder.ScheduledNotificationDto;
import com.cruvs.backend.entity.ReminderRule;
import com.cruvs.backend.entity.ScheduledNotification;
import com.cruvs.backend.repository.ReminderRuleRepository;
import com.cruvs.backend.repository.ScheduledNotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {
    private final ReminderRuleRepository reminderRuleRepository;
    private final ScheduledNotificationRepository scheduledNotificationRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createOrUpdateReminders(UUID userId, String sourceType, UUID sourceId, LocalDate targetDate,
                                        Integer leadTimeDays, List<Integer> offsets, String title, String body) {
        deleteRemindersForSource(sourceType, sourceId);

        if (targetDate == null) return;

        String offsetsJson = "[]";
        try {
            offsetsJson = objectMapper.writeValueAsString(offsets);
        } catch (Exception e) {
            log.error("Failed ot serialize offsets list", e);
        }

        ReminderRule rule = ReminderRule.builder()
                .userId(userId)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .targetDate(targetDate)
                .leadTimeDays(leadTimeDays)
                .reminderOffsets(offsetsJson)
                .status("active")
                .build();
        rule = reminderRuleRepository.save(rule);

        List<ScheduledNotification> notifications = new ArrayList<>();
        for (Integer offset : offsets) {
            System.out.println("offset "+offset);
            LocalDate scheduledDate = targetDate.plusDays(offset);
            LocalDateTime scheduledFor = scheduledDate.atTime(9, 0);

            System.out.println("scheduled Date " +scheduledDate);
            System.out.println("Current Date " + LocalDate.now());

            if (offset == 0 && scheduledDate.equals(LocalDate.now())) {
                scheduledFor = LocalDateTime.now().plusMinutes(1);
                System.out.println("Scheduled Date"+scheduledDate);
                System.out.println("Scheduled For"+scheduledFor);


            }

            if (scheduledFor.isAfter(LocalDateTime.now())) {
                ScheduledNotification notification = ScheduledNotification.builder()
                        .reminderRuleId(rule.getId())
                        .userId(userId)
                        .scheduledFor(scheduledFor)
                        .channel("email")
                        .title(title)
                        .body(body)
                        .status("pending")
                        .build();
                notifications.add(notification);
            }
        }
        if (!notifications.isEmpty()) {
            scheduledNotificationRepository.saveAll(notifications);
        }
    }

    @Transactional
    public void deleteRemindersForSource(String sourceType, UUID sourceId) {
        reminderRuleRepository.findBySourceTypeAndSourceId(sourceType, sourceId).ifPresent(rule -> {
            scheduledNotificationRepository.deleteByReminderRuleId(rule.getId());
            reminderRuleRepository.delete(rule);
        });
    }

    public List<ScheduledNotificationDto> getUpcomingNotifications(UUID userId) {
        return scheduledNotificationRepository.findByUserIdAndStatusOrderByScheduledForAsc(userId, "pending")
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void dismissNotification(UUID userId, UUID notificationId) {
        ScheduledNotification notification = scheduledNotificationRepository.findById(notificationId)
                .orElseThrow();
        notification.setStatus("dismissed");
        notification.setSentAt(LocalDateTime.now());
        scheduledNotificationRepository.save(notification);
    }

    @Transactional
    public void snoozeNotification(UUID userId, UUID notificationId, int days) {
        ScheduledNotification notification = scheduledNotificationRepository.findById(notificationId)
                .orElseThrow();
        if (!notification.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        notification.setScheduledFor(LocalDateTime.now().plusDays(days));
        notification.setStatus("pending");
        scheduledNotificationRepository.save(notification);
    }

    public ScheduledNotificationDto mapToDto(ScheduledNotification entity) {
        return ScheduledNotificationDto.builder()
                .id(entity.getId())
                .reminderRuleId(entity.getReminderRuleId())
                .userId(entity.getUserId())
                .scheduledFor(entity.getScheduledFor())
                .channel(entity.getChannel())
                .title(entity.getTitle())
                .body(entity.getBody())
                .status(entity.getStatus())
                .sentAt(entity.getSentAt())
                .build();
    }


}
