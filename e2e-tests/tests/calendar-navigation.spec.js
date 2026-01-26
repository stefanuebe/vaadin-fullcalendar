// @ts-check
const { test, expect, navigateMonth, clickToday, waitForCalendarUpdate } = require('./fixtures');

test.describe('Calendar Navigation', () => {

  test('should display current month on initial load', async ({ page }) => {
    // The calendar should be visible with current month
    const calendar = page.locator('.fc');
    await expect(calendar).toBeVisible();

    // Should have today cell highlighted
    const todayCell = page.locator('.fc-day-today');
    await expect(todayCell).toBeVisible();
  });

  test('should navigate to previous month', async ({ page }) => {
    // Navigate to previous month
    await navigateMonth(page, 'prev');

    // Calendar should still be visible
    const calendar = page.locator('.fc');
    await expect(calendar).toBeVisible();
  });

  test('should navigate to next month', async ({ page }) => {
    // Navigate to next month
    await navigateMonth(page, 'next');

    // Calendar should still be visible
    const calendar = page.locator('.fc');
    await expect(calendar).toBeVisible();
  });

  test('should return to today when clicking Today button', async ({ page }) => {
    // Navigate away from today
    await navigateMonth(page, 'next');
    await navigateMonth(page, 'next');
    await navigateMonth(page, 'next');

    // Click Today
    await clickToday(page);

    // Calendar should show current month - verify today's date is highlighted
    const todayCell = page.locator('.fc-day-today');
    await expect(todayCell).toBeVisible();
  });

  test('should handle rapid navigation clicks', async ({ page }) => {
    const nextBtn = page.locator('vaadin-button:has(vaadin-icon[icon*="angle-right"])').first();

    // Click rapidly 10 times
    for (let i = 0; i < 10; i++) {
      await nextBtn.click();
      await page.waitForTimeout(50);
    }

    await waitForCalendarUpdate(page);

    // Calendar should still be functional
    const calendar = page.locator('.fc');
    await expect(calendar).toBeVisible();

    // Should be able to return to today
    await clickToday(page);
    const todayCell = page.locator('.fc-day-today');
    await expect(todayCell).toBeVisible();
  });

  test('should navigate to far future (year 2030)', async ({ page }) => {
    const nextBtn = page.locator('vaadin-button:has(vaadin-icon[icon*="angle-right"])').first();

    // Navigate 48 months forward (~4 years)
    for (let i = 0; i < 48; i++) {
      await nextBtn.click();
      await page.waitForTimeout(30);
    }

    await waitForCalendarUpdate(page);

    // Calendar should still work
    const calendar = page.locator('.fc');
    await expect(calendar).toBeVisible();
  });

  test('should navigate to far past (year 2020)', async ({ page }) => {
    const prevBtn = page.locator('vaadin-button:has(vaadin-icon[icon*="angle-left"])').first();

    // Navigate 72 months back (~6 years)
    for (let i = 0; i < 72; i++) {
      await prevBtn.click();
      await page.waitForTimeout(30);
    }

    await waitForCalendarUpdate(page);

    // Calendar should still work
    const calendar = page.locator('.fc');
    await expect(calendar).toBeVisible();

    // Should be able to return to today
    await clickToday(page);
  });

  test.skip('should maintain calendar state after browser back/forward', async ({ page }) => {
    // SKIPPED: This test is not applicable for SPAs where calendar navigation
    // doesn't push browser history entries. The goBack() navigates away from the app.

    // Navigate to a different month
    await navigateMonth(page, 'next');
    await navigateMonth(page, 'next');

    // Use browser back
    await page.goBack();
    await page.waitForTimeout(1000);

    // Calendar should still be visible (might reload)
    await page.waitForSelector('.fc', { timeout: 30000 });

    // Use browser forward
    await page.goForward();
    await page.waitForTimeout(1000);

    // Calendar should still be visible
    await page.waitForSelector('.fc', { timeout: 30000 });
  });
});
