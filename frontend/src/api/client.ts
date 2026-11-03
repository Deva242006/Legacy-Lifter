import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL ?? '/api/v1';

export const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Inject GitHub OAuth token from localStorage on every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('github_oauth_token');
  if (token) {
    config.headers['X-GitHub-Token'] = token;
  }
  return config;
});

// Global error interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      console.error(
        `[API] ${error.response.status} ${error.config?.method?.toUpperCase()} ${error.config?.url}`,
        error.response.data
      );
    } else {
      console.error('[API] Network error:', error.message);
    }
    return Promise.reject(error);
  }
);
