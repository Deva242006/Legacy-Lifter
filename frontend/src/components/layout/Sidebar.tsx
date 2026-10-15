import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, FileSearch, Sparkles, Gauge, TestTube, GitPullRequest, Settings } from 'lucide-react';

interface SidebarProps {
  projectId?: string;
}

export const Sidebar: React.FC<SidebarProps> = ({ projectId }) => {
  const basePrefix = projectId ? `/projects/${projectId}` : '';

  const navItems = [
    { label: 'Dashboard', icon: LayoutDashboard, path: projectId ? `/projects/${projectId}` : '/' },
    { label: 'Static Analysis', icon: FileSearch, path: projectId ? `${basePrefix}/analysis` : '/analysis' },
    { label: 'AI Modernization', icon: Sparkles, path: projectId ? `${basePrefix}/modernize` : '/modernize' },
    { label: 'Tech Debt Score', icon: Gauge, path: projectId ? `${basePrefix}/score` : '/score' },
    { label: 'JUnit 5 Tests', icon: TestTube, path: projectId ? `${basePrefix}/tests` : '/tests' },
    { label: 'GitHub & Settings', icon: GitPullRequest, path: projectId ? `${basePrefix}/settings` : '/settings' },
  ];

  return (
    <aside style={{
      width: '260px',
      borderRight: '1px solid var(--border-color)',
      background: 'rgba(9, 13, 22, 0.6)',
      padding: '1.5rem 1rem',
      display: 'flex',
      flexDirection: 'column',
      gap: '0.5rem'
    }}>
      <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', padding: '0 0.75rem 0.5rem 0.75rem', letterSpacing: '0.05em' }}>
        MAIN NAVIGATION
      </div>

      {navItems.map((item) => {
        const Icon = item.icon;
        return (
          <NavLink
            key={item.label}
            to={item.path}
            end={item.path === '/' || item.path === `/projects/${projectId}`}
            style={({ isActive }) => ({
              display: 'flex',
              alignItems: 'center',
              gap: '0.75rem',
              padding: '0.75rem 1rem',
              borderRadius: '10px',
              textDecoration: 'none',
              fontWeight: isActive ? 600 : 500,
              fontSize: '0.9rem',
              color: isActive ? '#fff' : 'var(--text-secondary)',
              background: isActive ? 'linear-gradient(90deg, rgba(99, 102, 241, 0.25) 0%, rgba(168, 85, 247, 0.15) 100%)' : 'transparent',
              borderLeft: isActive ? '3px solid var(--primary)' : '3px solid transparent',
              transition: 'all 0.2s ease'
            })}
          >
            <Icon size={18} color="currentColor" />
            {item.label}
          </NavLink>
        );
      })}
    </aside>
  );
};
