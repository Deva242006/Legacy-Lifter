import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Play, Sparkles, TestTube, ArrowRight, RefreshCw, FileText, Activity } from 'lucide-react';
import { getProject, getLatestAnalysis, getLatestScore, getScoreTrends, triggerAnalysis, triggerModernization, calculateScore, generateTests, Project, AnalysisRun, DebtScore, ScoreTrend } from '../api/projects';
import { ScoreGauge } from '../components/scoring/ScoreGauge';
import { ScoreTrendChart } from '../components/scoring/ScoreTrendChart';
import { useWebSocket } from '../hooks/useWebSocket';

const POLLING_STATUSES = new Set(['ANALYZING', 'RUNNING', 'PENDING', 'MODERNIZING']);

export const ProjectDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [project, setProject] = useState<Project | null>(null);
  const [analysis, setAnalysis] = useState<AnalysisRun | null>(null);
  const [score, setScore] = useState<DebtScore | null>(null);
  const [trends, setTrends] = useState<ScoreTrend[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [isPolling, setIsPolling] = useState(false);

  const loadData = useCallback(async (projectId: string) => {
    try {
      const p = await getProject(projectId);
      setProject(p);

      try { const a = await getLatestAnalysis(projectId); setAnalysis(a); } catch {}
      try { const s = await getLatestScore(projectId); setScore(s); } catch {}
      try { const t = await getScoreTrends(projectId); setTrends(t); } catch {}

      // If in active state, keep polling
      if (POLLING_STATUSES.has(p.status)) {
        setIsPolling(true);
      } else {
        setIsPolling(false);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (id) loadData(id);
  }, [id, loadData]);

  // Polling while analysis/modernization is in progress
  useEffect(() => {
    if (!isPolling || !id) return;
    const interval = setInterval(() => loadData(id), 3000);
    return () => clearInterval(interval);
  }, [isPolling, id, loadData]);

  // WebSocket live updates
  useWebSocket(id, (msg) => {
    if (msg.type === 'ANALYSIS_COMPLETED' || msg.type === 'MODERNIZATION_COMPLETED') {
      if (id) loadData(id);
    }
  });

  const handleRunAnalysis = async () => {
    if (!id) return;
    setActionLoading('analysis');
    try {
      await triggerAnalysis(id);
      setIsPolling(true);
      await loadData(id);
    } catch { alert('Analysis trigger failed'); }
    finally { setActionLoading(null); }
  };

  const handleRunModernize = async () => {
    if (!id) return;
    setActionLoading('modernize');
    try {
      await triggerModernization(id);
      navigate(`/projects/${id}/modernize`);
    } catch { alert('Modernization trigger failed'); }
    finally { setActionLoading(null); }
  };

  const handleCalculateScore = async () => {
    if (!id) return;
    setActionLoading('score');
    try { await calculateScore(id); await loadData(id); }
    catch { alert('Scoring calculation failed'); }
    finally { setActionLoading(null); }
  };

  const handleGenerateTests = async () => {
    if (!id) return;
    setActionLoading('tests');
    try { await generateTests(id); navigate(`/projects/${id}/tests`); }
    catch { alert('Test generation failed'); }
    finally { setActionLoading(null); }
  };

  if (loading || !project) {
    return (
      <div style={{ padding: '3rem', display: 'flex', alignItems: 'center', gap: '1rem', color: 'var(--text-muted)' }}>
        <div style={{ width: '24px', height: '24px', border: '3px solid var(--primary)', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} />
        Loading project details...
        <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      </div>
    );
  }

  const isActive = POLLING_STATUSES.has(project.status);

  return (
    <div style={{ padding: '2rem' }}>
      {/* Header */}
      <div className="glass-card" style={{ padding: '1.75rem', marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
              <span className="badge badge-medium">{project.status}</span>
              {isActive && (
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem', color: 'var(--accent-amber)' }}>
                  <Activity size={14} style={{ animation: 'pulse 1.5s ease-in-out infinite' }} />
                  Live processing...
                </span>
              )}
            </div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>{project.name}</h1>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.25rem' }}>{project.description || 'Legacy Java Application'}</p>
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
            <button className="btn-secondary" onClick={handleRunAnalysis} disabled={actionLoading !== null || isActive}>
              <Play size={16} /> {actionLoading === 'analysis' ? 'Analyzing...' : 'Run AST Analysis'}
            </button>
            <button className="btn-primary" onClick={handleRunModernize} disabled={actionLoading !== null || isActive}>
              <Sparkles size={16} /> {actionLoading === 'modernize' ? 'Modernizing...' : 'AI Modernize'}
            </button>
          </div>
        </div>

        {/* Progress bar for active states */}
        {isActive && (
          <div style={{ marginTop: '1.25rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
              <span>
                {project.status === 'ANALYZING' && 'Running deep AST analysis with Virtual Threads...'}
                {project.status === 'MODERNIZING' && 'Generating AI modernization suggestions with RAG...'}
                {project.status === 'PENDING' && 'Queued — starting shortly...'}
                {project.status === 'RUNNING' && 'Processing...'}
              </span>
              {analysis && analysis.filesAnalyzed != null && analysis.totalFiles != null && analysis.totalFiles > 0 && (
                <span>{analysis.filesAnalyzed} / {analysis.totalFiles} files</span>
              )}
            </div>
            <div style={{ height: '6px', background: 'rgba(255,255,255,0.07)', borderRadius: '3px', overflow: 'hidden' }}>
              <div style={{
                height: '100%',
                borderRadius: '3px',
                background: 'linear-gradient(90deg, var(--primary), var(--accent-purple))',
                animation: 'progressIndeterminate 1.8s ease-in-out infinite',
                width: analysis?.totalFiles && analysis.totalFiles > 0
                  ? `${Math.min(100, (analysis.filesAnalyzed / analysis.totalFiles) * 100)}%`
                  : '100%',
              }} />
            </div>
          </div>
        )}
      </div>

      {/* Main Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem', marginBottom: '2rem' }}>
        {/* Score Gauge */}
        <div className="glass-card" style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '1.25rem', color: 'var(--text-secondary)' }}>TECHNICAL DEBT SCORE</h3>
          <ScoreGauge score={score ? score.overallScore : 82} grade={score?.breakdown?.grade || 'B'} />
          <button className="btn-secondary" onClick={handleCalculateScore} disabled={actionLoading !== null} style={{ marginTop: '1.5rem', fontSize: '0.8rem', padding: '0.4rem 0.8rem' }}>
            <RefreshCw size={14} /> Recalculate Score
          </button>
        </div>

        {/* Quick Stats & Actions */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem' }}>
            <div className="glass-card" style={{ padding: '1.25rem', cursor: 'pointer' }} onClick={() => navigate(`/projects/${id}/analysis`)}>
              <FileText size={20} color="var(--accent-cyan)" />
              <div style={{ fontSize: '1.25rem', fontWeight: 800, marginTop: '0.5rem' }}>{analysis ? analysis.issuesFound : 0} Issues</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>View AST Findings →</div>
            </div>

            <div className="glass-card" style={{ padding: '1.25rem', cursor: 'pointer' }} onClick={() => navigate(`/projects/${id}/modernize`)}>
              <Sparkles size={20} color="var(--accent-purple)" />
              <div style={{ fontSize: '1.25rem', fontWeight: 800, marginTop: '0.5rem' }}>Java 21 Refactor</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>View Code Diffs →</div>
            </div>

            <div className="glass-card" style={{ padding: '1.25rem', cursor: 'pointer' }} onClick={() => navigate(`/projects/${id}/tests`)}>
              <TestTube size={20} color="var(--accent-emerald)" />
              <div style={{ fontSize: '1.25rem', fontWeight: 800, marginTop: '0.5rem' }}>JUnit 5 Tests</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>Generate Suite →</div>
            </div>
          </div>

          {/* Score Breakdown */}
          <div className="glass-card" style={{ padding: '1.5rem', flex: 1 }}>
            <h4 style={{ fontSize: '0.9rem', fontWeight: 700, marginBottom: '1rem', color: 'var(--text-secondary)' }}>SCORE BREAKDOWN BY DIMENSION</h4>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              {[
                { label: 'Maintainability', value: score?.maintainabilityScore ?? 85, color: 'var(--accent-purple)' },
                { label: 'Modernity', value: score?.modernityScore ?? 78, color: 'var(--accent-cyan)' },
                { label: 'Security', value: score?.securityScore ?? 90, color: 'var(--accent-emerald)' },
                { label: 'Performance', value: score?.performanceScore ?? 88, color: 'var(--accent-amber)' },
              ].map((dim) => (
                <div key={dim.label}>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{dim.label}</span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginTop: '0.35rem' }}>
                    <div style={{ flex: 1, height: '6px', background: 'rgba(255,255,255,0.07)', borderRadius: '3px' }}>
                      <div style={{ width: `${dim.value}%`, height: '100%', background: dim.color, borderRadius: '3px', transition: 'width 1s ease' }} />
                    </div>
                    <span style={{ fontSize: '1rem', fontWeight: 700, color: dim.color, minWidth: '42px' }}>{dim.value}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Score Trend */}
      <div className="glass-card" style={{ padding: '1.75rem' }}>
        <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Technical Debt Score Trend</h3>
        <ScoreTrendChart trends={trends} />
      </div>

      <style>{`
        @keyframes spin { to { transform: rotate(360deg); } }
        @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
        @keyframes progressIndeterminate {
          0% { transform: translateX(-100%); width: 40%; }
          50% { transform: translateX(150%); width: 60%; }
          100% { transform: translateX(300%); width: 40%; }
        }
      `}</style>
    </div>
  );
};
