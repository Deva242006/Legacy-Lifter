import React from 'react';
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
import { ScoreTrend } from '../../api/projects';

interface ScoreTrendChartProps {
  trends: ScoreTrend[];
}

export const ScoreTrendChart: React.FC<ScoreTrendChartProps> = ({ trends }) => {
  const data = trends.map((t, idx) => ({
    name: `Run #${idx + 1}`,
    overall: t.overallScore,
    maintainability: t.maintainability,
    security: t.security,
    performance: t.performance,
    modernity: t.modernity,
  }));

  if (data.length === 0) {
    return (
      <div style={{ height: 260, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
        No historical trend data points yet. Run static analysis to record trends.
      </div>
    );
  }

  return (
    <div style={{ width: '100%', height: 280 }}>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(255, 255, 255, 0.05)" />
          <XAxis dataKey="name" stroke="var(--text-muted)" fontSize={12} />
          <YAxis domain={[0, 100]} stroke="var(--text-muted)" fontSize={12} />
          <Tooltip
            contentStyle={{ backgroundColor: '#0d1117', borderColor: 'var(--border-color)', borderRadius: '10px' }}
            itemStyle={{ color: '#fff' }}
          />
          <Line type="monotone" dataKey="overall" stroke="#6366f1" strokeWidth={3} dot={{ r: 5 }} name="Overall Score" />
          <Line type="monotone" dataKey="modernity" stroke="#06b6d4" strokeWidth={2} name="Modernity" />
          <Line type="monotone" dataKey="maintainability" stroke="#a855f7" strokeWidth={2} name="Maintainability" />
          <Line type="monotone" dataKey="security" stroke="#10b981" strokeWidth={2} name="Security" />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};
