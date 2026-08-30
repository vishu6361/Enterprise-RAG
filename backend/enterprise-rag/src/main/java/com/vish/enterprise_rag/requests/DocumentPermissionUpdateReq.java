package com.vish.enterprise_rag.requests;

import com.vish.enterprise_rag.enums.DocumentPermissionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DocumentPermissionUpdateReq {
    private Long documentId;
    private Long userId;
    private DocumentPermissionType permissionType;
}
