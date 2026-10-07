-- =====================================================
-- V5: Create github integration table
-- =====================================================

CREATE TABLE github_integrations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id      UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    repo_owner      VARCHAR(255) NOT NULL,
    repo_name       VARCHAR(255) NOT NULL,
    branch_name     VARCHAR(255),
    pr_number       VARCHAR(20),
    pr_url          VARCHAR(500),
    pr_status       VARCHAR(20),
    pr_description  TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_github_integrations_project ON github_integrations(project_id);
