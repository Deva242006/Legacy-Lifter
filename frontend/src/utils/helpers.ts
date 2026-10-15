// ============================================================
// Helper utilities for LegacyLifter frontend
// ============================================================

import type { IssueSeverity, ScoreGrade } from '../types';

/** Format a score (0–100) into a letter grade */
export function scoreToGrade(score: number): ScoreGrade {
  if (score >= 95) return 'A+';
  if (score >= 85) return 'A';
  if (score >= 75) return 'B';
  if (score >= 65) return 'C';
  if (score >= 55) return 'D';
  return 'F';
}

/** Map severity → CSS badge class */
export function severityToBadgeClass(severity: IssueSeverity | string): string {
  switch (severity?.toUpperCase()) {
    case 'CRITICAL': return 'badge-critical';
    case 'HIGH':     return 'badge-high';
    case 'MEDIUM':   return 'badge-medium';
    case 'LOW':      return 'badge-low';
    default:         return 'badge-low';
  }
}

/** Map score to a color string */
export function scoreToColor(score: number): string {
  if (score >= 85) return '#10b981';   // emerald
  if (score >= 70) return '#f59e0b';   // amber
  if (score >= 50) return '#f97316';   // orange
  return '#f43f5e';                    // rose
}

/** Map risk level to a badge-like color */
export function riskToColor(risk: string): string {
  switch (risk?.toUpperCase()) {
    case 'LOW':    return '#10b981';
    case 'MEDIUM': return '#f59e0b';
    case 'HIGH':   return '#f43f5e';
    default:       return '#94a3b8';
  }
}

/** Human-friendly category labels */
export const CATEGORY_LABELS: Record<string, string> = {
  RAW_TYPES:        'Raw Types',
  ANONYMOUS_CLASS:  'Anonymous Class → Lambda',
  OLD_CONCURRENCY:  'Old Concurrency',
  BLOCKING_IO:      'Blocking I/O',
  MISSING_RECORD:   'Record Candidate',
  MISSING_SEALED:   'Sealed Class Candidate',
  OLD_SWITCH:       'Old Switch',
  STREAM_IMPROVEMENT: 'Stream Improvement',
  OPTIONAL_MISUSE:  'Optional Misuse',
};

/** Format an ISO date string into a readable date */
export function formatDate(iso: string | undefined | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleString(undefined, {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

/** Format bytes into a human-readable string */
export function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1048576).toFixed(1)} MB`;
}

/** Truncate a long string */
export function truncate(str: string, max = 60): string {
  if (!str) return '';
  return str.length <= max ? str : str.slice(0, max) + '…';
}

/** Extract repo name from a GitHub URL */
export function repoNameFromUrl(url: string): string {
  if (!url) return '';
  const parts = url.replace(/\.git$/, '').split('/');
  return parts.slice(-2).join('/');
}

/** Sleep helper for demo purposes */
export const sleep = (ms: number) => new Promise(r => setTimeout(r, ms));
