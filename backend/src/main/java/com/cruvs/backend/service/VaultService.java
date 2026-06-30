package com.cruvs.backend.service;

import com.cruvs.backend.dto.minio.VaultDocumentResponse;
import com.cruvs.backend.entity.User;
import com.cruvs.backend.entity.VaultDocument;
import com.cruvs.backend.repository.UserRepository;
import com.cruvs.backend.repository.VaultDocumentRepository;
import lombok.RequiredArgsConstructor;
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
public class VaultService {

    private final VaultDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ReminderService reminderService;

    @Transactional
    public VaultDocumentResponse upload(UUID userId, byte[] fileBytes, String fileNameEncrypted,
                                        String category, String encryptedDek, Long size, String mimeType,
                                        String tagsEncrypted, String notesEncrypted, LocalDate expiryDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

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
        }

        return mapToResponse(doc);
    }

    public Page<VaultDocumentResponse> list(UUID userId, String category, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Page<VaultDocument> page = (category != null && !category.isEmpty())
                ? documentRepository.findByUserAndCategory(user, category, pageable)
                : documentRepository.findByUser(user, pageable);

        return page.map(this::mapToResponse);
    }

    public VaultDocumentResponse getMetadata(UUID userId, UUID documentId) {
        VaultDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (!doc.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return mapToResponse(doc);
    }

    public byte[] download(UUID userId, UUID documentId) {
        VaultDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (!doc.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return storageService.downloadFile(doc.getBlobStorageKey());
    }

    @Transactional
    public void delete(UUID userId, UUID documentId) {
        VaultDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (!doc.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        storageService.deleteFile(doc.getBlobStorageKey());

        reminderService.deleteRemindersForSource("VAULT", doc.getId());
        documentRepository.delete(doc);
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