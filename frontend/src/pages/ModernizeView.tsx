import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Sparkles, CheckCircle2, RefreshCw } from 'lucide-react';
import { getLatestModernization, getSuggestions, applySuggestion, ModernizationSuggestion, ModernizationRun } from '../api/projects';
import { DiffViewer } from '../components/modernization/DiffViewer';

export const ModernizeView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [run, setRun] = useState<ModernizationRun | null>(null);
  const [suggestions, setSuggestions] = useState<ModernizationSuggestion[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) loadData();
  }, [id]);

  const loadData = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const r = await getLatestModernization(id);
      setRun(r);
      const list = await getSuggestions(r.id);
      setSuggestions(list);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleApply = async (suggestionId: string) => {
    try {
      await applySuggestion(suggestionId);
      setSuggestions((prev) =>
        prev.map((s) => (s.id === suggestionId ? { ...s, applied: true } : s))
      );
    } catch (e) {
      alert('Failed to apply suggestion');
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div className="glass-card" style={{ padding: '1.75rem', marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <span className="badge badge-medium" style={{ marginBottom: '0.5rem' }}>
            <Sparkles size={14} /> RAG-GROUNDED AI TRANSFORMATIONS
          </span>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Java 21 Code Modernizations</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Before and after refactoring diffs powered by Spring AI & JDK migration best practices.
          </p>
        </div>

        {run && (
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--accent-emerald)' }}>
              {suggestions.filter((s) => s.applied).length} / {suggestions.length} Applied
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Status: {run.status}</div>
          </div>
        )}
      </div>

      {loading ? (
        <div style={{ padding: '2rem', color: 'var(--text-muted)' }}>Generating code modernizations...</div>
      ) : suggestions.length === 0 ? (
        <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
          <Sparkles size={48} color="var(--primary)" style={{ marginBottom: '1rem' }} />
          <h3>No modernization suggestions generated yet.</h3>
          <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>Trigger AI Modernization from the Project Dashboard.</p>
        </div>
      ) : (
        <div>
          {suggestions.map((s) => (
            <DiffViewer
              key={s.id}
              filePath={s.filePath}
              originalCode={s.originalCode}
              modernizedCode={s.modernizedCode}
              explanation={s.explanation}
              strategyType={s.strategyType}
              confidenceScore={s.confidenceScore}
              riskLevel={s.riskLevel}
              applied={s.applied}
              onApply={() => handleApply(s.id)}
            />
          ))}
        </div>
      )}
    </div>
  );
};
