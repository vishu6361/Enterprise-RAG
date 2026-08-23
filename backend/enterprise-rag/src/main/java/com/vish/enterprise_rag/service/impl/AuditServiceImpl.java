package com.vish.enterprise_rag.service.impl;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.vish.enterprise_rag.entities.AuditLog;
import com.vish.enterprise_rag.entities.Document;
import com.vish.enterprise_rag.entities.Organization;
import com.vish.enterprise_rag.entities.User;
import com.vish.enterprise_rag.enums.UserActionType;
import com.vish.enterprise_rag.repositories.write.AuditLogWriteRepository;
import com.vish.enterprise_rag.service.AuditService;
import com.vish.enterprise_rag.utils.CommonUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogWriteRepository auditLogWriteRepository;

    @Override
    @Transactional
    public <T> void audit(UserActionType actionType, T entity, String details) {
        audit(actionType, entity, 0L, details);
    }

    @Override
    @Transactional
    public <T> void audit(UserActionType actionType, T entity, Long userId, String details) {
        if (entity == null) {
            log.warn("Cannot audit null entity for action: {}", actionType);
            return;
        }

        try {
            String tableName = CommonUtils.getTableName(entity.getClass());
            Long tableId = extractEntityId(entity);
            Long organizationId = extractOrganizationId(entity);

            AuditLog auditLog = new AuditLog();
            auditLog.setAction(actionType);
            auditLog.setTableName(tableName);
            auditLog.setTableId(tableId != null ? tableId : 0L);
            auditLog.setOrganizationId(organizationId != null ? organizationId : 0L);
            auditLog.setUserId(userId != null ? userId : 0L);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setDetails(details);

            auditLogWriteRepository.save(auditLog);
            log.info("Audit log recorded for action: {} on table: {} (id: {})", actionType, tableName, tableId);
        } catch (Exception e) {
            log.error("Failed to create audit log for action: {}", actionType, e);
        }
    }

    private <T> Long extractEntityId(T entity) {
        try {
            Method getIdMethod = entity.getClass().getMethod("getId");
            Object idObj = getIdMethod.invoke(entity);
            if (idObj instanceof Long id) {
                return id;
            }
        } catch (Exception e) {
            log.debug("Could not extract id from entity: {}", entity.getClass().getName());
        }
        return null;
    }

    private <T> Long extractOrganizationId(T entity) {
        if (entity instanceof Organization org) {
            return org.getId();
        }
        if (entity instanceof User user && user.getOrganization() != null) {
            return user.getOrganization().getId();
        }
        if (entity instanceof Document doc && doc.getOrganization() != null) {
            return doc.getOrganization().getId();
        }

        try {
            Method getOrgMethod = entity.getClass().getMethod("getOrganization");
            Object orgObj = getOrgMethod.invoke(entity);
            if (orgObj instanceof Organization org) {
                return org.getId();
            }
        } catch (Exception ignored) {
        }

        try {
            Method getOrgIdMethod = entity.getClass().getMethod("getOrganizationId");
            Object orgIdObj = getOrgIdMethod.invoke(entity);
            if (orgIdObj instanceof Long orgId) {
                return orgId;
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
