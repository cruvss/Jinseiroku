package com.cruvs.backend.dto.atuh;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaultParamsResponse {
    private String encryptionSalt;
    private String encryptedKekVerification;
}
