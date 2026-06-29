CREATE TABLE tasks (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
   title_encrypted TEXT NOT NULL,
   description_encrypted TEXT,
   category VARCHAR(50) NOT NULL,
   is_recurring BOOLEAN DEFAULT FALSE,
   cycle_type VARCHAR(20),
   cycle_interval INTEGER,
   due_date DATE,
   lead_time_days INTEGER DEFAULT 7,
   status VARCHAR(20) DEFAULT 'pending',
   linked_document_id UUID REFERENCES vault_documents(id) ON DELETE SET NULL,
   created_at TIMESTAMP DEFAULT NOW(),
   updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE task_completions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
  completed_at TIMESTAMP DEFAULT NOW(),
  notes_encrypted TEXT
);