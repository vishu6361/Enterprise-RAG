-- ==============================================================================
-- Enterprise RAG Platform - Initial Schema Migration (V1)
-- ==============================================================================

-- 1. Organizations table
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 2. Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    designation VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT uk_users_organization_email UNIQUE (organization_id, email)
);

CREATE INDEX idx_users_org_id ON users(organization_id);

-- 3. Documents table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    document_name VARCHAR(256) NOT NULL,
    extension VARCHAR(255),
    content_hash VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    error_message VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_documents_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_documents_owner FOREIGN KEY (owner_id) REFERENCES users(id),
    CONSTRAINT uk_documents_organization_content_hash UNIQUE (organization_id, content_hash)
);

CREATE INDEX idx_documents_org_id ON documents(organization_id);
CREATE INDEX idx_documents_owner_id ON documents(owner_id);
CREATE INDEX idx_documents_status ON documents(status);

-- 4. Document Permissions table
CREATE TABLE document_permissions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    permission VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_doc_perm_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_doc_perm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_document_permissions_document_user UNIQUE (document_id, user_id)
);

CREATE INDEX idx_doc_perm_user_id ON document_permissions(user_id);
CREATE INDEX idx_doc_perm_document_id ON document_permissions(document_id);

-- 5. Document Ownership History table
CREATE TABLE document_ownership_history (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    old_user_id BIGINT NOT NULL,
    new_user_id BIGINT NOT NULL,
    ownership_changed_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_doc_hist_document FOREIGN KEY (document_id) REFERENCES documents(id),
    CONSTRAINT fk_doc_hist_old_user FOREIGN KEY (old_user_id) REFERENCES users(id),
    CONSTRAINT fk_doc_hist_new_user FOREIGN KEY (new_user_id) REFERENCES users(id)
);

CREATE INDEX idx_doc_hist_document_id ON document_ownership_history(document_id);

-- 6. Audit Logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    table_name VARCHAR(255) NOT NULL,
    table_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    details VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_audit_logs_org_id ON audit_logs(organization_id);
CREATE INDEX idx_audit_logs_table ON audit_logs(table_name, table_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- 7. Conversations table
CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    sender_user_id BIGINT NOT NULL,
    receiver_user_id BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_conversations_sender FOREIGN KEY (sender_user_id) REFERENCES users(id),
    CONSTRAINT fk_conversations_receiver FOREIGN KEY (receiver_user_id) REFERENCES users(id)
);

CREATE INDEX idx_conversations_sender ON conversations(sender_user_id);
CREATE INDEX idx_conversations_receiver ON conversations(receiver_user_id);

-- 8. Messages table
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    content VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    attachment_url VARCHAR(255),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
