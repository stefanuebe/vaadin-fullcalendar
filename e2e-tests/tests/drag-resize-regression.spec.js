// @ts-check
/**
 * Regression tests for drag/drop and resize operations.
 *
 * These tests verify that no server-side errors (e.g. ClassCastException)
 * occur when entries are dragged or resized. Unlike the soft-skip tests in
 * calendar-interactions.spec.js, these tests HARD-FAIL when entries cannot
 * be found and explicitly check for Vaadin error notifications after every
 * interaction.
 *
 * The demo creates entries relative to LocalDate.now(), so all entries are
 * in the current month when the demo starts.
 */
const { test, expect, waitForCalendarUpdate, changeView } = require('./fixtures');

/**
 * Assert that no Vaadin error notification or system error appeared on the page.
 * This catches server-side exceptions (e.g. ClassCastException) that the
 * DefaultErrorHandler surfaces as notifications.
 */
async function assertNoServerError(page) {
  // Give any pending error notification a moment to appear
  await page.waitForTimeout(200);

  // Check for Vaadin system error overlay
  const systemError = page.locator('.v-system-error');
  await expect(systemError).toHaveCount(0);

  // Check for error notification cards containing typical server error keywords
  const allCards = page.locator('vaadin-notification-card');
  const count = await allCards.count();
  for (let i = 0; i < count; i++) {
    const text = await allCards.nth(i).textContent();
    expect(text).not.toMatch(/Exception|Internal error|ClassCast/i);
  }
}

// ---------------------------------------------------------------------------
// Drag & Drop
// ---------------------------------------------------------------------------
test.describe('Drag & Drop Regression', () => {

  test.describe('Day Grid Month', () => {

    test('drag all-day entry', async ({ page }) => {
      // "Short trip" — 2-day all-day entry, day 17
      const entry = page.locator('.fc-daygrid-event:has-text("Short trip")').first();
      await expect(entry).toBeVisible({ timeout: 5000 });

      const box = await entry.boundingBox();
      expect(box).toBeTruthy();

      await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
      await page.mouse.down();
      await page.mouse.move(box.x + 200, box.y, { steps: 20 });
      await page.mouse.up();

      await waitForCalendarUpdate(page, 1500);

      // Entry must still be visible (use .first() — drag can leave a ghost element briefly)
      await expect(page.locator('.fc-event:has-text("Short trip")').first()).toBeVisible();
      await assertNoServerError(page);
    });

    test('drag timed entry', async ({ page }) => {
      // "Meeting 8" — timed entry, day 20, no resource constraints
      const entry = page.locator('.fc-daygrid-event:has-text("Meeting 8")').first();
      await expect(entry).toBeVisible({ timeout: 5000 });

      const box = await entry.boundingBox();
      expect(box).toBeTruthy();

      await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
      await page.mouse.down();
      await page.mouse.move(box.x + 150, box.y, { steps: 20 });
      await page.mouse.up();

      await waitForCalendarUpdate(page, 1500);

      await expect(page.locator('.fc-event:has-text("Meeting 8")')).toBeVisible();
      await assertNoServerError(page);
    });
  });

  test.describe('Time Grid Week', () => {

    test.beforeEach(async ({ page }) => {
      await changeView(page, 'Time Grid Week');
    });

    test('drag all-day entry in header', async ({ page }) => {
      // All-day entries appear in the .fc-daygrid-body header area of time-grid views.
      // The recurring sunday event guarantees at least one all-day entry every week.
      const entry = page.locator('.fc-daygrid-body .fc-event').first();
      await expect(entry).toBeVisible({ timeout: 5000 });

      const box = await entry.boundingBox();
      expect(box).toBeTruthy();

      await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
      await page.mouse.down();
      await page.mouse.move(box.x + 120, box.y, { steps: 20 });
      await page.mouse.up();

      await waitForCalendarUpdate(page, 1500);
      await assertNoServerError(page);
    });

    test('drag timed entry in grid body', async ({ page }) => {
      // Timed entries appear as blocks in the time-grid body
      const entry = page.locator('.fc-timegrid-event').first();
      await expect(entry).toBeVisible({ timeout: 5000 });

      const box = await entry.boundingBox();
      expect(box).toBeTruthy();

      // Drag to a different day (horizontal move)
      await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
      await page.mouse.down();
      await page.mouse.move(box.x + 150, box.y, { steps: 20 });
      await page.mouse.up();

      await waitForCalendarUpdate(page, 1500);
      await assertNoServerError(page);
    });
  });
});

