// @ts-check
const { test, expect, closeDialog, waitForCalendarUpdate, changeView, clickToday } = require('./fixtures');

test.describe('Calendar Stress Tests', () => {

  test.describe('Rapid Interactions', () => {

    test('should handle rapid navigation without errors', async ({ page }) => {
      const nextBtn = page.locator('vaadin-button:has(vaadin-icon[icon="vaadin:angle-right"])').first();

      // Click rapidly 20 times
      for (let i = 0; i < 20; i++) {
        await nextBtn.click();
        await page.waitForTimeout(30);
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

    test('should handle rapid view switching without errors', async ({ page }) => {
      const viewButton = page.locator('vaadin-menu-bar-button:has-text("View:")');
      const views = ['Day Grid Week', 'Time Grid Day', 'Day Grid Month', 'List Week', 'Time Grid Week', 'Day Grid Month'];

      for (const viewName of views) {
        await viewButton.click();
        await page.waitForTimeout(200);

        const option = page.locator(`vaadin-menu-bar-list-box vaadin-menu-bar-item:has-text("${viewName}")`).first();
        if (await option.isVisible({ timeout: 1000 })) {
          await option.click();
          await page.waitForTimeout(200);
        } else {
          await page.keyboard.press('Escape');
        }
      }

      await waitForCalendarUpdate(page);

      // Calendar should still be functional
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();
    });

    test('should handle rapid entry clicking without errors', async ({ page }) => {
      const entries = await page.locator('.fc-event').all();

      // Click multiple entries rapidly
      for (let i = 0; i < Math.min(5, entries.length); i++) {
        await entries[i].click();
        await page.waitForTimeout(300);
        await closeDialog(page);
        await page.waitForTimeout(100);
      }

      // Calendar should still be functional
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();
    });

    test('should handle rapid dialog open/close without errors', async ({ page }) => {
      const entry = page.locator('.fc-event').first();

      // Open and close dialog rapidly 5 times
      for (let i = 0; i < 5; i++) {
        await entry.click();
        await page.waitForTimeout(500);
        await closeDialog(page);
        await page.waitForTimeout(200);
      }

      // Calendar should still be functional
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();
    });
  });

  test.describe('Edge Cases', () => {

    test('should handle clicking on calendar boundaries', async ({ page }) => {
      const calendar = page.locator('.fc');
      const box = await calendar.boundingBox();

      if (box) {
        // Click near edges
        await page.mouse.click(box.x + 5, box.y + 5);
        await page.waitForTimeout(200);

        await page.mouse.click(box.x + box.width - 5, box.y + 5);
        await page.waitForTimeout(200);

        await page.mouse.click(box.x + 5, box.y + box.height - 5);
        await page.waitForTimeout(200);

        await page.mouse.click(box.x + box.width - 5, box.y + box.height - 5);
        await page.waitForTimeout(200);
      }

      // Calendar should still be functional
      await expect(calendar).toBeVisible();
    });

    test('should handle right-click on entries', async ({ page }) => {
      const entry = page.locator('.fc-event').first();
      await entry.click({ button: 'right' });
      await page.waitForTimeout(500);

      // Should not crash - close any context menu that appeared
      await page.keyboard.press('Escape');

      // Calendar should still be functional
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();
    });

    test('should handle right-click on empty calendar area', async ({ page }) => {
      const calendar = page.locator('.fc-daygrid-body');
      await calendar.click({ button: 'right' });
      await page.waitForTimeout(500);

      // Should not crash
      await page.keyboard.press('Escape');

      // Calendar should still be functional
      const calendarEl = page.locator('.fc');
      await expect(calendarEl).toBeVisible();
    });

    test('should handle escape key when no dialog is open', async ({ page }) => {
      // Press Escape multiple times when nothing is open
      for (let i = 0; i < 5; i++) {
        await page.keyboard.press('Escape');
        await page.waitForTimeout(100);
      }

      // Calendar should still be functional
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();
    });

    test('should handle keyboard input on calendar', async ({ page }) => {
      // Focus calendar and try various keys
      await page.click('.fc');

      const keys = ['t', 'n', 'p', 'ArrowRight', 'ArrowLeft', 'ArrowUp', 'ArrowDown', 'Enter', 'Tab'];

      for (const key of keys) {
        await page.keyboard.press(key);
        await page.waitForTimeout(100);
      }

      // Close any dialog that might have opened
      await page.keyboard.press('Escape');
      await page.waitForTimeout(200);

      // Calendar should still be functional
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();
    });
  });

  test.describe('Data Integrity', () => {

    test('should maintain entry count after multiple operations', async ({ page }) => {
      // Get initial entry count
      const initialCount = await page.locator('.fc-event').count();
      expect(initialCount).toBeGreaterThan(0);

      // Perform various operations
      await page.locator('.fc-event').first().click();
      await page.waitForTimeout(500);
      await closeDialog(page);

      const nextBtn = page.locator('vaadin-button:has(vaadin-icon[icon="vaadin:angle-right"])').first();
      await nextBtn.click();
      await waitForCalendarUpdate(page);

      await clickToday(page);

      // Entry count should still be positive
      const finalCount = await page.locator('.fc-event').count();
      expect(finalCount).toBeGreaterThan(0);
    });

    test('should preserve entry data after navigation', async ({ page }) => {
      // Click an entry and remember its title
      await page.locator('.fc-event').first().click();
      await page.waitForTimeout(1000);

      const dialog = page.locator('vaadin-dialog-overlay');
      await expect(dialog).toBeVisible({ timeout: 5000 });

      const titleInput = page.locator('vaadin-text-field input').first();
      const originalTitle = await titleInput.inputValue();

      await closeDialog(page);

      // Navigate away and back
      const nextBtn = page.locator('vaadin-button:has(vaadin-icon[icon="vaadin:angle-right"])').first();
      await nextBtn.click();
      await waitForCalendarUpdate(page);

      await clickToday(page);

      // Click the same entry again
      await page.locator('.fc-event').first().click();
      await page.waitForTimeout(1000);

      const titleInputAfter = page.locator('vaadin-text-field input').first();
      await expect(titleInputAfter).toBeVisible({ timeout: 5000 });
      const titleAfter = await titleInputAfter.inputValue();

      // Title should be preserved
      expect(titleAfter).toBe(originalTitle);

      await closeDialog(page);
    });
  });

  test.describe('Concurrent Operations', () => {

    test('should handle navigation while dialog is open', async ({ page }) => {
      // Open a dialog
      await page.locator('.fc-event').first().click();
      await page.waitForTimeout(1000);

      const dialog = page.locator('vaadin-dialog-overlay');
      await expect(dialog).toBeVisible({ timeout: 5000 });

      // Try to navigate (should be blocked by modal or handled gracefully)
      const nextBtn = page.locator('vaadin-button:has(vaadin-icon[icon="vaadin:angle-right"])').first();

      // This might fail if blocked by modal - that's expected behavior
      try {
        await nextBtn.click({ timeout: 1000 });
      } catch (e) {
        // Expected - modal blocks interaction
      }

      // Dialog should still be visible or closed properly
      await closeDialog(page);

      // Calendar should still be functional
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();
    });
  });
});
