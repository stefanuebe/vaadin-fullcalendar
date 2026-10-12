// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the event sources test view and wait for the calendar to render.
 */
async function gotoEventSourcesView(page) {
    await page.goto('/test/event-sources');
    await page.waitForSelector('.fc', { timeout: 10000 });
    // timeGridWeek renders time-slot rows
    await page.waitForSelector('.fc-timegrid-slot', { timeout: 5000 });
    await waitForVaadin(page);
}

/**
 * Drag the entry with the given title one day column to the right.
 * Returns the entry's box before the drag.
 */
async function dragToNextDay(page, title) {
    const entry = page.locator(`.fc-event:has-text("${title}")`).first();
    await expect(entry).toBeVisible({ timeout: 10000 });
    await entry.scrollIntoViewIfNeeded();
    const box = await entry.boundingBox();
    if (!box) throw new Error(`Could not get bounding box for ${title}`);
    const startX = box.x + box.width / 2;
    const startY = box.y + box.height / 2;

    await page.mouse.move(startX, startY);
    await page.mouse.down();
    // ~120px reaches the next day column in timeGridWeek
    await page.mouse.move(startX + 120, startY, { steps: 10 });
    await page.mouse.up();
    await waitForVaadin(page);
    return box;
}

// =============================================================================

test.describe('Event Source Improvements', () => {

    test.beforeEach(async ({ page }) => {
        await gotoEventSourcesView(page);
    });

    // -------------------------------------------------------------------------
    // Calendar renders correctly
    // -------------------------------------------------------------------------

    test('calendar renders with .fc element', async ({ page }) => {
        await expect(page.locator('.fc')).toBeVisible();
    });

    test('calendar renders in timeGridWeek view', async ({ page }) => {
        await expect(page.locator('.fc-timeGridWeek-view')).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // Event source failure fires for non-existent URL
    // -------------------------------------------------------------------------

    test('event source failure fires for non-existent feed URL', async ({ page }) => {
        // The configured feed URL (/test/api/event-sources/events) does not exist.
        // FullCalendar will call the failure handler on the client, which dispatches
        // the eventSourceFailure custom event. The server-side listener updates the
        // #event-source-failure-message span.
        // Wait up to 10 seconds for the failure message to appear.
        await expect(page.locator('#event-source-failure-message')).not.toHaveText('', { timeout: 10000 });
    });

    test('the failure listener of the new name receives the failures of sources of both names', async ({ page }) => {
        await expect(page.locator('#remote-failure-sources')).toHaveText('failing-feed,failing-remote-feed', { timeout: 10000 });
    });

    test('the withFailure callback runs besides the server listener', async ({ page }) => {
        await expect(page.locator('#event-source-failure-message')).not.toHaveText('', { timeout: 10000 });
        // the client calls the user's callback before it dispatches the event to the server
        expect(await page.evaluate(() => window.userFailureCalls)).toBe(1);
    });

    // -------------------------------------------------------------------------
    // Counter badge is present and initially zero
    // -------------------------------------------------------------------------

    test('external-drop-count badge is present and starts at 0', async ({ page }) => {
        await expect(page.locator('#external-drop-count')).toBeVisible();
        await expect(page.locator('#external-drop-count')).toHaveText('0');
    });

    test('event-source-failure-message span is present', async ({ page }) => {
        await expect(page.locator('#event-source-failure-message')).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // Per-source allow callback (#303)
    // -------------------------------------------------------------------------

    test('an entry whose source allow callback returns true is dropped', async ({ page }) => {
        await dragToNextDay(page, 'Allowed Drag');
        await expect(page.locator('#remote-drop-count')).toHaveText('1');
    });

    test('an entry whose source allow callback returns false snaps back', async ({ page }) => {
        const before = await dragToNextDay(page, 'Blocked Drag');

        // FullCalendar asked the source's callback, with the custom API on the dragged entry
        expect(await page.evaluate(() => window.blockedAllowCalls)).toBeGreaterThan(0);
        expect(await page.evaluate(() => window.blockedAllowHasCustomApi)).toBe(true);

        const blocked = page.locator('.fc-event:has-text("Blocked Drag")').first();
        await expect.poll(async () => (await blocked.boundingBox())?.x).toBeCloseTo(before.x, 0);

        // A drop that reaches the server afterwards proves the blocked one sent nothing
        await dragToNextDay(page, 'Allowed Drag');
        await expect(page.locator('#remote-drop-count')).toHaveText('1');
    });

});

// Note: The following event source features are not covered by E2E tests because they
// require external infrastructure or cannot be reliably simulated in headless Playwright:
//   - GoogleCalendarEventSource: requires a Google Calendar API key
//   - ICalendarEventSource: would require serving a real .ics file from the test app
//   - setStartParam / setEndParam / setTimeZoneParam: require capturing HTTP request query params
//   - ExternalEntryDroppedEvent: requires dragging an external DOM element into the calendar
