// @ts-check
const { test, expect, closeDialog, waitForCalendarUpdate, waitForVaadin } = require('./fixtures');

test.describe('Calendar Responsive Design', () => {

  test.describe('Tablet View (768x1024)', () => {

    test.beforeEach(async ({ page }) => {
      await page.setViewportSize({ width: 768, height: 1024 });
      await page.reload();
      await page.waitForSelector('.fc', { timeout: 10000 });
      await waitForVaadin(page);
    });

    test('should display calendar correctly on tablet', async ({ page }) => {
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();

      // Calendar entries should still be visible
      const entries = await page.locator('.fc-event').count();
      expect(entries).toBeGreaterThan(0);
    });

    test('should allow clicking entries on tablet', async ({ page }) => {
      const entry = page.locator('.fc-event').first();
      await entry.click();
      await page.waitForTimeout(1000);

      const dialog = page.locator('vaadin-dialog-overlay');
      await expect(dialog).toBeVisible({ timeout: 5000 });

      await closeDialog(page);
    });

    test('should allow navigation on tablet', async ({ page }) => {
      const nextBtn = page.locator('vaadin-button:has(vaadin-icon[icon*="angle-right"])').first();
      await nextBtn.click();
      await waitForCalendarUpdate(page);

      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();
    });
  });

  test.describe('Mobile View (375x667)', () => {

    test.beforeEach(async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 });
      await page.reload();
      await page.waitForSelector('.fc', { timeout: 10000 });
      await waitForVaadin(page);
    });

    test('should display calendar correctly on mobile', async ({ page }) => {
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();
    });

    test('should show navigation controls on mobile', async ({ page }) => {
      // Navigation should still be accessible
      const todayBtn = page.locator('vaadin-button:has-text("Today")');

      // At least the today button should be visible
      await expect(todayBtn).toBeVisible();
    });

    test('should allow clicking entries on mobile', async ({ page }) => {
      const entry = page.locator('.fc-event').first();

      if (await entry.isVisible({ timeout: 2000 })) {
        await entry.click();
        await page.waitForTimeout(1000);

        const dialog = page.locator('vaadin-dialog-overlay');
        await expect(dialog).toBeVisible({ timeout: 5000 });

        await closeDialog(page);
      }
    });

    test('should display entry dialog correctly on mobile', async ({ page }) => {
      const entry = page.locator('.fc-event').first();

      if (await entry.isVisible({ timeout: 2000 })) {
        await entry.click();
        await page.waitForTimeout(1000);

        const dialog = page.locator('vaadin-dialog-overlay');
        await expect(dialog).toBeVisible({ timeout: 5000 });

        // Dialog should have the essential fields
        const titleLabel = page.getByText('Title');
        await expect(titleLabel).toBeVisible();

        // Buttons should be visible
        const saveBtn = page.locator('vaadin-button:has-text("Save")');
        const cancelBtn = page.locator('vaadin-button:has-text("Cancel")');
        await expect(saveBtn).toBeVisible();
        await expect(cancelBtn).toBeVisible();

        await closeDialog(page);
      }
    });
  });

  test.describe('Small Mobile View (320x568)', () => {

    test.beforeEach(async ({ page }) => {
      await page.setViewportSize({ width: 320, height: 568 });
      await page.reload();
      await page.waitForSelector('.fc', { timeout: 10000 });
      await waitForVaadin(page);
    });

    test('should display calendar on very small screen', async ({ page }) => {
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();
    });

    test('should handle viewport resize during use', async ({ page }) => {
      // Start at mobile size
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();

      // Resize to tablet
      await page.setViewportSize({ width: 768, height: 1024 });
      await waitForCalendarUpdate(page, 1500);
      await expect(calendar).toBeVisible();

      // Resize to desktop
      await page.setViewportSize({ width: 1920, height: 1080 });
      await waitForCalendarUpdate(page, 1500);
      await expect(calendar).toBeVisible();

      // Resize back to mobile
      await page.setViewportSize({ width: 320, height: 568 });
      await waitForCalendarUpdate(page, 1500);
      await expect(calendar).toBeVisible();
    });
  });

  test.describe('Wide Screen View (2560x1440)', () => {

    test.beforeEach(async ({ page }) => {
      await page.setViewportSize({ width: 2560, height: 1440 });
      await page.reload();
      await page.waitForSelector('.fc', { timeout: 10000 });
      await waitForVaadin(page);
    });

    test('should display calendar correctly on wide screen', async ({ page }) => {
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();

      // Calendar should use available space
      const entries = await page.locator('.fc-event').count();
      expect(entries).toBeGreaterThan(0);
    });
  });
});

/**
 * FullCalendar 7 observes its own size. The add-on no longer calls updateSize(). These tests change the
 * height of the calendar's container without a window resize and check a result FullCalendar computes
 * from measuring. With dayMaxEventRows "true" only the entries that fit a day cell are shown, and the
 * rest go behind "+N more".
 */
test.describe('Calendar follows its own size', () => {

  const MARCH_10 = '.fc-daygrid-day[data-date="2025-03-10"]';

  // number of entries behind the "+N more" link of March 10, 0 when there is none
  const hiddenOnMarch10 = async (page) => {
    const moreLink = page.locator(`${MARCH_10} .fc-more-link`);
    if (await moreLink.count() === 0) return 0;
    return parseInt((await moreLink.textContent()).replace(/\D/g, ''), 10);
  };

  // FC writes the height of the calendar element itself, so the test sizes the container (the test view)
  const setContainerHeight = (page, height) => page.evaluate(height => {
    document.querySelector('vaadin-full-calendar').parentElement.style.height = height;
  }, height);

  const setHidden = (page, hidden) => page.evaluate(hidden => {
    document.querySelector('vaadin-full-calendar').hidden = hidden;
  }, hidden);

  test.beforeEach(async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 1200 });
    await page.goto('/test/listener-data');
    await page.waitForSelector('.fc-view', { timeout: 10000 });
    await waitForVaadin(page);
    // fit as many entry rows as the cell height allows
    await page.evaluate(() => document.querySelector('vaadin-full-calendar').calendar.setOption('dayMaxEventRows', true));
    await setContainerHeight(page, '1600px');
    // all five overflow entries of March 10 are rendered and fit
    await expect(page.locator(`${MARCH_10} .fc-event:has-text("Overflow")`)).toHaveCount(5);
    await expect.poll(() => hiddenOnMarch10(page)).toBe(0);
  });

  test('fewer entries fit after the container gets smaller', async ({ page }) => {
    await setContainerHeight(page, '600px');
    await expect.poll(() => hiddenOnMarch10(page)).toBeGreaterThan(0);
  });

  test('the calendar measures again when it is shown after its container got smaller while it was hidden', async ({ page }) => {
    await setHidden(page, true);
    await expect(page.locator('vaadin-full-calendar')).toBeHidden();
    await setContainerHeight(page, '600px');
    await setHidden(page, false);
    await expect.poll(() => hiddenOnMarch10(page)).toBeGreaterThan(0);
  });
});