// ---------------------------------------------------------------------------
// Resize
// ---------------------------------------------------------------------------
test.describe('Resize Regression', () => {

  test.describe('Day Grid Month', () => {

    test('resize all-day entry (multi-day span)', async ({ page }) => {
      // "Short trip" — 2-day all-day entry, day 17, has resize handle
      const entry = page.locator('.fc-daygrid-event:has-text("Short trip")').first();
      await expect(entry).toBeVisible({ timeout: 5000 });

      // Hover to reveal the resize handle (hidden via CSS until hover)
      await entry.hover();
      await page.waitForTimeout(300);

      // Use the entry's bounding box to locate the resize handle at the right edge
      const entryBox = await entry.boundingBox();
      expect(entryBox).toBeTruthy();

      // Drag from the right edge of the entry to extend it
      const startX = entryBox.x + entryBox.width - 3;
      const startY = entryBox.y + entryBox.height / 2;
      await page.mouse.move(startX, startY);
      await page.mouse.down();
      await page.mouse.move(startX + 100, startY, { steps: 15 });
      await page.mouse.up();

      await waitForCalendarUpdate(page, 1500);

      await expect(page.locator('.fc-event:has-text("Short trip")').first()).toBeVisible();
      await assertNoServerError(page);
    });

    test('resize another all-day entry', async ({ page }) => {
      // "Multi 1" — 2-day all-day entry, day 22, has resize handle
      const entry = page.locator('.fc-daygrid-event:has-text("Multi 1")').first();
      await expect(entry).toBeVisible({ timeout: 5000 });

      // Hover to reveal the resize handle
      await entry.hover();
      await page.waitForTimeout(300);

      const entryBox = await entry.boundingBox();
      expect(entryBox).toBeTruthy();

      // Drag from the right edge of the entry
      const startX = entryBox.x + entryBox.width - 3;
      const startY = entryBox.y + entryBox.height / 2;
      await page.mouse.move(startX, startY);
      await page.mouse.down();
      await page.mouse.move(startX + 100, startY, { steps: 15 });
      await page.mouse.up();

      await waitForCalendarUpdate(page, 1500);

      await expect(page.locator('.fc-event:has-text("Multi 1")').first()).toBeVisible();
      await assertNoServerError(page);
    });
  });

  test.describe('Time Grid Week', () => {

    test.beforeEach(async ({ page }) => {
      await changeView(page, 'Time Grid Week');
    });

    test('resize all-day entry in header', async ({ page }) => {
      // All-day entries in time-grid header that span multiple days have a resize handle.
      const entry = page.locator('.fc-daygrid-body .fc-event:has(.fc-event-resizer)').first();
      await expect(entry).toBeVisible({ timeout: 5000 });

      // Hover to reveal resize handle
      await entry.hover();
      await page.waitForTimeout(300);

      const entryBox = await entry.boundingBox();
      expect(entryBox).toBeTruthy();

      // Drag from the right edge
      const startX = entryBox.x + entryBox.width - 3;
      const startY = entryBox.y + entryBox.height / 2;
      await page.mouse.move(startX, startY);
      await page.mouse.down();
      await page.mouse.move(startX + 100, startY, { steps: 15 });
      await page.mouse.up();

      await waitForCalendarUpdate(page, 1500);
      await assertNoServerError(page);
    });

    test('resize timed entry in grid body (bottom handle)', async ({ page }) => {
      // Timed entries in time-grid have a bottom resize handle
      const entry = page.locator('.fc-timegrid-event').first();
      await expect(entry).toBeVisible({ timeout: 5000 });

      // Hover to reveal the bottom resize handle (force needed — harness div intercepts pointer events)
      await entry.hover({ force: true });
      await page.waitForTimeout(300);

      const entryBox = await entry.boundingBox();
      expect(entryBox).toBeTruthy();

      // Drag from the bottom edge to extend duration
      const startX = entryBox.x + entryBox.width / 2;
      const startY = entryBox.y + entryBox.height - 3;
      await page.mouse.move(startX, startY);
      await page.mouse.down();
      await page.mouse.move(startX, startY + 60, { steps: 15 });
      await page.mouse.up();

      await waitForCalendarUpdate(page, 1500);
      await assertNoServerError(page);
    });
  });
});
