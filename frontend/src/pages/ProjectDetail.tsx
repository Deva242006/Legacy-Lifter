import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Play, Sparkles, Gauge, TestTube, GitPullRequest, ArrowRight, RefreshCw, FileText } from 'lucide-react';
import { getProject, getLatestAnalysis, getLatestScore, getScoreTrends, triggerAnalysis, triggerModernization, calculateScore, generateTests, Project, AnalysisRun, DebtScore, ScoreTrend } from '../api/projects';
import { ScoreGauge } from '../components/scoring/ScoreGauge';
import { ScoreTrendChart } from '../components/scoring/ScoreTrendChart';

export const ProjectDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [project, setProject] = useState<Project | null>(null);
  const [analysis, setAnalysis] = useState<AnalysisRun | null>(null);
  const [score, setScore] = useState<DebtScore | null>(null);
  const [trends, setTrends] = useState<ScoreTrend[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  useEffect(() => {
    if (id) loadData(id);
  }, [id]);

  const loadData = async (projectId: string) => {
    try {
      const p = await getProject(projectId);
      setProject(p);

      try {
        const a = await getLatestAnalysis(projectId);
        setAnalysis(a);
      } catch (e) {}

      try {
        const s = await getLatestScore(projectId);
        setScore(s);
      } catch (e) {}

      try {
        const t = await getScoreTrends(projectId);
        setTrends(t);
      } catch (e) {}
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleRunAnalysis = async () => {
    if (!id) return;
    setActionLoading('analysis');
    try {
      await triggerAnalysis(id);
      await loadData(id);
    } catch (e) {
      alert('Analysis trigger failed');
    } finally {
      setActionLoading(null);
    }
  };

  const handleRunModernize = async () => {
    if (!id) return;
    setActionLoading('modernize');
    try {
      await triggerModernization(id);
      navigate(`/projects/${id}/modernize`);
    } catch (e) {
      alert('Modernization trigger failed');
    } finally {
      setActionLoading(null);
    }
  };

  const handleCalculateScore = async () => {
    if (!id) return;
    setActionLoading('score');
    try {
      await calculateScore(id);
      await loadData(id);
    } catch (e) {
      alert('Scoring calculation failed');
    } finally {
      setActionLoading(null);
    }
  };

  const handleGenerateTests = async () => {
    if (!id) return;
    setActionLoading('tests');
    try {
      await generateTests(id);
      navigate(`/projects/${id}/tests`);
    } catch (e) {
      alert('Test generation failed');
    } finally {
      setActionLoading(null);
    }
  };

  if (loading || !project) {
    return <div style={{ padding: '2rem', color: 'var(--text-muted)' }}>Loading project details...</div>;
  }

  return (
    <div style={{ padding: '2rem' }}>
      {/* Header Bar */}
      <div className="glass-card" style={{ padding: '1.75rem', marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <span className="badge badge-medium" style={{ marginBottom: '0.5rem' }}>{project.status}</span>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>{project.name}</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>{project.description || 'Legacy Java Application'}</p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn-secondary" onClick={handleRunAnalysis} disabled={actionLoading !== null}>
            <Play size={16} /> {actionLoading === 'analysis' ? 'Analyzing...' : 'Run AST Analysis'}
          </button>
          <button className="btn-primary" onClick={handleRunModernize} disabled={actionLoading !== null}>
            <Sparkles size={16} /> {actionLoading === 'modernize' ? 'Modernizing...' : 'AI Modernize'}
          </button>
        </div>
      </div>

      {/* Main Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem', marginBottom: '2rem' }}>
        {/* Score Gauge Card */}
        <div className="glass-card" style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '1.25rem', color: 'var(--text-secondary)' }}>TECHNICAL DEBT SCORE</h3>
          <ScoreGauge score={score ? score.overallScore : 82} grade={score?.breakdown?.grade || 'B'} />
          <button className="btn-secondary" onClick={handleCalculateScore} style={{ marginTop: '1.5rem', fontSize: '0.8rem', padding: '0.4rem 0.8rem' }}>
            <RefreshCw size={14} /> Recalculate Score
          </button>
        </div>

        {/* Action Shortcuts & Quick Stats */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem' }}>
            <div className="glass-card" style={{ padding: '1.25rem', cursor: 'pointer' }} onClick={() => navigate(`/projects/${id}/analysis`)}>
              <FileText size={20} color="var(--accent-cyan)" />
              <div style={{ fontSize: '1.25rem', fontWeight: 800, marginTop: '0.5rem' }}>
                {analysis ? analysis.issuesFound : 0} Issues
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>View AST Findings →</div>
            </div>

            <div className="glass-card" style={{ padding: '1.25rem', cursor: 'pointer' }} onClick={() => navigate(`/projects/${id}/modernize`)}>
              <Sparkles size={20} color="var(--accent-purple)" />
              <div style={{ fontSize: '1.25rem', fontWeight: 800, marginTop: '0.5rem' }}>Java 21 Refactor</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>View Code Diffs →</div>
            </div>

            <div className="glass-card" style={{ padding: '1.25rem', cursor: 'pointer' }} onClick={() => navigate(`/projects/${id}/tests`)}>
              <TestTube size={20} color="var(--accent-emerald)" />
              <div style={{ fontSize: '1.25rem', fontWeight: 800, marginTop: '0.5rem' }}>JUnit 5 Tests</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Generate Suite →</div>
            </div>
          </div>

          <div className="glass-card" style={{ padding: '1.5rem', flex: 1 }}>
            <h4 style={{ fontSize: '0.9rem', fontWeight: 700, marginBottom: '1rem', color: 'var(--text-secondary)' }}>SCORE BREAKDOWN BY DIMENSION</h4>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Maintainability</span>
                <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--accent-purple)' }}>{score ? score.maintainabilityScore : 85} / 100</div>
              </div>
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Modernity</span>
                <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--accent-cyan)' }}>{score ? score.modernityScore : 78} / 100</div>
              </div>
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Security</span>
                <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--accent-emerald)' }}>{score ? score.securityScore : 90} / 100</div>
              </div>
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Performance</span>
                <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--accent-amber)' }}>{score ? score.performanceScore : 88} / 100</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Historical Score Trend */}
      <div className="glass-card" style={{ padding: '1.75rem' }}>
        <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Technical Debt Score Trend</h3>
        <ScoreTrendChart trends={trends} />
      </div>
    </div>
  );
};
