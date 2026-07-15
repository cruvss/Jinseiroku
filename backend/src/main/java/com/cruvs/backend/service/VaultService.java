package com.cruvs.backend.service;

import com.cruvs.backend.dto.minio.VaultDocumentResponse;
import com.cruvs.backend.entity.SubscriptionPlan;
import com.cruvs.backend.entity.User;
import com.cruvs.backend.entity.VaultDocument;
import com.cruvs.backend.exception.AccessDeniedException;
import com.cruvs.backend.exception.BusinessRuleException;
import com.cruvs.backend.exception.ResourceNotFoundException;
import com.cruvs.backend.repository.SubscriptionPlanRepository;
import com.cruvs.backend.repository.UserRepository;
import com.cruvs.backend.repository.VaultDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultService {

    private final VaultDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ReminderService reminderService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Transactional
    public VaultDocumentResponse upload(UUID userId, byte[] fileBytes, String fileNameEncrypted,
                                        String category, String encryptedDek, Long size, String mimeType,
                                        String tagsEncrypted, String notesEncrypted, LocalDate expiryDate) {
        log.info("Vault upload initiated: userId: {}, size: {} bytes, mimeType: {}",userId, size, mimeType);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User",userId));
        SubscriptionPlan plan = user.getSubscriptionPlan();
        if (plan ==null){
            plan = subscriptionPlanRepository.findById(UUID.fromString("b199d750-a9cf-4bc1-9f93-4a6c8e310001"))
                    .orElseThrow(()-> new ResourceNotFoundException("Default subscription plan not configured"));
        }

        if (plan.getMaxAttachmentSizeBytes() !=null && size > plan.getMaxAttachmentSizeBytes()){

            Long sizeInMB = size/(1024*1024);
            log.warn("Upload rejected - size limit: userId: {}, fileSize: {} MB, planLimit: {} MB",userId, sizeInMB, plan.getMaxAttachmentSizeBytes()/(1024*1024));
            throw new BusinessRuleException("File size exceeds your plan limit of "+
                    (plan.getMaxAttachmentSizeBytes() / (1024 * 1024)) + " MB.");
        }

        long totalUsedBytes = documentRepository.sumFileSizeBytesByUser(user);
        log.info("Total vault used: {} MB for userId: {}",totalUsedBytes/(1024*1024), userId );

        long newTotalBytes = totalUsedBytes + size;

        if (plan.getMaxAttachmentSizeBytes() !=null && size > plan.getMaxVaultSizeBytes()){
            throw new BusinessRuleException("Uploading this file will exceed your plan's total vault storage limit of "+
                    (plan.getMaxVaultSizeBytes()/(1024*1024)) + "MB. Currently used: "+
                    String.format("%.2f", (double) totalUsedBytes/(1024*1024)) + "MB.");
        }

        String storageKey = "vault/" + userId + "/" + UUID.randomUUID();
        storageService.uploadFile(storageKey, fileBytes, "application/octet-stream");

        VaultDocument doc = VaultDocument.builder()
                .user(user)
                .fileNameEncrypted(fileNameEncrypted)
                .category(category)
                .blobStorageKey(storageKey)
                .encryptedDek(encryptedDek)
                .fileSizeBytes(size)
                .mimeType(mimeType)
                .tagsEncrypted(tagsEncrypted)
                .notesEncrypted(notesEncrypted)
                .expiryDate(expiryDate)
                .build();

        doc = documentRepository.save(doc);
        log.info("Document uploaded: docId: {}, storageKey: {}, userId: {}", doc.getId(), storageKey, userId);

        if (doc.getExpiryDate() !=null){
            reminderService.createOrUpdateReminders(
                    userId,
                    "VAULT",
                    doc.getId(),
                    doc.getExpiryDate(),
                    null,
                    List.of(0),
                    "Document Expiring Soon",
                    "Your secure document will expire on "+ doc.getExpiryDate()+ ". Please renew it."
            );
            log.warn("Expiry reminder scheduled for document: {}, expiryDate: {}", doc.getId(), doc.getExpiryDate());
        }

        return mapToResponse(doc);
    }

    public Page<VaultDocumentResponse> list(UUID userId, String category, Pageable pageable) {
        log.debug("Listing vault documents for userId: {}, category: {}",userId, category);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User",userId));

        Page<VaultDocument> page = (category != null && !category.isEmpty())
                ? documentRepository.findByUserAndCategory(user, category, pageable)
                : documentRepository.findByUser(user, pageable);

        return page.map(this::mapToResponse);
    }

    public VaultDocumentResponse getMetadata(UUID userId, UUID documentId) {
        VaultDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document",documentId));
        if (!doc.getUser().getId().equals(userId)) {
            log.warn("Unauthorized metadata access: userId: {}, docId: {}", userId, doc.getId());
            throw new AccessDeniedException("Access denied");
        }
        return mapToResponse(doc);
    }

    public byte[] download(UUID userId, UUID documentId) {
        VaultDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document",documentId));
        log.info("Downloading docId: {} for userId: {}", doc.getId(), userId);
        if (!doc.getUser().getId().equals(userId)) {
            log.warn("Unauthorized download attempt: userId: {}, docId: {}", userId, doc.getId());
            throw new AccessDeniedException("Access denied");
        }
        return storageService.downloadFile(doc.getBlobStorageKey());
    }

    @Transactional
    public void delete(UUID userId, UUID documentId) {
        VaultDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document",documentId));
        if (!doc.getUser().getId().equals(userId)) {
            log.warn("Unauthorized delete attempt: userId: {}, docId: {}", userId, doc.getId());
            throw new AccessDeniedException("Access denied");
        }
        log.debug("Document delete attempt- userId: {}, documentId: {}",userId, doc.getId());
        storageService.deleteFile(doc.getBlobStorageKey());
        reminderService.deleteRemindersForSource("VAULT", doc.getId());
        documentRepository.delete(doc);
        log.info("Document deleted: docId: {}, userId: {}",doc.getId(), userId);

    }

    private VaultDocumentResponse mapToResponse(VaultDocument doc) {
        return VaultDocumentResponse.builder()
                .id(doc.getId())
                .fileNameEncrypted(doc.getFileNameEncrypted())
                .category(doc.getCategory())
                .tagsEncrypted(doc.getTagsEncrypted())
                .notesEncrypted(doc.getNotesEncrypted())
                .encryptedDek(doc.getEncryptedDek())
                .fileSizeBytes(doc.getFileSizeBytes())
                .mimeType(doc.getMimeType())
                .expiryDate(doc.getExpiryDate())
                .build();
    }
}