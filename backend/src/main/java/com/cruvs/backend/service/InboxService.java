package com.cruvs.backend.service;


import com.cruvs.backend.dto.inbox.InboxItemResponse;
import com.cruvs.backend.entity.InboxItem;
import com.cruvs.backend.entity.User;
import com.cruvs.backend.exception.AccessDeniedException;
import com.cruvs.backend.exception.ResourceNotFoundException;
import com.cruvs.backend.repository.InboxItemRepository;
import com.cruvs.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InboxService {
    private final InboxItemRepository inboxRepo;
    private final UserRepository userRepo;
    private final StorageService storageService;

    @Transactional
    public InboxItemResponse capture(UUID userId, byte[] fileBytes, String textEncrypted,String encryptedDek,Long size, String mimeType ){
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User",userId));
        String contentType = "TEXT";
        String storageKey = null;

        if (fileBytes !=null && fileBytes.length > 0 ){
            contentType = (textEncrypted !=null && !textEncrypted.isEmpty()) ? "MIXED" : "FILE";
            storageKey = "inbox/" + userId +"/" + UUID.randomUUID();
            storageService.uploadFile(storageKey,fileBytes,"application/octet-stream");
        }

        InboxItem item = InboxItem.builder()
                .user(user)
                .contentType(contentType)
                .textContentEncrypted(textEncrypted)
                .fileStorageKey(storageKey)
                .fileSizeBytes(size)
                .encryptedDek(encryptedDek)
                .mimeType(mimeType)
                .build();

        return mapToResponse(inboxRepo.save(item));

    }
    public List<InboxItemResponse> listUnprocessed(UUID userId){
        User user = userRepo.findById(userId).orElseThrow(()->new ResourceNotFoundException("User",userId));
        return inboxRepo.findByUserAndStatus(user,"unprocessed",
                Sort.by(Sort.Direction.DESC,"capturedAt")).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void delete(UUID userId, UUID itemId){
        InboxItem item = inboxRepo.findById(itemId).orElseThrow(()-> new ResourceNotFoundException("InboxItem",itemId));
        if (!item.getUser().getId().equals(userId)) throw new AccessDeniedException("Access denied to inbox item: "+itemId);
        if (item.getFileStorageKey() !=null) storageService.deleteFile(item.getFileStorageKey());

        inboxRepo.delete(item);
    }

    @Transactional
    public void markAsProcessed(UUID userId, UUID itemId, String targetType, UUID targetId){
        InboxItem item = inboxRepo.findById(itemId).orElseThrow();
        if (!item.getUser().getId().equals(userId)) throw new AccessDeniedException("Access denied to inbox item: " + itemId);

        item.setStatus("processed");
        item.setProcessedToType(targetType);
        item.setProcessedToId(targetId);
        item.setProcessedAt(LocalDateTime.now());
        inboxRepo.save(item);

    }

    public byte[] downloadFile(UUID userId, UUID itemId){
        InboxItem item = inboxRepo.findById(itemId).orElseThrow(()->new ResourceNotFoundException("InboxItem",itemId));

        if (!item.getUser().getId().equals(userId)) throw new AccessDeniedException("Access denied to inbox item: " + itemId);
        return storageService.downloadFile(item.getFileStorageKey());
    }


    private InboxItemResponse mapToResponse(InboxItem item){
        return InboxItemResponse.builder()
                .id(item.getId())
                .contentType(item.getContentType())
                .textContentEncrypted(item.getTextContentEncrypted())
                .encryptedDek(item.getEncryptedDek())
                .fileSizeBytes(item.getFileSizeBytes())
                .mimeType(item.getMimeType())
                .status(item.getStatus())
                .capturedAt(item.getCapturedAt())
                .build();
    }
}
