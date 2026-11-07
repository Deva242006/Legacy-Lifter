import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Folder, ArrowRight, Code2, Sparkles, AlertTriangle, GitPullRequest, Gauge, Activity } from 'lucide-react';
import { getProjects, getDashboardOverview, Project } from '../api/projects';

interface DashboardProps {
  onOpenUpload: () => void;
}

interface OverviewStats {
  totalProjects: number;
  totalAnalysisRuns: number;
  totalIssuesFound: number;
  totalCriticalIssues: number;
  totalSuggestionsApplied: number;
  totalPrsCreated: number;
  averageDebtScore: number;
}

const STATUS_COLORS: Record<string, string> = {
  CREATED: 'var(--text-muted)',
  ANALYZING: 'var(--accent-amber)',
  ANALYZED: 'var(--accent-cyan)',
  MODERNIZING: 'var(--accent-purple)',
  MODERNIZED: 'var(--accent-emerald)',
  ERROR: 'var(--accent-rose)',
};

export const Dashboard: React.FC<DashboardProps> = ({ onOpenUpload }) => {
  const [projects, setProjects] = useState<Project[]>([]);
  const [stats, setStats] = useState<OverviewStats | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadAll();
  }, []);

  const loadAll = async () => {
    try {
      const [data, overview] = await Promise.allSettled([
        getProjects(),
        getDashboardOverview(),
      ]);
      if (data.status === 'fulfilled') setProjects(data.value);
      if (overview.status === 'fulfilled') setStats(overview.value);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const statCards = [
    {
      label: 'TOTAL PROJECTS',
      value: stats ? stats.totalProjects : projects.length,
      color: 'var(--primary)',
      icon: <Folder size={18} />,
    },
    {
      label: 'ISSUES DETECTED',
      value: stats ? stats.totalIssuesFound.toLocaleString() : '—',
      sub: stats ? `${stats.totalCriticalIssues} critical` : '',
      color: 'var(--accent-rose)',
      icon: <AlertTriangle size={18} />,
    },
    {
      label: 'AVG DEBT SCORE',
      value: stats ? `${stats.averageDebtScore}` : '—',
      color: 'var(--accent-cyan)',
      icon: <Gauge size={18} />,
    },
    {
      label: 'PULL REQUESTS',
      value: stats ? stats.totalPrsCreated : '—',
      color: 'var(--accent-emerald)',
      icon: <GitPullRequest size={18} />,
    },
  ];

  return (
    <div style={{ padding: '2rem' }}>
      {/* Hero Header */}
      <div className="glass-card" style={{ padding: '2.5rem', marginBottom: '2rem', background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(168, 85, 247, 0.1) 100%)' }}>
        <div style={{ maxWidth: '800px' }}>
          <span className="badge badge-medium" style={{ marginBottom: '1rem' }}>
            <Sparkles size={14} /> AI-POWERED JAVA MODERNIZATION PLATFORM
          </span>
          <h1 style={{ fontSize: '2.25rem', fontWeight: 800, marginBottom: '0.75rem', lineHeight: 1.2 }}>
            Erase Decades of Technical Debt from Your Java Codebase
          </h1>
          <p style={{ fontSize: '1.05rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
            Automated AST static analysis with JavaParser + Spoon, RAG-grounded AI modernization to Java 21/25 standards (Virtual Threads, Records, Pattern Matching), and automated JUnit 5 test generation.
          </p>
          <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
            <button className="btn-primary" onClick={onOpenUpload}>
              <Folder size={18} /> Ingest Codebase Now
            </button>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.25rem', marginBottom: '2.5rem' }}>
        {statCards.map((card) => (
          <div key={card.label} className="glass-card" style={{ padding: '1.35rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', fontWeight: 700, letterSpacing: '0.04em' }}>{card.label}</div>
              <div style={{ color: card.color, opacity: 0.7 }}>{card.icon}</div>
            </div>
            <div style={{ fontSize: '2rem', fontWeight: 800, color: card.color }}>{card.value}</div>
            {card.sub && <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>{card.sub}</div>}
          </div>
        ))}
      </div>

      {/* Engine Capability Badges */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '0.875rem', marginBottom: '2.5rem' }}>
        {[
          { label: 'Engine Target', value: 'Java 21 / 25 LTS', color: 'var(--accent-cyan)' },
          { label: 'Concurrency', value: 'Virtual Threads', color: 'var(--accent-emerald)' },
          { label: 'RAG Embeddings', value: 'PGVector Active', color: 'var(--accent-purple)' },
          { label: 'AST Analysis', value: '9 Detectors', color: 'var(--accent-amber)' },
        ].map((b) => (
          <div key={b.label} className="glass-card" style={{ padding: '1rem 1.25rem' }}>
            <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 700 }}>{b.label}</div>
            <div style={{ fontSize: '1.1rem', fontWeight: 800, color: b.color, marginTop: '0.25rem' }}>{b.value}</div>
          </div>
        ))}
      </div>

      {/* Projects Grid */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
        <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>Ingested Java Codebases</h2>
        {!loading && projects.length > 0 && (
          <button className="btn-secondary" onClick={onOpenUpload} style={{ fontSize: '0.85rem', padding: '0.5rem 1rem' }}>
            <Folder size={14} /> Add Project
          </button>
        )}
      </div>

      {loading ? (
        <div style={{ color: 'var(--text-muted)', padding: '2rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{ width: '18px', height: '18px', border: '2px solid var(--primary)', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} />
          Loading projects...
        </div>
      ) : projects.length === 0 ? (
        <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
          <Code2 size={48} color="var(--primary)" style={{ marginBottom: '1rem' }} />
          <h3>No projects ingested yet</h3>
          <p style={{ color: 'var(--text-muted)', margin: '0.5rem 0 1.5rem 0' }}>Ingest a Java project via directory path or ZIP file to start deep static AST analysis.</p>
          <button className="btn-primary" onClick={onOpenUpload}>Ingest First Codebase</button>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '1.5rem' }}>
          {projects.map((p) => (
            <div key={p.id} className="glass-card" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', gap: '1rem' }}>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>{p.name}</h3>
                  <span
                    className="badge badge-medium"
                    style={{ color: STATUS_COLORS[p.status] || 'var(--text-muted)', borderColor: STATUS_COLORS[p.status] || 'var(--border-color)' }}
                  >
                    {p.status}
                  </span>
                </div>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
                  {p.description || 'Legacy Java application'}
                </p>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'flex', gap: '1.25rem', flexWrap: 'wrap' }}>
                  <span>Detected: <strong style={{ color: 'var(--text-primary)' }}>{p.javaVersionDetected}</strong></span>
                  <span>Target: <strong style={{ color: 'var(--accent-emerald)' }}>{p.targetJavaVersion}</strong></span>
                </div>
              </div>

              <button className="btn-primary" onClick={() => navigate(`/projects/${p.id}`)} style={{ width: '100%', justifyContent: 'center' }}>
                Open Dashboard <ArrowRight size={16} />
              </button>
            </div>
          ))}
        </div>
      )}

      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
};
