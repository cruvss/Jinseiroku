CREATE TABLE reminder_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    source_type VARCHAR(20) NOT NULL,
    source_id UUID NOT NULL,
    target_date DATE NOT NULL,
    lead_time_days INTEGER DEFAULT 7,
    reminder_offsets JSONB DEFAULT '[-30, -7, -1]',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT NOW()
);
CREATE TABLE scheduled_notifications (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     reminder_rule_id UUID REFERENCES reminder_rules(id) ON DELETE CASCADE,
     user_id UUID REFERENCES users(id) ON DELETE CASCADE,
     scheduled_for TIMESTAMP NOT NULL,
     channel VARCHAR(20) DEFAULT 'in_app',
     title VARCHAR(500),
     body TEXT,
     status VARCHAR(20) DEFAULT 'pending',
     sent_at TIMESTAMP
);