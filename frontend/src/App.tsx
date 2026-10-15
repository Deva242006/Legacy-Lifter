import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, useParams } from 'react-router-dom';
import { Navbar } from './components/layout/Navbar';
import { Sidebar } from './components/layout/Sidebar';
import { UploadModal } from './components/dashboard/UploadModal';
import { Dashboard } from './pages/Dashboard';
import { ProjectDetail } from './pages/ProjectDetail';
import { AnalysisView } from './pages/AnalysisView';
import { ModernizeView } from './pages/ModernizeView';
import { DebtScoreView } from './pages/DebtScoreView';
import { TestsView } from './pages/TestsView';
import { Settings } from './pages/Settings';
import { Project } from './api/projects';

const ProjectLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { id } = useParams<{ id: string }>();
  return (
    <div style={{ display: 'flex', minHeight: 'calc(100vh - 70px)' }}>
      <Sidebar projectId={id} />
      <main style={{ flex: 1, overflowY: 'auto' }}>
        {children}
      </main>
    </div>
  );
};

export const App: React.FC = () => {
  const [isUploadOpen, setIsUploadOpen] = useState(false);

  const handleProjectCreated = (project: Project) => {
    window.location.href = `/projects/${project.id}`;
  };

  return (
    <BrowserRouter>
      <Navbar onOpenUpload={() => setIsUploadOpen(true)} />
      <UploadModal
        isOpen={isUploadOpen}
        onClose={() => setIsUploadOpen(false)}
        onProjectCreated={handleProjectCreated}
      />

      <Routes>
        <Route
          path="/"
          element={
            <div style={{ display: 'flex', minHeight: 'calc(100vh - 70px)' }}>
              <Sidebar />
              <main style={{ flex: 1, overflowY: 'auto' }}>
                <Dashboard onOpenUpload={() => setIsUploadOpen(true)} />
              </main>
            </div>
          }
        />

        <Route path="/projects/:id" element={<ProjectLayout><ProjectDetail /></ProjectLayout>} />
        <Route path="/projects/:id/analysis" element={<ProjectLayout><AnalysisView /></ProjectLayout>} />
        <Route path="/projects/:id/modernize" element={<ProjectLayout><ModernizeView /></ProjectLayout>} />
        <Route path="/projects/:id/score" element={<ProjectLayout><DebtScoreView /></ProjectLayout>} />
        <Route path="/projects/:id/tests" element={<ProjectLayout><TestsView /></ProjectLayout>} />
        <Route path="/projects/:id/settings" element={<ProjectLayout><Settings /></ProjectLayout>} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
