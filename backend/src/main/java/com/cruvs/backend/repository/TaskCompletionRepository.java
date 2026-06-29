package com.cruvs.backend.repository;

import com.cruvs.backend.entity.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, UUID> {
    List<TaskCompletion> findByTaskIdOrderByCompletedAtDesc(UUID taskId);
}