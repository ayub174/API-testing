/// <reference types="vitest" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Devservern proxar /api -> Spring Boot på :8080 så frontend kan anropa
// /api/... same-origin (CORS-konfig finns ändå backend-sidan).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test-setup.ts',
    // Playwright-specarna ligger i e2e/ och körs separat, inte av Vitest.
    exclude: ['**/node_modules/**', '**/e2e/**'],
  },
});
