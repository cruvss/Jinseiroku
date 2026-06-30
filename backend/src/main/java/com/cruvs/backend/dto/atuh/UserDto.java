package com.cruvs.backend.dto.atuh;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String timezone;
    private LocalDateTime createdAt;
}