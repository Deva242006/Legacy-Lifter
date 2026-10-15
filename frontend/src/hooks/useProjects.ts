import { useState, useEffect, useCallback } from 'react';
import {
  getProjects,
  getProject,
  createProject,
  uploadProjectZip,
  type Project,
} from '../api/projects';

/** Fetch and cache the full project list */
export function useProjects() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getProjects();
      setProjects(data ?? []);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e.message ?? 'Failed to load projects');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { refresh(); }, [refresh]);

  const addViaUrl = async (payload: { name: string; description: string; sourceUrl: string }) => {
    const project = await createProject(payload);
    setProjects(prev => [project, ...prev]);
    return project;
  };

  const addViaZip = async (formData: FormData) => {
    const project = await uploadProjectZip(formData);
    setProjects(prev => [project, ...prev]);
    return project;
  };

  return { projects, loading, error, refresh, addViaUrl, addViaZip };
}

/** Fetch a single project by id */
export function useProject(id: string | undefined) {
  const [project, setProject]   = useState<Project | null>(null);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const data = await getProject(id);
      setProject(data);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e.message ?? 'Failed to load project');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { refresh(); }, [refresh]);

  return { project, loading, error, refresh };
}
