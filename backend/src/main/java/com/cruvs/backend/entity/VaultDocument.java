package com.cruvs.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vault_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaultDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_name_encrypted", nullable = false, columnDefinition = "TEXT")
    private String fileNameEncrypted;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "tags_encrypted", columnDefinition = "TEXT")
    private String tagsEncrypted;

    @Column(name = "notes_encrypted", columnDefinition = "TEXT")
    private String notesEncrypted;

    @Column(name = "blob_storage_key", nullable = false, length = 500)
    private String blobStorageKey;

    @Column(name = "encrypted_dek", nullable = false, columnDefinition = "TEXT")
    private String encryptedDek;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}