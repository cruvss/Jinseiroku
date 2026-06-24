package com.cruvs.backend.dto.minio;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class VaultDocumentResponse {
    private UUID id;
    private String fileNameEncrypted;
    private String category;
    private String tagsEncrypted;
    private String notesEncrypted;
    private String encryptedDek;
    private Long fileSizeBytes;
    private String mimeType;
    private LocalDate expiryDate;
}