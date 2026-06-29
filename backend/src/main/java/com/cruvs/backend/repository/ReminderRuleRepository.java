package com.cruvs.backend.repository;

import com.cruvs.backend.entity.ReminderRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReminderRuleRepository extends JpaRepository<ReminderRule, UUID> {
    Optional<ReminderRule> findBySourceTypeAndSourceId(String sourceType, UUID sourceId);
    void deleteBySourceTypeAndSourceId(String sourceType, UUID sourceId);
}