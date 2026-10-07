-- =====================================================
-- V4: Create scoring tables
-- =====================================================

CREATE TABLE debt_scores (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id              UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    analysis_run_id         UUID,
    overall_score           REAL NOT NULL DEFAULT 0.0,
    maintainability_score   REAL DEFAULT 0.0,
    security_score          REAL DEFAULT 0.0,
    performance_score       REAL DEFAULT 0.0,
    modernity_score         REAL DEFAULT 0.0,
    grade                   VARCHAR(2),
    breakdown               JSONB,
    calculated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_debt_scores_project ON debt_scores(project_id);

CREATE TABLE score_trends (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id          UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    overall_score       REAL NOT NULL,
    maintainability     REAL DEFAULT 0.0,
    security            REAL DEFAULT 0.0,
    performance         REAL DEFAULT 0.0,
    modernity           REAL DEFAULT 0.0,
    recorded_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_score_trends_project ON score_trends(project_id);
CREATE INDEX idx_score_trends_recorded_at ON score_trends(recorded_at);

CREATE TABLE generated_tests (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id              UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    modernization_run_id    UUID,
    target_class            VARCHAR(500) NOT NULL,
    target_method           VARCHAR(255),
    test_code               TEXT NOT NULL,
    test_framework          VARCHAR(50) DEFAULT 'JUnit5',
    coverage_estimate       REAL DEFAULT 0.0,
    mutation_score          REAL DEFAULT 0.0,
    generated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_generated_tests_project ON generated_tests(project_id);
