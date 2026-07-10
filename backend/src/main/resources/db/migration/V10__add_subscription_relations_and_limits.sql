ALTER TABLE subscription_plan ADD COLUMN max_attachment_size_bytes BIGINT;
ALTER TABLE subscription_plan ADD COLUMN max_vault_size_bytes BIGINT;

--Free plan
UPDATE subscription_plan SET max_attachment_size_bytes=1048576,
                             max_vault_size_bytes=262144000
WHERE id = 'b199d750-a9cf-4bc1-9f93-4a6c8e310001';

--Eco plan
UPDATE subscription_plan SET max_attachment_size_bytes = 26214400,
                              max_vault_size_bytes = 1073741824
WHERE id = 'b199d750-a9cf-4bc1-9f93-4a6c8e310002';

--Plus plan
UPDATE subscription_plan SET max_attachment_size_bytes = 104857600,
                              max_vault_size_bytes = 5368709120
WHERE id = 'b199d750-a9cf-4bc1-9f93-4a6c8e310003';

--Pro plan
UPDATE subscription_plan SET max_attachment_size_bytes = 1073741824,
                              max_vault_size_bytes = 536870912000
WHERE id = 'b199d750-a9cf-4bc1-9f93-4a6c8e310004';


--by default free plan for user
ALTER TABLE users ADD COLUMN subscription_plan_id UUID REFERENCES subscription_plan(id)
DEFAULT 'b199d750-a9cf-4bc1-9f93-4a6c8e310001';