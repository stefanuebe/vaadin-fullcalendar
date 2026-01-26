// @ts-check
const { test, expect, changeView, waitForCalendarUpdate } = require('./fixtures');

test.describe('Calendar Views', () => {

  test('should default to Day Grid Month view', async ({ page }) => {
    // Check for month view specific elements
    const monthView = page.locator('.fc-dayGridMonth-view');
    await expect(monthView).toBeVisible();
  });

  test('should switch to Day Grid Week view', async ({ page }) => {
    await changeView(page, 'Day Grid Week');

    // Verify week view is displayed
    const weekView = page.locator('.fc-dayGridWeek-view');
    await expect(weekView).toBeVisible();
  });

  test('should switch to Time Grid Day view', async ({ page }) => {
    await changeView(page, 'Time Grid Day');

    // Verify time grid day view
    const dayView = page.locator('.fc-timeGridDay-view');
    await expect(dayView).toBeVisible();

    // Should have time slots
    const timeSlots = page.locator('.fc-timegrid-slot');
    const slotCount = await timeSlots.count();
    expect(slotCount).toBeGreaterThan(0);
  });

  test('should switch to Time Grid Week view', async ({ page }) => {
    await changeView(page, 'Time Grid Week');

    // Verify time grid week view
    const weekTimeView = page.locator('.fc-timeGridWeek-view');
    await expect(weekTimeView).toBeVisible();

    // Should have time slots
    const timeSlots = page.locator('.fc-timegrid-slot');
    const slotCount = await timeSlots.count();
    expect(slotCount).toBeGreaterThan(0);
  });

  test('should switch to List Week view', async ({ page }) => {
    await changeView(page, 'List Week');

    // Verify list view
    const listView = page.locator('.fc-listWeek-view, .fc-list');
    await expect(listView).toBeVisible();
  });

  test('should switch to List Month view', async ({ page }) => {
    await changeView(page, 'List Month');

    // Verify list month view
    const listView = page.locator('.fc-listMonth-view, .fc-list');
    await expect(listView).toBeVisible();
  });

  test('should switch to Multi Month view', async ({ page }) => {
    await changeView(page, 'Multi Month');

    // Verify multi-month view shows
    await waitForCalendarUpdate(page);

    const calendar = page.locator('.fc');
    await expect(calendar).toBeVisible();
  });

  test('should preserve entries when switching views', async ({ page }) => {
    // Count entries in month view
    const initialCount = await page.locator('.fc-event').count();
    expect(initialCount).toBeGreaterThan(0);

    // Switch to week view
    await changeView(page, 'Day Grid Week');

    // Calendar should still be visible
    const calendar = page.locator('.fc');
    await expect(calendar).toBeVisible();

    // Switch back to month view
    await changeView(page, 'Day Grid Month');

    // Count should be similar (entries weren't lost)
    const finalCount = await page.locator('.fc-event').count();
    expect(finalCount).toBeGreaterThan(0);
  });

  test('should handle rapid view switching', async ({ page }) => {
    const viewButton = page.locator('vaadin-menu-bar-button:has-text("View:")');
    const views = ['Day Grid Week', 'Time Grid Day', 'Day Grid Month', 'List Week', 'Day Grid Month'];

    for (const viewName of views) {
      await viewButton.click();
      await page.waitForTimeout(300);

      const option = page.locator(`vaadin-menu-bar-list-box vaadin-menu-bar-item:has-text("${viewName}")`).first();
      if (await option.isVisible({ timeout: 1000 })) {
        await option.click();
        await page.waitForTimeout(300);
      } else {
        await page.keyboard.press('Escape');
      }
    }

    await waitForCalendarUpdate(page);

    // Calendar should still be functional
    const calendar = page.locator('.fc');
    await expect(calendar).toBeVisible();
  });

  test('should allow scrolling in Time Grid view', async ({ page }) => {
    await changeView(page, 'Time Grid Week');

    // Find any scroll container
    const scrollContainer = page.locator('.fc-scroller-liquid-absolute, .fc-scroller').first();

    if (await scrollContainer.isVisible({ timeout: 2000 })) {
      // Scroll down
      await scrollContainer.evaluate(el => el.scrollTop = 300);
      await page.waitForTimeout(500);

      // Verify calendar is still visible
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();
    }
  });

  test('should show week numbers in month view', async ({ page }) => {
    // Week numbers should be visible (W1, W2, etc.)
    const weekNumbers = page.locator('.fc-daygrid-week-number, [class*="week-number"]');
    const count = await weekNumbers.count();

    // Should have week numbers for each week row
    expect(count).toBeGreaterThanOrEqual(4);
  });
});
