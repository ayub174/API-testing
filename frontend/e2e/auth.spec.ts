import { test, expect } from '@playwright/test';
import { loginAsAdmin } from './helpers';

test('admin kan logga in och hamnar på böcker', async ({ page }) => {
  await loginAsAdmin(page);
  await expect(page).toHaveURL(/\/books/);
  await expect(page.getByTestId('current-user')).toHaveText('admin');
});

test('fel lösenord visar felmeddelande', async ({ page }) => {
  await page.goto('/login');
  await page.getByTestId('username').fill('admin');
  await page.getByTestId('password').fill('fel-losen');
  await page.getByTestId('login-submit').click();
  await expect(page.getByTestId('login-error')).toBeVisible();
  await expect(page).toHaveURL(/\/login/);
});

test('oinloggad omdirigeras till login', async ({ page }) => {
  await page.goto('/books');
  await expect(page).toHaveURL(/\/login/);
});
