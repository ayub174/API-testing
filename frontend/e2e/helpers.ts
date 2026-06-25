import { Page, expect } from '@playwright/test';

export async function login(page: Page, username: string, password: string) {
  await page.goto('/login');
  await page.getByTestId('username').fill(username);
  await page.getByTestId('password').fill(password);
  await page.getByTestId('login-submit').click();
}

export async function loginAsAdmin(page: Page) {
  await login(page, 'admin', 'hemligt123');
  await expect(page.getByTestId('books-table')).toBeVisible();
}

export async function loginAsHandlaggare(page: Page) {
  await login(page, 'handlaggare', 'handlaggare123');
  await expect(page.getByTestId('books-table')).toBeVisible();
}
