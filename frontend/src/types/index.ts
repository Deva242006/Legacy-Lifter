// ============================================================
// Shared TypeScript types for LegacyLifter frontend
// ============================================================

export type ProjectStatus =
  | 'CREATED'
  | 'ANALYZING'
  | 'ANALYZED'
  | 'MODERNIZING'
  | 'MODERNIZED'
  | 'ERROR';

export type IssueCategory =
  | 'RAW_TYPES'
  | 'ANONYMOUS_CLASS'
  | 'OLD_CONCURRENCY'
  | 'BLOCKING_IO'
  | 'MISSING_RECORD'
  | 'MISSING_SEALED'
  | 'OLD_SWITCH'
  | 'STREAM_IMPROVEMENT'
  | 'OPTIONAL_MISUSE';

export type IssueSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export type ScoreGrade = 'A+' | 'A' | 'B' | 'C' | 'D' | 'F';

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface WebSocketMessage {
  type:
    | 'ANALYSIS_PROGRESS'
    | 'ANALYSIS_COMPLETED'
    | 'MODERNIZATION_COMPLETED'
    | 'SCORE_CALCULATED'
    | 'TESTS_GENERATED'
    | 'ERROR';
  projectId: string;
  payload: Record<string, unknown>;
  timestamp: string;
}

export interface DashboardOverview {
  totalProjects: number;
  totalAnalysisRuns: number;
  totalIssuesFound: number;
  totalSuggestionsApplied: number;
  averageDebtScore: number;
  totalPrsCreated: number;
}
