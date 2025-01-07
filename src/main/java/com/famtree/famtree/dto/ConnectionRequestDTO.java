package com.famtree.famtree.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequestDTO {
    private Long requestId;
    private FamilyBasicInfo requesterFamily;
    private FamilyBasicInfo receiverFamily;
    private String status;
    private LocalDateTime createdAt;
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FamilyBasicInfo {
        private String familyUid;
        private String familyName;
    }
} 