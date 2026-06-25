import { test, expect } from '@playwright/test';
import { loginAsAnvandare } from './helpers';

test('användare kan låna en bok och återlämna den', async ({ page }) => {
  await loginAsAnvandare(page);

  // Låna en tillgänglig bok (bok 3 = Sagan om ringen, seedas med lager).
  await page.getByTestId('borrow-3').click();
  await expect(page.getByTestId('books-notice')).toBeVisible();

  // Gå till Mina lån och hitta lånet (nyaste först).
  await page.getByRole('link', { name: 'Mina lån' }).click();
  await expect(page.getByTestId('my-loans-table')).toBeVisible();
  const row = page.locator('tr', { hasText: 'Sagan om ringen' }).first();
  await expect(row).toBeVisible();

  // Återlämna och se statusen ändras.
  await row.getByTestId(/^return-/).click();
  await expect(page.locator('tr', { hasText: 'Sagan om ringen' }).first().getByText('Återlämnad')).toBeVisible();
});

test('användare ser bara låntagar-funktioner', async ({ page }) => {
  await loginAsAnvandare(page);
  await expect(page.getByRole('link', { name: 'Mina lån' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Alla lån' })).toHaveCount(0);
  await expect(page.getByRole('link', { name: 'Användare' })).toHaveCount(0);
  await expect(page.getByRole('link', { name: 'Admin' })).toHaveCount(0);
  // Inga katalog-knappar (redigera/ta bort) och inget bok-formulär.
  await expect(page.locator('[data-testid^="delete-"]')).toHaveCount(0);
  await expect(page.locator('[data-testid^="edit-"]')).toHaveCount(0);
  await expect(page.getByTestId('book-form')).toHaveCount(0);
});

test('ny låntagare kan registrera sig och loggas in', async ({ page }) => {
  await page.goto('/login');
  await page.getByRole('link', { name: 'Skapa ett konto' }).click();
  await expect(page).toHaveURL(/\/register/);

  const username = 'e2e' + Date.now();
  await page.getByTestId('reg-username').fill(username);
  await page.getByTestId('reg-password').fill('hemligt123');
  await page.getByTestId('register-submit').click();

  await expect(page.getByTestId('books-table')).toBeVisible();
  await expect(page.getByTestId('current-user')).toHaveText(username);
});
