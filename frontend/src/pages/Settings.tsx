import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { GitPullRequest, ExternalLink, Key, CheckCircle2, Eye, EyeOff } from 'lucide-react';
import { createPullRequest, getIntegrations, GitHubIntegration } from '../api/projects';

const TOKEN_KEY = 'github_oauth_token';

export const Settings: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [integrations, setIntegrations] = useState<GitHubIntegration[]>([]);
  const [repoOwner, setRepoOwner] = useState('Deva242006');
  const [repoName, setRepoName] = useState('Legacy-Lifter');
  const [baseBranch, setBaseBranch] = useState('main');
  const [loading, setLoading] = useState(false);

  // GitHub OAuth Token
  const [githubToken, setGithubToken] = useState(() => localStorage.getItem(TOKEN_KEY) ?? '');
  const [tokenSaved, setTokenSaved] = useState(!!localStorage.getItem(TOKEN_KEY));
  const [showToken, setShowToken] = useState(false);

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

  const handleSaveToken = () => {
    if (githubToken.trim()) {
      localStorage.setItem(TOKEN_KEY, githubToken.trim());
      setTokenSaved(true);
    } else {
      localStorage.removeItem(TOKEN_KEY);
      setTokenSaved(false);
    }
  };

  const handleClearToken = () => {
    localStorage.removeItem(TOKEN_KEY);
    setGithubToken('');
    setTokenSaved(false);
  };

  const handleCreatePr = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id) return;
    setLoading(true);
    try {
      const created = await createPullRequest(id, repoOwner, repoName, baseBranch);
      setIntegrations((prev) => [created, ...prev]);
      alert(`GitHub Pull Request created! URL: ${created.prUrl}`);
    } catch {
      alert('Pull Request creation failed. Ensure a GitHub token is set and the repo exists.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div className="glass-card" style={{ padding: '1.75rem', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <GitPullRequest color="var(--primary)" /> GitHub &amp; Pipeline Integrations
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.35rem' }}>
          Configure your GitHub OAuth token and create automated Pull Requests with modernized Java 21 code.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2rem' }}>
        {/* GitHub Token Config */}
        <div className="glass-card" style={{ padding: '1.75rem' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Key size={18} color="var(--accent-amber)" /> GitHub OAuth Token
          </h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
            Required for creating real GitHub PRs. Stored locally in your browser. Generate at GitHub → Settings → Developer Settings → Personal Access Tokens (needs <code>repo</code> scope).
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ position: 'relative' }}>
              <input
                type={showToken ? 'text' : 'password'}
                placeholder="ghp_xxxxxxxxxxxxxxxxxxxx"
                value={githubToken}
                onChange={(e) => { setGithubToken(e.target.value); setTokenSaved(false); }}
                style={{ width: '100%', padding: '0.75rem 2.75rem 0.75rem 0.75rem', borderRadius: '8px', background: '#0d1117', border: `1px solid ${tokenSaved ? 'var(--accent-emerald)' : 'var(--border-color)'}`, color: '#fff', fontFamily: 'var(--font-mono)', fontSize: '0.875rem', boxSizing: 'border-box' }}
              />
              <button
                type="button"
                onClick={() => setShowToken((v) => !v)}
                style={{ position: 'absolute', right: '0.75rem', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                {showToken ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>

            {tokenSaved && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem', color: 'var(--accent-emerald)' }}>
                <CheckCircle2 size={14} /> Token saved locally
              </div>
            )}

            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <button className="btn-primary" onClick={handleSaveToken} style={{ flex: 1, justifyContent: 'center' }}>
                <Key size={14} /> Save Token
              </button>
              {tokenSaved && (
                <button className="btn-secondary" onClick={handleClearToken}>
                  Clear
                </button>
              )}
            </div>
          </div>
        </div>

        {/* PR Creation Form */}
        <div className="glass-card" style={{ padding: '1.75rem' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '1.25rem' }}>Create GitHub Pull Request</h2>

          <form onSubmit={handleCreatePr} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {[
              { label: 'Repository Owner', value: repoOwner, setter: setRepoOwner, placeholder: 'e.g. octocat' },
              { label: 'Repository Name', value: repoName, setter: setRepoName, placeholder: 'e.g. my-legacy-app' },
              { label: 'Base Target Branch', value: baseBranch, setter: setBaseBranch, placeholder: 'e.g. main' },
            ].map((field) => (
              <div key={field.label}>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>{field.label}</label>
                <input
                  required
                  type="text"
                  placeholder={field.placeholder}
                  value={field.value}
                  onChange={(e) => field.setter(e.target.value)}
                  style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', background: '#0d1117', border: '1px solid var(--border-color)', color: '#fff', boxSizing: 'border-box' }}
                />
              </div>
            ))}

            <button type="submit" className="btn-primary" disabled={loading} style={{ marginTop: '0.5rem', justifyContent: 'center' }}>
              <GitPullRequest size={18} /> {loading ? 'Creating Pull Request...' : 'Create GitHub Pull Request'}
            </button>
          </form>
        </div>
      </div>

      {/* Integration History */}
      <div className="glass-card" style={{ padding: '1.75rem' }}>
        <h2 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '1.25rem' }}>Created Pull Requests</h2>

        {integrations.length === 0 ? (
          <div style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '2rem' }}>
            No GitHub Pull Requests created yet for this project.
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1rem' }}>
            {integrations.map((item) => (
              <div key={item.id} style={{ background: 'rgba(255,255,255,0.03)', padding: '1rem 1.25rem', borderRadius: '10px', border: '1px solid var(--border-color)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                  <span className="badge badge-low">{item.prStatus}</span>
                  <a href={item.prUrl} target="_blank" rel="noreferrer" style={{ color: 'var(--accent-cyan)', textDecoration: 'none', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                    PR #{item.prNumber} <ExternalLink size={14} />
                  </a>
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                  Branch: <code style={{ fontFamily: 'var(--font-mono)', color: 'var(--accent-purple)' }}>{item.branchName}</code>
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
                  {item.repoOwner}/{item.repoName}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
