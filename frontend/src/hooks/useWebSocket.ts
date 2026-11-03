import { useEffect, useRef, useCallback } from 'react';

/**
 * useAnalysisPolling – polls the analysis status endpoint every `intervalMs`
 * and calls `onUpdate` when it detects a status change (COMPLETED / FAILED).
 *
 * This replaces the raw-WebSocket hook which is incompatible with the Spring
 * Boot STOMP endpoint without a dedicated STOMP client library.
 */
export function useWebSocket(
  projectId: string | undefined,
  onMessage: (msg: { type: string; payload: Record<string, unknown> }) => void
) {
  const intervalRef = useRef<ReturnType<typeof setInterval>>();
  const lastStatusRef = useRef<string>('');

  const startPolling = useCallback(() => {
    if (!projectId) return;

    intervalRef.current = setInterval(async () => {
      try {
        const res = await fetch(`/api/v1/projects/${projectId}/analysis`);
        if (!res.ok) return;
        const json = await res.json();
        const status: string = json?.data?.status ?? '';
        if (status && status !== lastStatusRef.current) {
          lastStatusRef.current = status;
          if (status === 'COMPLETED') {
            onMessage({ type: 'ANALYSIS_COMPLETED', payload: json.data });
          } else if (status === 'FAILED' || status === 'ERROR') {
            onMessage({ type: 'ERROR', payload: { message: 'Analysis failed', ...json.data } });
          } else if (status === 'RUNNING') {
            onMessage({ type: 'ANALYSIS_PROGRESS', payload: json.data });
          }
        }
      } catch {
        // network error — keep polling silently
      }
    }, 3000);
  }, [projectId, onMessage]);

  useEffect(() => {
    startPolling();
    return () => clearInterval(intervalRef.current);
  }, [startPolling]);

  return { send: (_payload: object) => {} };
}
