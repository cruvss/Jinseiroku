package com.cruvs.backend.dto.inbox;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class InboxItemResponse {
    private UUID id;
    private String contentType;
    private String textContentEncrypted;
    private String encryptedDek;
    private Long fileSizeBytes;
    private String mimeType;
    private String status;
    private LocalDateTime capturedAt;
}