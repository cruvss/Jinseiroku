package com.cruvs.backend.controller;


import com.cruvs.backend.dto.minio.VaultDocumentResponse;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.AuthService;
import com.cruvs.backend.service.VaultService;
import com.cruvs.backend.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/v1/vault/documents")
@RequiredArgsConstructor
public class VaultController {

    private final VaultService vaultService;

    private UUID getAuthenticatedUserId() {
        return (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VaultDocumentResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileNameEncrypted") String fileNameEncrypted,
            @RequestParam("category") String category,
            @RequestParam("encryptedDek") String encryptedDek,
            @RequestParam(value = "tagsEncrypted", required = false) String tagsEncrypted,
            @RequestParam(value = "notesEncrypted", required = false) String notesEncrypted,
            @RequestParam(value = "expiryDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate
    ) throws IOException {

//        System.out.println(getAuthenticatedUserId());
        VaultDocumentResponse response = vaultService.upload(
                getAuthenticatedUserId(),
                file.getBytes(),
                fileNameEncrypted,
                category,
                encryptedDek,
                file.getSize(),
                file.getContentType(),
                tagsEncrypted,
                notesEncrypted,
                expiryDate
        );
        return ResponseEntity.ok(ApiResponseUtil.success("Document uploaded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VaultDocumentResponse>>> list(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<VaultDocumentResponse> documents = vaultService.list(getAuthenticatedUserId(), category, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponseUtil.success("Documents listed successfully", documents));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VaultDocumentResponse>> getMetadata(@PathVariable("id") UUID documentId) {
        VaultDocumentResponse response = vaultService.getMetadata(getAuthenticatedUserId(), documentId);
        return ResponseEntity.ok(ApiResponseUtil.success("Document metadata retrieved", response));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable("id") UUID documentId) {
        byte[] rawBytes = vaultService.download(getAuthenticatedUserId(), documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"encrypted.bin\"")
                .body(rawBytes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") UUID documentId) {
        vaultService.delete(getAuthenticatedUserId(), documentId);
        return ResponseEntity.ok(ApiResponseUtil.success("Document deleted successfully", null));
    }


}