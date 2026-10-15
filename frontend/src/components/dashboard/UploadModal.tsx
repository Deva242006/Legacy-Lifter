import React, { useState } from 'react';
import { X, Upload, Folder, GitBranch, ArrowRight } from 'lucide-react';
import { createProject, uploadProjectZip, Project } from '../../api/projects';

interface UploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onProjectCreated: (project: Project) => void;
}

export const UploadModal: React.FC<UploadModalProps> = ({ isOpen, onClose, onProjectCreated }) => {
  const [tab, setTab] = useState<'zip' | 'path'>('path');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [path, setPath] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (tab === 'zip' && file) {
        const formData = new FormData();
        formData.append('name', name);
        formData.append('description', description);
        formData.append('file', file);
        const proj = await uploadProjectZip(formData);
        onProjectCreated(proj);
      } else {
        const proj = await createProject({
          name,
          description,
          sourceDirectoryPath: path,
          sourceUrl: path,
          targetJavaVersion: 'Java 21',
        });
        onProjectCreated(proj);
      }
      onClose();
    } catch (err) {
      alert('Failed to ingest project codebase.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      background: 'rgba(0, 0, 0, 0.75)',
      backdropFilter: 'blur(8px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000
    }}>
      <div className="glass-card" style={{ width: '540px', padding: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Ingest Java Codebase</h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', background: 'rgba(255, 255, 255, 0.05)', padding: '0.25rem', borderRadius: '10px' }}>
          <button
            onClick={() => setTab('path')}
            style={{
              flex: 1,
              padding: '0.5rem',
              borderRadius: '8px',
              border: 'none',
              background: tab === 'path' ? 'var(--primary)' : 'transparent',
              color: '#fff',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            Local Directory / Path
          </button>
          <button
            onClick={() => setTab('zip')}
            style={{
              flex: 1,
              padding: '0.5rem',
              borderRadius: '8px',
              border: 'none',
              background: tab === 'zip' ? 'var(--primary)' : 'transparent',
              color: '#fff',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            ZIP File Upload
          </button>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Project Name</label>
            <input
              required
              type="text"
              placeholder="e.g. LegacyPaymentGateway"
              value={name}
              onChange={(e) => setName(e.target.value)}
              style={{
                width: '100%',
                padding: '0.75rem',
                borderRadius: '8px',
                background: '#0d1117',
                border: '1px solid var(--border-color)',
                color: '#fff'
              }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Description</label>
            <input
              type="text"
              placeholder="Monolithic Java 8 codebase migration"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              style={{
                width: '100%',
                padding: '0.75rem',
                borderRadius: '8px',
                background: '#0d1117',
                border: '1px solid var(--border-color)',
                color: '#fff'
              }}
            />
          </div>

          {tab === 'path' ? (
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Local Directory Path</label>
              <input
                required
                type="text"
                placeholder="C:\Projects\MyLegacyApp or /home/user/app"
                value={path}
                onChange={(e) => setPath(e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  borderRadius: '8px',
                  background: '#0d1117',
                  border: '1px solid var(--border-color)',
                  color: '#fff'
                }}
              />
            </div>
          ) : (
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>ZIP Archive</label>
              <input
                required
                type="file"
                accept=".zip"
                onChange={(e) => setFile(e.target.files?.[0] || null)}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  borderRadius: '8px',
                  background: '#0d1117',
                  border: '1px solid var(--border-color)',
                  color: '#fff'
                }}
              />
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
            <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Ingesting...' : 'Start Ingestion'} <ArrowRight size={16} />
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
