package com.cruvs.backend.controller;


import com.cruvs.backend.dto.inbox.InboxItemResponse;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.InboxService;
import com.cruvs.backend.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/v1/inbox")
@RequiredArgsConstructor
public class InboxController {
    private final InboxService inboxService;
    private UUID getUserId() { return (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal(); }
    @PostMapping
    public ResponseEntity<ApiResponse<InboxItemResponse>> capture(
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String textContentEncrypted,
            @RequestParam(required = false) String encryptedDek) throws Exception {
        byte[] bytes = (file != null) ? file.getBytes() : null;
        Long size = (file != null) ? file.getSize() : null;
        String mime = (file != null) ? file.getContentType() : null;
        return ResponseEntity.ok(ApiResponseUtil.success("Captured",
                inboxService.capture(getUserId(), bytes, textContentEncrypted, encryptedDek, size, mime)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InboxItemResponse>>> list() {
        return ResponseEntity.ok(ApiResponseUtil.success("List", inboxService.listUnprocessed(getUserId())));
    }

    @GetMapping(value = "/{id}/download", produces = "application/octet-stream")
    public byte[] download(@PathVariable UUID id) { return inboxService.downloadFile(getUserId(), id); }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        inboxService.delete(getUserId(), id);
        return ResponseEntity.ok(ApiResponseUtil.success("Deleted", null));
    }
    @PutMapping("/{id}/staging")
    public ResponseEntity<ApiResponse<Void>> triage(@PathVariable UUID id, @RequestParam String type, @RequestParam UUID targetId) {
        inboxService.markAsProcessed(getUserId(), id, type, targetId);
        return ResponseEntity.ok(ApiResponseUtil.success("Staged", null));
    }
}
