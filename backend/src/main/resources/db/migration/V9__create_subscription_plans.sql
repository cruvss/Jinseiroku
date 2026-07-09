CREATE TABLE subscription_plan(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL ,
    price DECIMAL(10,2) NOT NULL ,
    currency VARCHAR(5) DEFAULT 'USD',
    billing_cycle VARCHAR(20) DEFAULT 'month',
    subtitle VARCHAR(255) NOT NULL ,
    is_popular BOOLEAN DEFAULT FALSE,
    plan_class VARCHAR(50) DEFAULT '',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE subscription_plan_feature(
    plan_id UUID REFERENCES subscription_plan(id) ON DELETE CASCADE,
    feature VARCHAR(255) NOT NULL,
    PRIMARY KEY (plan_id, feature)
);

INSERT INTO subscription_plan(id, name, price, currency, billing_cycle, subtitle, is_popular, plan_class) VALUES
('b199d750-a9cf-4bc1-9f93-4a6c8e310001', 'Free', 0.00, 'USD', 'month', 'Begin documenting your life journey securely.', FALSE, 'current'),
('b199d750-a9cf-4bc1-9f93-4a6c8e310002', 'Eco', 5.00, 'USD', 'month', 'Keep uploading with expanded access', FALSE, 'go-plan'),
('b199d750-a9cf-4bc1-9f93-4a6c8e310003', 'Plus', 20.00, 'USD', 'month', 'Unlock the full experience', TRUE, ''),
('b199d750-a9cf-4bc1-9f93-4a6c8e310004', 'Pro', 113.00, 'USD', 'month', 'Maximize your productivity', FALSE, '');
-- Seed features for each plan
INSERT INTO subscription_plan_feature(plan_id, feature) VALUES
('b199d750-a9cf-4bc1-9f93-4a6c8e310001', 'Up to 1 MB attachments'),
('b199d750-a9cf-4bc1-9f93-4a6c8e310001', '250 MB Encrypted Vault'),

('b199d750-a9cf-4bc1-9f93-4a6c8e310002', 'Everything in Free Plan'),
('b199d750-a9cf-4bc1-9f93-4a6c8e310002', 'Up to 25 MB attachments'),
('b199d750-a9cf-4bc1-9f93-4a6c8e310002', '1 GB Encrypted Vault'),

('b199d750-a9cf-4bc1-9f93-4a6c8e310003', 'Everything in ECO Plan'),
('b199d750-a9cf-4bc1-9f93-4a6c8e310003', '5 GB Encrypted Vault'),
('b199d750-a9cf-4bc1-9f93-4a6c8e310003', 'Up to 100 MB attachments'),

('b199d750-a9cf-4bc1-9f93-4a6c8e310004', 'Everything in Plus Plan'),
('b199d750-a9cf-4bc1-9f93-4a6c8e310004', '500 GB Encrypted Vault'),
('b199d750-a9cf-4bc1-9f93-4a6c8e310004', 'Up to 1 GB attachments');
