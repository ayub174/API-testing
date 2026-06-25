import { test, expect } from '@playwright/test';
import { loginAsAdmin } from './helpers';

test('admin kan skapa, redigera och ta bort en bok', async ({ page }) => {
  await loginAsAdmin(page);

  const title = 'E2E Testbok';
  const editedTitle = 'E2E Testbok (redigerad)';

  // Skapa
  await page.getByTestId('book-title').fill(title);
  await page.getByTestId('book-author').fill('Playwright');
  await page.getByTestId('book-price').fill('123');
  await page.getByTestId('book-save').click();

  const createdRow = page.locator('tr', { hasText: title });
  await expect(createdRow).toBeVisible();

  // Redigera (via radens Redigera-knapp)
  const id = await createdRow.getAttribute('data-testid');
  const bookId = id!.replace('book-row-', '');
  await page.getByTestId(`edit-${bookId}`).click();
  await page.getByTestId('book-title').fill(editedTitle);
  await page.getByTestId('book-save').click();
  await expect(page.locator('tr', { hasText: editedTitle })).toBeVisible();

  // Ta bort
  await page.getByTestId(`delete-${bookId}`).click();
  await expect(page.locator('tr', { hasText: editedTitle })).toHaveCount(0);
});
