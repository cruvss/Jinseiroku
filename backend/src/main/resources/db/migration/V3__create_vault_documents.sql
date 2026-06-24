CREATE TABLE vault_documents (
     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
     user_id UUID REFERENCES users(id) ON DELETE CASCADE,
     file_name_encrypted TEXT NOT NULL,
     category VARCHAR(50) NOT NULL,
     tags_encrypted TEXT,
     notes_encrypted TEXT,
     blob_storage_key VARCHAR(500) NOT NULL,
     encrypted_dek TEXT NOT NULL,
     file_size_bytes BIGINT NOT NULL,
     mime_type VARCHAR(100),
     expiry_date DATE,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);