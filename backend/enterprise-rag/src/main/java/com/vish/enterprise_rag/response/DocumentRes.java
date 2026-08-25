package com.vish.enterprise_rag.response;

import java.time.LocalDateTime;

import com.vish.enterprise_rag.enums.DocumentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRes {
    private Long id;
    private String documentName;
    private String contentHash;
    private String extension;
    private DocumentStatus status;
    private Long organizationId;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
