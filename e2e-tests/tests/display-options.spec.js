// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to a test view and wait for the FullCalendar daygrid to render.
 */
async function gotoTestView(page, path) {
  await page.goto(path);
  await page.waitForSelector('.fc', { timeout: 30000 });
  await page.waitForSelector('.fc-daygrid-day', { timeout: 15000 });
}

// =============================================================================

// =============================================================================

test.describe('Render Hook Callbacks', () => {

  test.beforeEach(async ({ page }) => {
    await gotoTestView(page, '/test/render-hooks');
    await waitForVaadin(page);
  });

  test('dayCellClassNames: all day cells have hook-day-cell class', async ({ page }) => {
    const cells = page.locator('.fc-daygrid-day.hook-day-cell');
    const count = await cells.count();
    expect(count).toBeGreaterThan(0);
  });

  test('dayCellContent: custom span with hook-day-content data-testid is rendered', async ({ page }) => {
    const customContent = page.locator('[data-testid="hook-day-content"]');
    const count = await customContent.count();
    expect(count).toBeGreaterThan(0);
  });

  test('dayCellContent: custom span with hook-day-num class is rendered', async ({ page }) => {
    const customSpans = page.locator('.hook-day-num');
    const count = await customSpans.count();
    expect(count).toBeGreaterThan(0);
  });

  test('dayHeaderClassNames: all column headers have hook-header class', async ({ page }) => {
    const headers = page.locator('.fc-col-header-cell.hook-header');
    const count = await headers.count();
    // dayGridMonth always has 7 column headers (Mon–Sun or Sun–Sat)
    expect(count).toBe(7);
  });

  test('dayHeaderContent: custom spans with hook-header-text class are rendered', async ({ page }) => {
    const headerSpans = page.locator('.hook-header-text');
    const count = await headerSpans.count();
    expect(count).toBe(7);
  });

  test('weekNumberClassNames: week number cells have hook-weeknum class', async ({ page }) => {
    // Week numbers are visible (setWeekNumbersVisible(true) in the view)
    const weeknums = page.locator('.fc-daygrid-week-number.hook-weeknum');
    const count = await weeknums.count();
    // March 2025 spans 5–6 week rows depending on locale's first-day-of-week
    expect(count).toBeGreaterThanOrEqual(5);
  });

  test('weekNumberContent: custom spans with hook-weeknum-text class are rendered', async ({ page }) => {
    const weeknumSpans = page.locator('.hook-weeknum-text');
    const count = await weeknumSpans.count();
    expect(count).toBeGreaterThanOrEqual(5);
  });

  test('weekNumberContent: week numbers are prefixed with W', async ({ page }) => {
    // The callback returns 'W' + info.num → cells show e.g. "W9", "W10"
    // Use toHaveText to avoid a textContent() race condition after toBeVisible()
    const firstWeeknum = page.locator('.hook-weeknum-text').first();
    await expect(firstWeeknum).toHaveText(/^W\d+$/);
  });

  // Note: allDayClassNames (adds 'hook-allday') only applies in timegrid views
  // where the all-day row header exists. The current test view uses dayGridMonth,
  // which has no all-day row header. The option is verified by DisplayOptionsTest.java.

});

// =============================================================================

// =============================================================================

test.describe('Display Options', () => {

  test.beforeEach(async ({ page }) => {
    await gotoTestView(page, '/test/display-options');
    await waitForVaadin(page);
    // Wait for events to be rendered before running assertions
    await page.waitForSelector('.fc-event', { timeout: 15000 });
  });

  test('calendar is visible in dayGridMonth view', async ({ page }) => {
    await expect(page.locator('.fc')).toBeVisible();
    await expect(page.locator('.fc-dayGridMonth-view')).toBeVisible();
  });

  test('maxEntriesPerDay: "+3 more" overflow link appears on 2025-03-10', async ({ page }) => {
    // 5 events on 2025-03-10 with maxEntriesPerDay=2 → "+3 more" link must appear
    const crowdedDay = page.locator('.fc-daygrid-day[data-date="2025-03-10"]');
    await expect(crowdedDay).toBeVisible();
    const moreLink = crowdedDay.locator('.fc-daygrid-more-link');
    await expect(moreLink).toBeVisible({ timeout: 10000 });
    // Verify the link shows exactly "+3 more" (5 events − 2 visible = 3 hidden)
    await expect(moreLink).toHaveText(/\+3\s+more/i);
  });

  test('maxEntriesPerDay: exactly 2 event rows visible in the crowded day cell', async ({ page }) => {
    // With maxEntriesPerDay=2 (FC dayMaxEvents), only 2 events are visible per day cell;
    // the remaining 3 are behind the "+N more" link. FC may keep hidden events in the DOM,
    // so we count only visible elements.
    const crowdedDay = page.locator('.fc-daygrid-day[data-date="2025-03-10"]');
    const visibleEvents = crowdedDay.locator('.fc-daygrid-event:visible');
    const count = await visibleEvents.count();
    expect(count).toBe(2);
  });

  test('displayEventEnd: event time element shows both start and end time', async ({ page }) => {
    // "Has End Time" runs 10:00–11:30; with displayEventEnd=true the .fc-event-time
    // element must contain both a start and an end time (e.g. "10:00 - 11:30am")
    const eventTime = page.locator('.fc-event:has-text("Has End Time") .fc-event-time');
    await expect(eventTime).toBeVisible({ timeout: 10000 });
    // Two separate time components (HH:MM ... HH:MM) with any separator between them
    await expect(eventTime).toHaveText(/\d+:\d+.*\d+:\d+/);
  });

});
