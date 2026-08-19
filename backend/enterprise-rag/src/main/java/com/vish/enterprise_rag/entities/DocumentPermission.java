package com.vish.enterprise_rag.entities;

import java.time.LocalDateTime;

import com.vish.enterprise_rag.enums.DocumentPermissionType;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(
    name = "document_permissions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_document_permissions_document_user",
        columnNames = {"document_id", "user_id"}
    )
)
@Data
@ToString
public class DocumentPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    private DocumentPermissionType permission;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
