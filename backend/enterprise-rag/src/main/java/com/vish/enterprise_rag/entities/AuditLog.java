package com.vish.enterprise_rag.entities;

import java.time.LocalDateTime;

import com.vish.enterprise_rag.enums.UserActionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "audit_logs")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserActionType action;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "details")
    private String details;
}
