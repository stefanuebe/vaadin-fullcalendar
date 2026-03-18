// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the Phase 4 test view and wait for the calendar to render.
 */
async function gotoPhase4View(page) {
    await page.goto('/test/phase4-event-sources');
    await page.waitForSelector('.fc', { timeout: 30000 });
    // timeGridWeek renders time-slot rows
    await page.waitForSelector('.fc-timegrid-slot', { timeout: 15000 });
    await waitForVaadin(page);
}

// =============================================================================
// Phase 4 — Event Source Improvements
// =============================================================================

test.describe('Phase 4 — Event Source Improvements', () => {

    test.beforeEach(async ({ page }) => {
        await gotoPhase4View(page);
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
        // The configured feed URL (/test/api/phase4/events) does not exist.
        // FullCalendar will call the failure handler on the client, which dispatches
        // the eventSourceFailure custom event. The server-side listener updates the
        // #event-source-failure-message span.
        // Wait up to 10 seconds for the failure message to appear.
        await expect(page.locator('#event-source-failure-message')).not.toHaveText('', { timeout: 10000 });
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

});
