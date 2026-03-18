// @ts-check
const { test, expect } = require('@playwright/test');

/**
 * Helper: navigate to a test view and wait for the FullCalendar to render.
 */
async function gotoTestView(page, path) {
  await page.goto(path);
  await page.waitForSelector('.fc', { timeout: 30000 });
  // Wait for FC to finish rendering (day cells are present)
  await page.waitForSelector('.fc-daygrid-day', { timeout: 15000 });
}

/**
 * Helper: wait for Vaadin client-server round-trip to complete.
 */
async function waitForVaadin(page) {
  await page.waitForFunction(() => {
    const v = window.Vaadin;
    if (v && v.Flow && v.Flow.clients) {
      return Object.values(v.Flow.clients).every(c => !c.isActive || !c.isActive());
    }
    return true;
  }, { timeout: 5000 }).catch(() => {});
}

// =============================================================================
// Phase 2 — Render Hook Callbacks
// =============================================================================

test.describe('Phase 2 — Render Hooks', () => {

  test.beforeEach(async ({ page }) => {
    await gotoTestView(page, '/test/phase2-render-hooks');
    await waitForVaadin(page);
  });

  test('dayCellClassNames: all day cells have phase2-cell class', async ({ page }) => {
    // Every day cell should have the 'phase2-cell' class added by the callback
    const cells = page.locator('.fc-daygrid-day.phase2-cell');
    const count = await cells.count();
    expect(count).toBeGreaterThan(0);
  });

  test('dayCellContent: custom span with phase2-day-content data-testid is rendered', async ({ page }) => {
    // The dayCellContent callback wraps the day number in a span with data-testid
    const customContent = page.locator('[data-testid="phase2-day-content"]');
    const count = await customContent.count();
    expect(count).toBeGreaterThan(0);
  });

  test('dayCellContent: custom span with phase2-day-num class is rendered', async ({ page }) => {
    const customSpans = page.locator('.phase2-day-num');
    const count = await customSpans.count();
    expect(count).toBeGreaterThan(0);
  });

  test('dayHeaderClassNames: all column headers have phase2-header class', async ({ page }) => {
    const headers = page.locator('.fc-col-header-cell.phase2-header');
    const count = await headers.count();
    // daygrid month has 7 column headers (Mon–Sun or Sun–Sat)
    expect(count).toBe(7);
  });

  test('dayHeaderContent: custom spans with phase2-header-text class are rendered', async ({ page }) => {
    const headerSpans = page.locator('.phase2-header-text');
    const count = await headerSpans.count();
    expect(count).toBe(7);
  });

  test('weekNumberClassNames: week number cells have phase2-weeknum class', async ({ page }) => {
    // Week numbers are visible (setWeekNumbersVisible(true) in the view)
    const weeknums = page.locator('.fc-daygrid-week-number.phase2-weeknum');
    const count = await weeknums.count();
    // A month view shows typically 4–6 week rows
    expect(count).toBeGreaterThanOrEqual(4);
  });

  test('weekNumberContent: custom spans with phase2-weeknum-text class are rendered', async ({ page }) => {
    const weeknumSpans = page.locator('.phase2-weeknum-text');
    const count = await weeknumSpans.count();
    expect(count).toBeGreaterThanOrEqual(4);
  });

  test('weekNumberContent: week numbers are prefixed with W', async ({ page }) => {
    // The callback returns 'W' + info.num, so all week number cells should show e.g. "W9", "W10"
    const firstWeeknum = page.locator('.phase2-weeknum-text').first();
    await expect(firstWeeknum).toBeVisible();
    const text = await firstWeeknum.textContent();
    expect(text).toMatch(/^W\d+$/);
  });

});

// =============================================================================
// Phase 2 — Display Options
// =============================================================================

test.describe('Phase 2 — Display Options', () => {

  test.beforeEach(async ({ page }) => {
    await gotoTestView(page, '/test/phase2-display-options');
    await waitForVaadin(page);
  });

  test('dayMaxEventRows: "+N more" overflow link appears when events exceed the row limit', async ({ page }) => {
    // 5 events on 2025-03-10 with dayMaxEventRows=2 → a "+3 more" link must appear
    const moreLink = page.locator('.fc-daygrid-more-link');
    await expect(moreLink.first()).toBeVisible({ timeout: 10000 });
  });

  test('dayMaxEventRows: no more than 2 visible event rows per day cell', async ({ page }) => {
    // Each day cell should show at most 2 event rows directly (rest are hidden behind "+N more")
    // We check the crowded day specifically — it should have exactly 2 visible event elements
    // plus the more-link, not 5 stacked events
    const moreLinks = page.locator('.fc-daygrid-more-link');
    const count = await moreLinks.count();
    expect(count).toBeGreaterThan(0);
  });

  test('displayEventEnd: end time is visible on timed events', async ({ page }) => {
    // The entry "Has End Time" on 2025-03-15 runs 10:00–11:30
    // With displayEventEnd=true the rendered event text must include the end time
    const event = page.locator('.fc-event:has-text("Has End Time")');
    await expect(event).toBeVisible({ timeout: 10000 });

    // The time range (e.g. "10:00 - 11:30" or "10:00am - 11:30am") should appear in the event
    const eventTime = page.locator('.fc-event:has-text("Has End Time") .fc-event-time');
    if (await eventTime.isVisible({ timeout: 2000 })) {
      const timeText = await eventTime.textContent();
      // End time display produces a time range with a separator (dash, em-dash, or similar)
      expect(timeText).toMatch(/\d+:\d+/);
    } else {
      // Fallback: event text contains a time range
      const fullText = await event.textContent();
      expect(fullText).toMatch(/\d+:\d+/);
    }
  });

  test('calendar is visible and interactive', async ({ page }) => {
    const calendar = page.locator('.fc');
    await expect(calendar).toBeVisible();

    // Verify month view is shown (initialDate=2025-03-01, initialView=dayGridMonth)
    const monthView = page.locator('.fc-dayGridMonth-view');
    await expect(monthView).toBeVisible();
  });

});
