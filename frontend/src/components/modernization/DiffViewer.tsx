import React from 'react';
import { ArrowRight, CheckCircle2, ShieldAlert } from 'lucide-react';

interface DiffViewerProps {
  originalCode: string;
  modernizedCode: string;
  filePath: string;
  explanation: string;
  strategyType: string;
  confidenceScore: number;
  riskLevel: string;
  applied: boolean;
  onApply?: () => void;
}

export const DiffViewer: React.FC<DiffViewerProps> = ({
  originalCode,
  modernizedCode,
  filePath,
  explanation,
  strategyType,
  confidenceScore,
  riskLevel,
  applied,
  onApply,
}) => {
  return (
    <div className="glass-card" style={{ padding: '1.5rem', marginBottom: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
        <div>
          <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--accent-cyan)' }}>{filePath}</span>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginTop: '0.2rem' }}>{strategyType}</h3>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <span className={`badge badge-${riskLevel.toLowerCase()}`}>
            Risk: {riskLevel}
          </span>
          <span className="badge badge-low">
            Confidence: {(confidenceScore * 100).toFixed(0)}%
          </span>
          {applied ? (
            <span className="badge badge-low" style={{ padding: '0.5rem 1rem' }}>
              <CheckCircle2 size={16} /> Applied
            </span>
          ) : (
            onApply && (
              <button className="btn-primary" onClick={onApply} style={{ padding: '0.4rem 0.9rem', fontSize: '0.85rem' }}>
                Apply Modernization <ArrowRight size={14} />
              </button>
            )
          )}
        </div>
      </div>

      <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginBottom: '1.25rem', background: 'rgba(255, 255, 255, 0.03)', padding: '0.75rem', borderRadius: '8px', borderLeft: '3px solid var(--primary)' }}>
        {explanation}
      </p>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
        <div>
          <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--accent-rose)', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
            <ShieldAlert size={14} /> LEGACY CODE
          </div>
          <pre className="code-block diff-remove">
            <code>{originalCode}</code>
          </pre>
        </div>

        <div>
          <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--accent-emerald)', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
            <CheckCircle2 size={14} /> MODERN JAVA 21+
          </div>
          <pre className="code-block diff-add">
            <code>{modernizedCode}</code>
          </pre>
        </div>
      </div>
    </div>
  );
};
