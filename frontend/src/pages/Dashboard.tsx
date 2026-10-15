import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Folder, Play, ArrowRight, ShieldCheck, Cpu, Code2, Sparkles } from 'lucide-react';
import { getProjects, Project } from '../api/projects';
import { ScoreGauge } from '../components/scoring/ScoreGauge';

interface DashboardProps {
  onOpenUpload: () => void;
}

export const Dashboard: React.FC<DashboardProps> = ({ onOpenUpload }) => {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    try {
      const data = await getProjects();
      setProjects(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

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
          <div style={{ display: 'flex', gap: '1rem' }}>
            <button className="btn-primary" onClick={onOpenUpload}>
              <Folder size={18} /> Ingest Codebase Now
            </button>
          </div>
        </div>
      </div>

      {/* Stats Summary Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1.25rem', marginBottom: '2.5rem' }}>
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600 }}>TOTAL INGESTED PROJECTS</div>
          <div style={{ fontSize: '2rem', fontWeight: 800, marginTop: '0.25rem' }}>{projects.length}</div>
        </div>
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600 }}>ENGINE TARGET</div>
          <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--accent-cyan)', marginTop: '0.25rem' }}>Java 21 / 25 LTS</div>
        </div>
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600 }}>PARALLEL PROCESSING</div>
          <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--accent-emerald)', marginTop: '0.25rem' }}>Virtual Threads</div>
        </div>
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600 }}>RAG EMBEDDINGS</div>
          <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--accent-purple)', marginTop: '0.25rem' }}>PGVector Active</div>
        </div>
      </div>

      {/* Projects Grid */}
      <h2 style={{ fontSize: '1.35rem', fontWeight: 700, marginBottom: '1.25rem' }}>Ingested Java Codebases</h2>

      {loading ? (
        <div style={{ color: 'var(--text-muted)', padding: '2rem' }}>Loading projects...</div>
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
            <div key={p.id} className="glass-card" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
                  <h3 style={{ fontSize: '1.15rem', fontWeight: 700 }}>{p.name}</h3>
                  <span className="badge badge-medium">{p.status}</span>
                </div>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
                  {p.description || 'Legacy Java application'}
                </p>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'flex', gap: '1rem', marginBottom: '1.5rem' }}>
                  <span>Detected: <strong>{p.javaVersionDetected}</strong></span>
                  <span>Target: <strong>{p.targetJavaVersion}</strong></span>
                </div>
              </div>

              <button className="btn-primary" onClick={() => navigate(`/projects/${p.id}`)} style={{ width: '100%', justifyContent: 'center' }}>
                Open Dashboard <ArrowRight size={16} />
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
