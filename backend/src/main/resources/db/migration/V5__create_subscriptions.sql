CREATE TABLE subscriptions (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       user_id UUID REFERENCES users(id) ON DELETE CASCADE,
       name VARCHAR(255) NOT NULL,
       cost DECIMAL(10,2) NOT NULL,
       currency VARCHAR(5) DEFAULT 'NPR',
       billing_cycle VARCHAR(20) NOT NULL,
       next_billing_date DATE NOT NULL,
       status VARCHAR(20) DEFAULT 'active',
       linked_document_id UUID REFERENCES vault_documents(id),
       created_at TIMESTAMP DEFAULT NOW(),
       updated_at TIMESTAMP DEFAULT NOW()
);
