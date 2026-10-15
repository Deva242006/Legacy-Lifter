import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { GitPullRequest, GitBranch, ExternalLink, CheckCircle2, ShieldCheck } from 'lucide-react';
import { createPullRequest, getIntegrations, GitHubIntegration } from '../api/projects';

export const Settings: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [integrations, setIntegrations] = useState<GitHubIntegration[]>([]);
  const [repoOwner, setRepoOwner] = useState('Deva242006');
  const [repoName, setRepoName] = useState('Legacy-Lifter');
  const [baseBranch, setBaseBranch] = useState('main');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (id) loadIntegrations();
  }, [id]);

  const loadIntegrations = async () => {
    if (!id) return;
    try {
      const data = await getIntegrations(id);
      setIntegrations(data);
    } catch (e) {
      console.error(e);
    }
  };

  const handleCreatePr = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id) return;
    setLoading(true);
    try {
      const created = await createPullRequest(id, repoOwner, repoName, baseBranch);
      setIntegrations((prev) => [created, ...prev]);
      alert(`GitHub Pull Request created successfully! URL: ${created.prUrl}`);
    } catch (e) {
      alert('Pull Request creation failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div className="glass-card" style={{ padding: '1.75rem', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <GitPullRequest color="var(--primary)" /> GitHub & Pipeline Integrations
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          Automatically open GitHub Pull Requests containing modernized Java 21 code refactoring and technical debt reports.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
        {/* Pull Request Form */}
        <div className="glass-card" style={{ padding: '1.75rem' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '1.25rem' }}>Create GitHub Pull Request</h2>

          <form onSubmit={handleCreatePr} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Repository Owner</label>
              <input
                required
                type="text"
                value={repoOwner}
                onChange={(e) => setRepoOwner(e.target.value)}
                style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', background: '#0d1117', border: '1px solid var(--border-color)', color: '#fff' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Repository Name</label>
              <input
                required
                type="text"
                value={repoName}
                onChange={(e) => setRepoName(e.target.value)}
                style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', background: '#0d1117', border: '1px solid var(--border-color)', color: '#fff' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Base Target Branch</label>
              <input
                required
                type="text"
                value={baseBranch}
                onChange={(e) => setBaseBranch(e.target.value)}
                style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', background: '#0d1117', border: '1px solid var(--border-color)', color: '#fff' }}
              />
            </div>

            <button type="submit" className="btn-primary" disabled={loading} style={{ marginTop: '0.5rem', justifyContent: 'center' }}>
              <GitPullRequest size={18} /> {loading ? 'Creating Pull Request...' : 'Create GitHub Pull Request'}
            </button>
          </form>
        </div>

        {/* Integration History */}
        <div className="glass-card" style={{ padding: '1.75rem' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '1.25rem' }}>Created Pull Requests</h2>

          {integrations.length === 0 ? (
            <div style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '2rem' }}>
              No GitHub Pull Requests created yet for this project.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {integrations.map((item) => (
                <div key={item.id} style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '1rem', borderRadius: '10px', border: '1px solid var(--border-color)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span className="badge badge-low">{item.prStatus}</span>
                    <a href={item.prUrl} target="_blank" rel="noreferrer" style={{ color: 'var(--accent-cyan)', textDecoration: 'none', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                      PR #{item.prNumber} <ExternalLink size={14} />
                    </a>
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                    Branch: <code>{item.branchName}</code>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
