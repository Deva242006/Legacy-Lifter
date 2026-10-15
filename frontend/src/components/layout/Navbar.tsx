import React from 'react';
import { Cpu, Zap, FolderPlus, ShieldCheck } from 'lucide-react';

interface NavbarProps {
  onOpenUpload: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onOpenUpload }) => {
  return (
    <header style={{
      height: '70px',
      borderBottom: '1px solid var(--border-color)',
      background: 'rgba(9, 13, 22, 0.8)',
      backdropFilter: 'blur(16px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 2rem',
      position: 'sticky',
      top: 0,
      zIndex: 100
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div style={{
          width: '42px',
          height: '42px',
          borderRadius: '12px',
          background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: '0 4px 20px rgba(99, 102, 241, 0.4)'
        }}>
          <Zap size={24} color="#fff" />
        </div>
        <div>
          <h1 style={{ fontSize: '1.25rem', fontWeight: 700, background: 'linear-gradient(90deg, #fff, #94a3b8)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            LegacyLifter
          </h1>
          <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            <Cpu size={12} color="#06b6d4" /> Java 21/25 Code Modernization & Debt Eraser
          </p>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <span className="badge badge-low" style={{ padding: '0.4rem 0.8rem' }}>
          <ShieldCheck size={14} /> AST + Virtual Threads Engine Active
        </span>

        <button className="btn-primary" onClick={onOpenUpload}>
          <FolderPlus size={18} /> Ingest Codebase
        </button>
      </div>
    </header>
  );
};
