import React, { useEffect, useState, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { FileSearch, ChevronLeft, ChevronRight, ShieldCheck } from 'lucide-react';
import { getIssues, CodeIssue } from '../api/projects';

const PAGE_SIZE = 20;

const CATEGORIES = [
  'RAW_TYPES', 'ANONYMOUS_CLASSES', 'OLD_CONCURRENCY', 'BLOCKING_IO',
  'MISSING_RECORDS', 'MISSING_SEALED_CLASSES', 'OLD_SWITCH', 'STREAM_IMPROVEMENTS', 'OPTIONAL_MISUSE'
];
const SEVERITIES = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO'];

export const AnalysisView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [issues, setIssues] = useState<CodeIssue[]>([]);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedSeverity, setSelectedSeverity] = useState('');
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const loadIssues = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      // Use raw api call to get paged response
      const { api } = await import('../api/client');
      let url = `/projects/${id}/issues?page=${page}&size=${PAGE_SIZE}`;
      if (selectedCategory) url += `&category=${selectedCategory}`;
      if (selectedSeverity) url += `&severity=${selectedSeverity}`;
      const res = await api.get(url);
      const paged = res.data.data;
      if (paged && paged.content !== undefined) {
        setIssues(paged.content);
        setTotalPages(paged.totalPages ?? 1);
        setTotalElements(paged.totalElements ?? paged.content.length);
      } else {
        // fallback: flat array
        setIssues(Array.isArray(paged) ? paged : []);
        setTotalPages(1);
        setTotalElements(Array.isArray(paged) ? paged.length : 0);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }, [id, selectedCategory, selectedSeverity, page]);

  useEffect(() => {
    loadIssues();
  }, [loadIssues]);

  // Reset to page 0 when filters change
  useEffect(() => {
    setPage(0);
  }, [selectedCategory, selectedSeverity]);

  const severityColor: Record<string, string> = {
    CRITICAL: 'var(--accent-rose)',
    HIGH: 'var(--accent-amber)',
    MEDIUM: 'var(--accent-cyan)',
    LOW: 'var(--accent-emerald)',
    INFO: 'var(--text-muted)',
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div className="glass-card" style={{ padding: '1.75rem', marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <FileSearch color="var(--accent-cyan)" /> Deep Static AST Analysis
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.25rem' }}>
            Parsed with JavaParser &amp; Spoon • <strong>{totalElements}</strong> issues found
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            style={{ padding: '0.5rem 1rem', borderRadius: '8px', background: '#0d1117', border: '1px solid var(--border-color)', color: '#fff', fontSize: '0.85rem' }}
          >
            <option value="">All Categories</option>
            {CATEGORIES.map((c) => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
          </select>

          <select
            value={selectedSeverity}
            onChange={(e) => setSelectedSeverity(e.target.value)}
            style={{ padding: '0.5rem 1rem', borderRadius: '8px', background: '#0d1117', border: '1px solid var(--border-color)', color: '#fff', fontSize: '0.85rem' }}
          >
            <option value="">All Severities</option>
            {SEVERITIES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
      </div>

      {loading ? (
        <div style={{ padding: '2rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{ width: '20px', height: '20px', border: '2px solid var(--primary)', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} />
          Analyzing code issues...
        </div>
      ) : issues.length === 0 ? (
        <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
          <ShieldCheck size={48} color="var(--accent-emerald)" style={{ marginBottom: '1rem' }} />
          <h3>No issues detected for selected filter!</h3>
          <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>Try clearing category/severity filters or run analysis first.</p>
        </div>
      ) : (
        <>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.875rem' }}>
            {issues.map((issue) => (
              <div key={issue.id} className="glass-card" style={{ padding: '1.25rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                    <span className={`badge badge-${issue.severity.toLowerCase()}`}>{issue.severity}</span>
                    <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--accent-cyan)', fontFamily: 'var(--font-mono)' }}>
                      {issue.filePath}:{issue.lineNumber}
                    </span>
                  </div>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', background: 'rgba(255,255,255,0.04)', padding: '0.2rem 0.6rem', borderRadius: '6px' }}>
                    {issue.category?.replace(/_/g, ' ')}
                  </span>
                </div>

                <p style={{ fontSize: '0.95rem', fontWeight: 500, marginBottom: '0.75rem' }}>{issue.description}</p>

                {issue.originalCode && (
                  <pre className="code-block" style={{ marginTop: '0.5rem', maxHeight: '200px', overflowY: 'auto' }}>
                    <code>{issue.originalCode}</code>
                  </pre>
                )}

                {issue.suggestedFix && (
                  <div style={{ marginTop: '0.75rem', padding: '0.75rem', background: 'rgba(16,185,129,0.06)', borderRadius: '8px', borderLeft: '3px solid var(--accent-emerald)' }}>
                    <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--accent-emerald)', marginBottom: '0.35rem' }}>SUGGESTED FIX</div>
                    <pre className="code-block" style={{ background: 'transparent', padding: 0, border: 'none', maxHeight: '150px', overflowY: 'auto' }}>
                      <code style={{ color: '#6ee7b7' }}>{issue.suggestedFix}</code>
                    </pre>
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', marginTop: '2rem' }}>
              <button
                className="btn-secondary"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                style={{ padding: '0.5rem 0.875rem' }}
              >
                <ChevronLeft size={16} /> Prev
              </button>

              <div style={{ display: 'flex', gap: '0.4rem' }}>
                {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => {
                  const pageNum = totalPages <= 7 ? i : Math.max(0, Math.min(page - 3, totalPages - 7)) + i;
                  return (
                    <button
                      key={pageNum}
                      onClick={() => setPage(pageNum)}
                      style={{
                        width: '36px', height: '36px', borderRadius: '8px', border: pageNum === page ? 'none' : '1px solid var(--border-color)',
                        background: pageNum === page ? 'var(--primary)' : 'rgba(255,255,255,0.04)',
                        color: '#fff', fontWeight: pageNum === page ? 700 : 400, cursor: 'pointer', fontSize: '0.875rem'
                      }}
                    >
                      {pageNum + 1}
                    </button>
                  );
                })}
              </div>

              <button
                className="btn-secondary"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                style={{ padding: '0.5rem 0.875rem' }}
              >
                Next <ChevronRight size={16} />
              </button>

              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                Page {page + 1} of {totalPages} ({totalElements} total)
              </span>
            </div>
          )}
        </>
      )}

      <style>{`
        @keyframes spin { to { transform: rotate(360deg); } }
      `}</style>
    </div>
  );
};
