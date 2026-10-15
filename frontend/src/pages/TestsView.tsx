import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { TestTube, CheckCircle2, ShieldCheck, Code2 } from 'lucide-react';
import { getTests, generateTests, GeneratedTest } from '../api/projects';

export const TestsView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [tests, setTests] = useState<GeneratedTest[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);

  useEffect(() => {
    if (id) loadTests();
  }, [id]);

  const loadTests = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await getTests(id);
      setTests(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerate = async () => {
    if (!id) return;
    setGenerating(true);
    try {
      const data = await generateTests(id);
      setTests(data);
    } catch (e) {
      alert('Test generation failed');
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div className="glass-card" style={{ padding: '1.75rem', marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <span className="badge badge-low" style={{ marginBottom: '0.5rem' }}>
            <ShieldCheck size={14} /> JUNIT 5 + ASSERTJ REGRESSION SUITE
          </span>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Auto-Generated Unit Tests</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Guarantees non-breaking behavior after applying Java 21 modernizations.
          </p>
        </div>

        <button className="btn-primary" onClick={handleGenerate} disabled={generating}>
          <TestTube size={16} /> {generating ? 'Generating Test Suite...' : 'Generate New Test Suite'}
        </button>
      </div>

      {loading ? (
        <div style={{ padding: '2rem', color: 'var(--text-muted)' }}>Loading generated test suites...</div>
      ) : tests.length === 0 ? (
        <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
          <TestTube size={48} color="var(--accent-emerald)" style={{ marginBottom: '1rem' }} />
          <h3>No unit test suites generated yet.</h3>
          <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>Click "Generate New Test Suite" above to build regression tests for modernized code.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {tests.map((t) => (
            <div key={t.id} className="glass-card" style={{ padding: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>{t.targetClass}Test.java</h3>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Framework: {t.testFramework}</span>
                </div>

                <div style={{ display: 'flex', gap: '0.75rem' }}>
                  <span className="badge badge-low">
                    Coverage: {(t.coverageEstimate * 100).toFixed(0)}%
                  </span>
                  <span className="badge badge-medium">
                    PIT Mutation: {(t.mutationScore * 100).toFixed(0)}%
                  </span>
                </div>
              </div>

              <pre className="code-block">
                <code>{t.testCode}</code>
              </pre>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
