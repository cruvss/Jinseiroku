package com.cruvs.backend.repository;


import com.cruvs.backend.entity.VaultDocument;
import com.cruvs.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VaultDocumentRepository extends JpaRepository<VaultDocument, UUID> {
    Page<VaultDocument> findByUserAndCategory(User user, String category, Pageable pageable);
    Page<VaultDocument> findByUser(User user, Pageable pageable);
    long countByUser(User user);
    Optional<VaultDocument> findFirstByUserAndExpiryDateIsNotNullAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(User user, LocalDate date);
}