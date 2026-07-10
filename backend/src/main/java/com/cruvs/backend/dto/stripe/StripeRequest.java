package com.cruvs.backend.dto.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StripeRequest {
    private Long amount;
    private Long quantity;
    private String name;
    private String currency;
    private UUID planId;
}