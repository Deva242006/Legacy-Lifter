import React from 'react';

interface ScoreGaugeProps {
  score: number;
  grade?: string;
  size?: number;
}

export const ScoreGauge: React.FC<ScoreGaugeProps> = ({ score, grade = 'B', size = 180 }) => {
  const normalizedScore = Math.min(100, Math.max(0, score));
  const strokeWidth = 14;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (normalizedScore / 100) * circumference;

  const getColor = (s: number) => {
    if (s >= 85) return '#10b981';
    if (s >= 70) return '#6366f1';
    if (s >= 55) return '#f59e0b';
    return '#f43f5e';
  };

  return (
    <div style={{ position: 'relative', width: size, height: size, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <svg width={size} height={size} style={{ transform: 'rotate(-90deg)' }}>
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke="rgba(255, 255, 255, 0.08)"
          strokeWidth={strokeWidth}
          fill="transparent"
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke={getColor(normalizedScore)}
          strokeWidth={strokeWidth}
          fill="transparent"
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          strokeLinecap="round"
          style={{ transition: 'stroke-dashoffset 1s ease-in-out' }}
        />
      </svg>
      <div style={{ position: 'absolute', textAlign: 'center' }}>
        <div style={{ fontSize: '2.5rem', fontWeight: 800, color: '#fff', lineHeight: 1 }}>
          {normalizedScore}
        </div>
        <div style={{ fontSize: '0.85rem', fontWeight: 700, color: getColor(normalizedScore), marginTop: '0.25rem' }}>
          GRADE {grade}
        </div>
      </div>
    </div>
  );
};
