package com.vish.enterprise_rag.repositories.read;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vish.enterprise_rag.entities.AuditLog;
import com.vish.enterprise_rag.enums.UserActionType;

public interface AuditLogReadRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByOrganizationIdOrderByTimestampDesc(Long organizationId);

    List<AuditLog> findByTableNameAndTableIdOrderByTimestampDesc(String tableName, Long tableId);

    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    List<AuditLog> findByOrganizationIdAndActionOrderByTimestampDesc(Long organizationId, UserActionType action);
}
