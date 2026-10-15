import { api } from './client';

export interface Project {
  id: string;
  name: string;
  description: string;
  sourceUrl: string;
  githubRepoUrl: string;
  javaVersionDetected: string;
  targetJavaVersion: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface AnalysisRun {
  id: string;
  projectId: string;
  status: string;
  totalFiles: number;
  filesAnalyzed: number;
  issuesFound: number;
  summary: {
    categoryCounts?: Record<string, number>;
    severityCounts?: Record<string, number>;
  };
  startedAt: string;
  completedAt: string;
}

export interface CodeIssue {
  id: string;
  analysisRunId: string;
  filePath: string;
  lineNumber: number;
  endLineNumber: number;
  category: string;
  severity: string;
  patternType: string;
  description: string;
  originalCode: string;
  suggestedFix: string;
  autoFixable: boolean;
}

export interface ModernizationRun {
  id: string;
  projectId: string;
  analysisRunId: string;
  status: string;
  totalSuggestions: number;
  appliedSuggestions: number;
  startedAt: string;
  completedAt: string;
}

export interface ModernizationSuggestion {
  id: string;
  modernizationRunId: string;
  strategyType: string;
  filePath: string;
  originalCode: string;
  modernizedCode: string;
  explanation: string;
  confidenceScore: number;
  riskLevel: string;
  applied: boolean;
}

export interface DebtScore {
  id: string;
  projectId: string;
  analysisRunId: string;
  overallScore: number;
  maintainabilityScore: number;
  securityScore: number;
  performanceScore: number;
  modernityScore: number;
  breakdown: Record<string, any>;
  calculatedAt: string;
}

export interface ScoreTrend {
  id: string;
  projectId: string;
  overallScore: number;
  maintainability: number;
  security: number;
  performance: number;
  modernity: number;
  recordedAt: string;
}

export interface GeneratedTest {
  id: string;
  projectId: string;
  targetClass: string;
  targetMethod: string;
  testCode: string;
  testFramework: string;
  coverageEstimate: number;
  mutationScore: number;
  generatedAt: string;
}

export interface GitHubIntegration {
  id: string;
  projectId: string;
  repoOwner: string;
  repoName: string;
  branchName: string;
  prNumber: string;
  prUrl: string;
  prStatus: string;
  prDescription: string;
  createdAt: string;
}

// ================= API CALLS =================

export const getProjects = async (): Promise<Project[]> => {
  const res = await api.get('/projects');
  return res.data.data;
};

export const getProject = async (id: string): Promise<Project> => {
  const res = await api.get(`/projects/${id}`);
  return res.data.data;
};

export const createProject = async (data: any): Promise<Project> => {
  const res = await api.post('/projects', data);
  return res.data.data;
};

export const uploadProjectZip = async (formData: FormData): Promise<Project> => {
  const res = await api.post('/projects/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data.data;
};

export const triggerAnalysis = async (id: string, path?: string): Promise<AnalysisRun> => {
  const res = await api.post(`/projects/${id}/analyze${path ? `?path=${encodeURIComponent(path)}` : ''}`);
  return res.data.data;
};

export const getLatestAnalysis = async (id: string): Promise<AnalysisRun> => {
  const res = await api.get(`/projects/${id}/analysis`);
  return res.data.data;
};

export const getIssues = async (id: string, category?: string, severity?: string): Promise<CodeIssue[]> => {
  let url = `/projects/${id}/issues?size=100`;
  if (category) url += `&category=${category}`;
  if (severity) url += `&severity=${severity}`;
  const res = await api.get(url);
  return res.data.data.content || res.data.data;
};

export const triggerModernization = async (id: string): Promise<ModernizationRun> => {
  const res = await api.post(`/projects/${id}/modernize`);
  return res.data.data;
};

export const getLatestModernization = async (id: string): Promise<ModernizationRun> => {
  const res = await api.get(`/projects/${id}/modernize`);
  return res.data.data;
};

export const getSuggestions = async (runId: string): Promise<ModernizationSuggestion[]> => {
  const res = await api.get(`/modernization-runs/${runId}/suggestions?size=100`);
  return res.data.data.content || res.data.data;
};

export const applySuggestion = async (suggestionId: string): Promise<ModernizationSuggestion> => {
  const res = await api.post(`/suggestions/${suggestionId}/apply`);
  return res.data.data;
};

export const calculateScore = async (id: string): Promise<DebtScore> => {
  const res = await api.post(`/projects/${id}/calculate-score`);
  return res.data.data;
};

export const getLatestScore = async (id: string): Promise<DebtScore> => {
  const res = await api.get(`/projects/${id}/score`);
  return res.data.data;
};

export const getScoreTrends = async (id: string): Promise<ScoreTrend[]> => {
  const res = await api.get(`/projects/${id}/score-trend`);
  return res.data.data;
};

export const generateTests = async (id: string): Promise<GeneratedTest[]> => {
  const res = await api.post(`/projects/${id}/generate-tests`);
  return res.data.data;
};

export const getTests = async (id: string): Promise<GeneratedTest[]> => {
  const res = await api.get(`/projects/${id}/tests?size=100`);
  return res.data.data.content || res.data.data;
};

export const createPullRequest = async (id: string, repoOwner: string, repoName: string, baseBranch: string): Promise<GitHubIntegration> => {
  const res = await api.post(`/projects/${id}/github/pull-request`, { repoOwner, repoName, baseBranch });
  return res.data.data;
};

export const getIntegrations = async (id: string): Promise<GitHubIntegration[]> => {
  const res = await api.get(`/projects/${id}/github/integrations`);
  return res.data.data;
};
