package com.vish.enterprise_rag.mappers;

import org.springframework.stereotype.Component;

import com.vish.enterprise_rag.entities.Document;
import com.vish.enterprise_rag.response.DocumentRes;

@Component
public class DocumentMapper {

    public DocumentRes toRes(Document document) {
        if (document == null) {
            return null;
        }
        return DocumentRes.builder()
                .id(document.getId())
                .documentName(document.getDocumentName())
                .contentHash(document.getContentHash())
                .extension(document.getExtension())
                .status(document.getStatus())
                .organizationId(document.getOrganization() != null ? document.getOrganization().getId() : null)
                .ownerId(document.getOwner() != null ? document.getOwner().getId() : null)
                .ownerName(document.getOwner() != null ? document.getOwner().getName() : null)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
