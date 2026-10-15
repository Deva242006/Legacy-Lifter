import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { FileSearch, Filter, AlertTriangle, ShieldCheck, CheckCircle2 } from 'lucide-react';
import { getIssues, CodeIssue } from '../api/projects';

export const AnalysisView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [issues, setIssues] = useState<CodeIssue[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [selectedSeverity, setSelectedSeverity] = useState<string>('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) loadIssues();
  }, [id, selectedCategory, selectedSeverity]);

  const loadIssues = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await getIssues(id, selectedCategory || undefined, selectedSeverity || undefined);
      setIssues(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const categories = [
    'RAW_TYPES', 'ANONYMOUS_CLASSES', 'OLD_CONCURRENCY', 'BLOCKING_IO',
    'MISSING_RECORDS', 'MISSING_SEALED_CLASSES', 'OLD_SWITCH', 'STREAM_IMPROVEMENTS', 'OPTIONAL_MISUSE'
  ];

  const severities = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO'];

  return (
    <div style={{ padding: '2rem' }}>
      <div className="glass-card" style={{ padding: '1.75rem', marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <FileSearch color="var(--accent-cyan)" /> Deep Static AST Analysis
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Parsed with JavaParser & Spoon to identify legacy idioms replaceable by Java 21+ features.
          </p>
        </div>

        {/* Filters */}
        <div style={{ display: 'flex', gap: '1rem' }}>
          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            style={{ padding: '0.5rem 1rem', borderRadius: '8px', background: '#0d1117', border: '1px solid var(--border-color)', color: '#fff' }}
          >
            <option value="">All Categories</option>
            {categories.map((c) => <option key={c} value={c}>{c}</option>)}
          </select>

          <select
            value={selectedSeverity}
            onChange={(e) => setSelectedSeverity(e.target.value)}
            style={{ padding: '0.5rem 1rem', borderRadius: '8px', background: '#0d1117', border: '1px solid var(--border-color)', color: '#fff' }}
          >
            <option value="">All Severities</option>
            {severities.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
      </div>

      {/* Issues Table */}
      {loading ? (
        <div style={{ padding: '2rem', color: 'var(--text-muted)' }}>Analyzing code issues...</div>
      ) : issues.length === 0 ? (
        <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
          <ShieldCheck size={48} color="var(--accent-emerald)" style={{ marginBottom: '1rem' }} />
          <h3>No issues detected for selected filter!</h3>
          <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>Try clearing category/severity filters.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {issues.map((issue) => (
            <div key={issue.id} className="glass-card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span className={`badge badge-${issue.severity.toLowerCase()}`}>{issue.severity}</span>
                  <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--accent-cyan)' }}>{issue.filePath}:{issue.lineNumber}</span>
                </div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{issue.patternType}</span>
              </div>

              <p style={{ fontSize: '0.95rem', fontWeight: 500, marginBottom: '0.75rem' }}>{issue.description}</p>

              {issue.originalCode && (
                <pre className="code-block" style={{ marginTop: '0.5rem' }}>
                  <code>{issue.originalCode}</code>
                </pre>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
