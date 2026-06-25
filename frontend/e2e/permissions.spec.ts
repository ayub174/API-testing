import { test, expect } from '@playwright/test';
import { loginAsAdmin, loginAsHandlaggare } from './helpers';

test('admin ser Ta bort-knappar och Admin-länk', async ({ page }) => {
  await loginAsAdmin(page);
  await expect(page.getByRole('link', { name: 'Admin' })).toBeVisible();
  // Minst en delete-knapp finns i boktabellen.
  await expect(page.locator('[data-testid^="delete-"]').first()).toBeVisible();
});

test('handläggare saknar Ta bort och Admin-länk', async ({ page }) => {
  await loginAsHandlaggare(page);
  await expect(page.getByRole('link', { name: 'Admin' })).toHaveCount(0);
  await expect(page.locator('[data-testid^="delete-"]')).toHaveCount(0);
  // Men handläggare får skapa böcker (BOOK_CREATE finns som standard).
  await expect(page.getByTestId('book-form')).toBeVisible();
});
