package com.cruvs.backend.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Sub {
    private UUID id;
    private String name;
    private BigDecimal cost;
    private String currency;
    private String billingCycle;
    private LocalDate nextBillingDate;
    private String status;
    private UUID linkedDocumentId;
}
