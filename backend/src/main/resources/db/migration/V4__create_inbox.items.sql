-- V4__create_inbox_items.sql
CREATE TABLE inbox_items (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     user_id UUID REFERENCES users(id) ON DELETE CASCADE,
     content_type VARCHAR(20) NOT NULL,
     text_content_encrypted TEXT,
     file_storage_key VARCHAR(500),
     encrypted_dek TEXT,
     file_size_bytes BIGINT,
     mime_type VARCHAR(100),
     status VARCHAR(20) DEFAULT 'unprocessed',
     processed_to_type VARCHAR(20),
     processed_to_id UUID,
     captured_at TIMESTAMP DEFAULT NOW(),
     processed_at TIMESTAMP
);
