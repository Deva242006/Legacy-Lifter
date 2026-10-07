-- =====================================================
-- V2: Create analysis_runs and code_issues tables
-- =====================================================

CREATE TABLE analysis_runs (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id          UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_files         INTEGER DEFAULT 0,
    files_analyzed      INTEGER DEFAULT 0,
    issues_found        INTEGER DEFAULT 0,
    summary             JSONB,
    started_at          TIMESTAMP WITH TIME ZONE,
    completed_at        TIMESTAMP WITH TIME ZONE,
    analysis_duration_ms BIGINT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_analysis_runs_project ON analysis_runs(project_id);
CREATE INDEX idx_analysis_runs_status ON analysis_runs(status);

CREATE TABLE code_issues (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    analysis_run_id     UUID NOT NULL REFERENCES analysis_runs(id) ON DELETE CASCADE,
    file_path           VARCHAR(500) NOT NULL,
    line_number         INTEGER DEFAULT 0,
    end_line_number     INTEGER DEFAULT 0,
    category            VARCHAR(50) NOT NULL,
    severity            VARCHAR(20) NOT NULL,
    pattern_type        VARCHAR(100) NOT NULL,
    description         TEXT NOT NULL,
    original_code       TEXT,
    suggested_fix       TEXT,
    is_auto_fixable     BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_code_issues_analysis_run ON code_issues(analysis_run_id);
CREATE INDEX idx_code_issues_category ON code_issues(category);
CREATE INDEX idx_code_issues_severity ON code_issues(severity);
