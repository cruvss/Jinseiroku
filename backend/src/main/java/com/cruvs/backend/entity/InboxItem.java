package com.cruvs.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "inbox_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboxItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "text_content_encrypted")
    private String textContentEncrypted;

    @Column(name = "file_storage_key")
    private String fileStorageKey;

    @Column(name = "encrypted_dek")
    private String encryptedDek;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(nullable = false)
    private String status;

    @Column(name = "processed_to_type")
    private String processedToType;

    @Column(name = "processed_to_id")
    private UUID processedToId;

    @Column(name = "captured_at", updatable = false)
    private LocalDateTime capturedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        if (capturedAt == null) capturedAt = LocalDateTime.now();
        if (status == null) status = "unprocessed";
    }
}