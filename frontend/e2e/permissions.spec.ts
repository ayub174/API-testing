import { test, expect } from '@playwright/test';
import { loginAsAdmin, loginAsHandlaggare } from './helpers';

test('admin ser Ta bort-knappar och Admin-länk', async ({ page }) => {
  await loginAsAdmin(page);
  await expect(page.getByRole('link', { name: 'Admin' })).toBeVisible();
  // Minst en delete-knapp finns i boktabellen.
  await expect(page.locator('[data-testid^="delete-"]').first()).toBeVisible();
});

test('handläggare har låntagar-admin men inte behörighetshantering', async ({ page }) => {
  await loginAsHandlaggare(page);
  // Inga bok-borttagningsknappar, men formuläret för att skapa böcker finns.
  await expect(page.locator('[data-testid^="delete-"]')).toHaveCount(0);
  await expect(page.getByTestId('book-form')).toBeVisible();

  // Har en Admin-länk för låntagarkonton ...
  await page.getByRole('link', { name: 'Admin' }).click();
  await expect(page.getByTestId('create-account-form')).toBeVisible();
  // ... men ingen behörighetsmatris (inga perm-kryssrutor).
  await expect(page.locator('[data-testid^="perm-"]')).toHaveCount(0);
});
