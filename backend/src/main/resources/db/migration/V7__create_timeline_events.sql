CREATE TABLE timeline_events (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     user_id UUID REFERENCES users(id) ON DELETE CASCADE,
     title_encrypted TEXT NOT NULL,
     description_encrypted TEXT,
     event_date DATE NOT NULL,
     end_date DATE,
     category VARCHAR(20) NOT NULL,
     linked_document_ids UUID[],
     created_at TIMESTAMP DEFAULT NOW(),
     updated_at TIMESTAMP DEFAULT NOW()
);