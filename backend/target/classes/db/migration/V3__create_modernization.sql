-- =====================================================
-- V3: Create modernization tables
-- =====================================================

CREATE TABLE modernization_runs (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id          UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    analysis_run_id     UUID,
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_suggestions   INTEGER DEFAULT 0,
    applied_suggestions INTEGER DEFAULT 0,
    llm_metadata        JSONB,
    started_at          TIMESTAMP WITH TIME ZONE,
    completed_at        TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_modernization_runs_project ON modernization_runs(project_id);

CREATE TABLE modernization_suggestions (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    modernization_run_id    UUID NOT NULL REFERENCES modernization_runs(id) ON DELETE CASCADE,
    strategy_type           VARCHAR(100) NOT NULL,
    file_path               VARCHAR(500) NOT NULL,
    original_code           TEXT,
    modernized_code         TEXT,
    explanation             TEXT,
    confidence_score        REAL DEFAULT 0.0,
    risk_level              VARCHAR(20) DEFAULT 'MEDIUM',
    applied                 BOOLEAN DEFAULT FALSE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_mod_suggestions_run ON modernization_suggestions(modernization_run_id);
CREATE INDEX idx_mod_suggestions_risk ON modernization_suggestions(risk_level);
