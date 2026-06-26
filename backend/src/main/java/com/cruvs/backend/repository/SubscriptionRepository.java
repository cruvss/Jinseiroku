package com.cruvs.backend.repository;

import com.cruvs.backend.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findAllByUserIdOrderByNextBillingDateAsc(UUID userId);
}
