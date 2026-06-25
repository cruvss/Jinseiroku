package com.cruvs.backend.service;


import com.cruvs.backend.dto.dashboard.DashboardStatsResponse;
import com.cruvs.backend.entity.User;
import com.cruvs.backend.repository.InboxItemRepository;
import com.cruvs.backend.repository.UserRepository;
import com.cruvs.backend.repository.VaultDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final InboxItemRepository inboxRepo;
    private final VaultDocumentRepository vaultRepo;
    private final UserRepository userRepo;

    public DashboardStatsResponse getStats(UUID userId){
        User user = userRepo.findById(userId).orElseThrow();
        return DashboardStatsResponse.builder()
                .unprocessedInboxCount(inboxRepo.countByUserAndStatus(user,"unprocessed"))
                .vaultDocumentCount(vaultRepo.countByUser(user))
                .build();
    }
}
