package com.cruvs.backend.repository;

import com.cruvs.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUserIdOrderByDueDateAsc(UUID userId);
    long countByUserIdAndStatusAndDueDateBefore(UUID userId, String status, LocalDate date);
    long countByUserIdAndIsRecurringAndStatus(UUID userId, boolean isRecurring, String status);
    List<Task> findByUserIdAndIsRecurringAndStatusOrderByDueDateAsc(UUID userId, boolean isRecurring, String status);
}