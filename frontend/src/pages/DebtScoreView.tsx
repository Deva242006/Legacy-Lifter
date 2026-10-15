import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Gauge, ShieldCheck, Zap, Activity, Award } from 'lucide-react';
import { getLatestScore, getScoreTrends, DebtScore, ScoreTrend } from '../api/projects';
import { ScoreGauge } from '../components/scoring/ScoreGauge';
import { ScoreTrendChart } from '../components/scoring/ScoreTrendChart';

export const DebtScoreView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [score, setScore] = useState<DebtScore | null>(null);
  const [trends, setTrends] = useState<ScoreTrend[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) loadData();
  }, [id]);

  const loadData = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const s = await getLatestScore(id);
      setScore(s);
      const t = await getScoreTrends(id);
      setTrends(t);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div className="glass-card" style={{ padding: '1.75rem', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Gauge color="var(--accent-purple)" /> Technical Debt Scoring Breakdown
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          4-dimension debt model evaluating maintainability, security, performance, and modernity.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem', marginBottom: '2rem' }}>
        <div className="glass-card" style={{ padding: '2rem', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <ScoreGauge score={score ? score.overallScore : 85} grade={score?.breakdown?.grade || 'A'} size={200} />
          <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
            <span className="badge badge-low" style={{ fontSize: '0.9rem', padding: '0.5rem 1rem' }}>
              <Award size={16} /> OVERALL GRADE {score?.breakdown?.grade || 'A'}
            </span>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.25rem' }}>
          <div className="glass-card" style={{ padding: '1.5rem' }}>
            <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--accent-purple)' }}>MAINTAINABILITY</div>
            <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.25rem 0' }}>{score ? score.maintainabilityScore : 88}%</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Low raw types & lambda candidacy</div>
          </div>

          <div className="glass-card" style={{ padding: '1.5rem' }}>
            <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--accent-cyan)' }}>MODERNITY SCORE</div>
            <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.25rem 0' }}>{score ? score.modernityScore : 80}%</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Java 21 Virtual Threads & Records adoption</div>
          </div>

          <div className="glass-card" style={{ padding: '1.5rem' }}>
            <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--accent-emerald)' }}>SECURITY SCORE</div>
            <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.25rem 0' }}>{score ? score.securityScore : 92}%</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Safe Optional handling & sealed types</div>
          </div>

          <div className="glass-card" style={{ padding: '1.5rem' }}>
            <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--accent-amber)' }}>PERFORMANCE</div>
            <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.25rem 0' }}>{score ? score.performanceScore : 86}%</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Non-blocking I/O & virtual executors</div>
          </div>
        </div>
      </div>

      <div className="glass-card" style={{ padding: '1.75rem' }}>
        <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Historical Debt Trend</h3>
        <ScoreTrendChart trends={trends} />
      </div>
    </div>
  );
};
