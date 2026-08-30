package com.vish.enterprise_rag.service;

import com.vish.enterprise_rag.enums.UserActionType;

public interface AuditService {
    <T> void audit(UserActionType actionType, T entity, String details);
    <T> void audit(UserActionType actionType, T entity, Long userId, String details);
}
