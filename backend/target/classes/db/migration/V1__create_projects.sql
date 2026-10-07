-- =====================================================
-- V1: Create projects table
-- =====================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

CREATE TABLE projects (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    source_url      VARCHAR(500),
    github_repo_url VARCHAR(500),
    java_version_detected VARCHAR(20),
    target_java_version   VARCHAR(20) DEFAULT '21',
    status          VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    source_path     VARCHAR(500),
    total_files     INTEGER,
    total_lines     BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_created_at ON projects(created_at DESC);
