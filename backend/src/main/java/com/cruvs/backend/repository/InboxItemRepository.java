package com.cruvs.backend.repository;

import com.cruvs.backend.entity.InboxItem;
import com.cruvs.backend.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InboxItemRepository extends JpaRepository<InboxItem, UUID> {
    List<InboxItem> findByUserAndStatus(User user, String status, Sort sort);
    long countByUserAndStatus(User user, String status);
}