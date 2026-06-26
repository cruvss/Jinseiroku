package com.cruvs.backend.dto.subscription;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
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
