import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright e2e. Startar automatiskt både backend (Spring Boot) och
 * frontend (Vite). Webbläsare finns förinstallerade i /opt/pw-browsers –
 * kör INTE `playwright install`.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: [
    {
      command: 'mvn -q -f ../pom.xml spring-boot:run',
      url: 'http://localhost:8080/api/status/health',
      reuseExistingServer: true,
      timeout: 180_000,
    },
    {
      command: 'npm run dev',
      url: 'http://localhost:5173',
      reuseExistingServer: true,
      timeout: 60_000,
    },
  ],
});
