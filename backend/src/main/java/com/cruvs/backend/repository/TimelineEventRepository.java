package com.cruvs.backend.repository;

import com.cruvs.backend.entity.TimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimelineEventRepository extends JpaRepository<TimelineEvent, UUID> {

    List<TimelineEvent> findAllByUserIdOrderByEventDateDesc(UUID userId);

}
