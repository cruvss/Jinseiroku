package com.cruvs.backend.repository;

import com.cruvs.backend.entity.ScheduledNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduledNotificationRepository extends JpaRepository<ScheduledNotification, UUID> {
    List<ScheduledNotification> findByUserIdAndStatusOrderByScheduledForAsc(UUID userId, String status);
    List<ScheduledNotification> findByStatusAndScheduledForLessThanEqual(String status, LocalDateTime time);
    void deleteByReminderRuleId(UUID reminderRuleId);
}
