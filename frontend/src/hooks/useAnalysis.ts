import { useState, useEffect, useCallback } from 'react';
import {
  getLatestAnalysis,
  getIssues,
  triggerAnalysis,
  type AnalysisRun,
  type CodeIssue,
} from '../api/projects';

/**
 * useAnalysis – wraps the analysis state for a given project.
 * - Loads the latest run + issues on mount
 * - Exposes `run()` to trigger a new analysis
 * - Provides polling during ANALYZING status
 */
export function useAnalysis(projectId: string | undefined) {
  const [run, setRun]         = useState<AnalysisRun | null>(null);
  const [issues, setIssues]   = useState<CodeIssue[]>([]);
  const [loading, setLoading] = useState(true);
  const [running, setRunning] = useState(false);
  const [error, setError]     = useState<string | null>(null);

  const fetchRun = useCallback(async () => {
    if (!projectId) return;
    try {
      const data = await getLatestAnalysis(projectId);
      setRun(data);
      return data;
    } catch {
      // No analysis run yet – that's OK
      return null;
    }
  }, [projectId]);

  const fetchIssues = useCallback(async () => {
    if (!projectId) return;
    try {
      const data = await getIssues(projectId);
      setIssues(data ?? []);
    } catch {
      setIssues([]);
    }
  }, [projectId]);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    await Promise.all([fetchRun(), fetchIssues()]);
    setLoading(false);
  }, [fetchRun, fetchIssues]);

  useEffect(() => { refresh(); }, [refresh]);

  // Poll while analysis is in progress
  useEffect(() => {
    if (run?.status !== 'RUNNING') return;
    const id = setInterval(async () => {
      const updated = await fetchRun();
      if (updated?.status !== 'RUNNING') {
        clearInterval(id);
        await fetchIssues();
      }
    }, 3000);
    return () => clearInterval(id);
  }, [run?.status, fetchRun, fetchIssues]);

  const triggerRun = useCallback(async (path?: string) => {
    if (!projectId) return;
    setRunning(true);
    setError(null);
    try {
      const data = await triggerAnalysis(projectId, path);
      setRun(data);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e.message ?? 'Analysis failed');
    } finally {
      setRunning(false);
    }
  }, [projectId]);

  return {
    run,
    issues,
    loading,
    running,
    error,
    refresh,
    triggerRun,
  };
}
