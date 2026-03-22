// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the entry model test view and wait for the calendar to render.
 */
async function gotoEntryModelView(page) {
    await page.goto('/test/entry-model');
    await page.waitForSelector('.fc', { timeout: 10000 });
    // dayGridMonth renders day-grid cells
    await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 5000 });
    // Wait for entries to be rendered before running assertions
    await page.waitForSelector('.fc-event', { timeout: 5000 });
    await waitForVaadin(page);
}

// =============================================================================

// =============================================================================

test.describe('Entry Model', () => {

    test.beforeEach(async ({ page }) => {
        await gotoEntryModelView(page);
    });

    // -------------------------------------------------------------------------
    // Calendar renders correctly
    // -------------------------------------------------------------------------

    test('calendar renders in dayGridMonth view', async ({ page }) => {
        await expect(page.locator('.fc-dayGridMonth-view')).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // URL entry
    // -------------------------------------------------------------------------

    test('url entry is present and has href attribute', async ({ page }) => {
        const urlEvent = page.locator('.fc-event:has-text("Visit Homepage")').first();
        await expect(urlEvent).toBeVisible();

        // In FC v6, URL entries render as <a> tags directly (the .fc-event IS the <a>)
        const href = await urlEvent.getAttribute('href');
        expect(href).not.toBeNull();
        expect(href).toContain('example.com');
    });

    // -------------------------------------------------------------------------
    // Interactive entry
    // -------------------------------------------------------------------------

    test('interactive entry is visible', async ({ page }) => {
        await expect(page.locator('.fc-event:has-text("Keyboard Event")').first()).toBeVisible();
    });

    test('interactive entry is focusable via keyboard (tabindex="0")', async ({ page }) => {
        // FullCalendar adds tabindex="0" to events when eventInteractive is true
        const tabindex = await page
            .locator('.fc-event:has-text("Keyboard Event")')
            .first()
            .getAttribute('tabindex');
        expect(tabindex).toBe('0');
    });

    // -------------------------------------------------------------------------
    // Recurring with duration
    // -------------------------------------------------------------------------

    test('recurring duration entry renders as multi-day span', async ({ page }) => {
        // Monday 2025-03-03 and Tuesday 2025-03-04 should both show the event
        // (recurringDuration="P2D" = 2-day span starting on each Monday/Wednesday)
        const monday = page.locator('.fc-daygrid-day[data-date="2025-03-03"] .fc-event:has-text("Multi-Day Recurring")');
        const tuesday = page.locator('.fc-daygrid-day[data-date="2025-03-04"] .fc-event:has-text("Multi-Day Recurring")');
        await expect(monday).toBeVisible();
        await expect(tuesday).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // RRule entry
    // -------------------------------------------------------------------------

    test('rrule weekly entry renders multiple occurrences', async ({ page }) => {
        // Weekly on Monday + Friday in March 2025 = 9 occurrences
        // (Mon 3,10,17,24,31 + Fri 7,14,21,28)
        const occurrences = page.locator('.fc-event:has-text("RRule Weekly")');
        const count = await occurrences.count();
        expect(count).toBeGreaterThanOrEqual(8);
    });

    // -------------------------------------------------------------------------
    // Exdate
    // -------------------------------------------------------------------------

    test('exdate: skipped Monday is absent from its day cell', async ({ page }) => {
        // March 10 is excluded via exdate — the cell should have no "Exdate Test" event
        const march10Cell = page.locator('.fc-daygrid-day[data-date="2025-03-10"]');
        await expect(march10Cell.locator('.fc-event:has-text("Exdate Test")')).toHaveCount(0);
    });

    test('exdate: non-excluded Monday still renders', async ({ page }) => {
        // March 17 is not excluded and should show the event
        const march17Cell = page.locator('.fc-daygrid-day[data-date="2025-03-17"]');
        await expect(march17Cell.locator('.fc-event:has-text("Exdate Test")')).toHaveCount(1);
    });

    test('exdate: total occurrences is 4 (5 Mondays minus 1 excluded)', async ({ page }) => {
        // Mondays in March 2025: 3, 10, 17, 24, 31 — exdate removes March 10 → 4 left
        const occurrences = page.locator('.fc-event:has-text("Exdate Test")');
        await expect(occurrences).toHaveCount(4);
    });

    // -------------------------------------------------------------------------
    // Monthly last-Friday
    // -------------------------------------------------------------------------

    test('monthly last-friday renders on correct day (March 28)', async ({ page }) => {
        // Last Friday of March 2025 = March 28
        const march28Cell = page.locator('.fc-daygrid-day[data-date="2025-03-28"]');
        await expect(march28Cell.locator('.fc-event:has-text("Last Friday")')).toHaveCount(1);
    });

    test('monthly last-friday does not render on non-last Friday (March 21)', async ({ page }) => {
        // March 21 is a Friday but not the last one
        const march21Cell = page.locator('.fc-daygrid-day[data-date="2025-03-21"]');
        await expect(march21Cell.locator('.fc-event:has-text("Last Friday")')).toHaveCount(0);
    });

    // -------------------------------------------------------------------------
    // ofRaw RRule
    // -------------------------------------------------------------------------

    test('ofRaw renders correct number of occurrences', async ({ page }) => {
        // BYDAY=WE in March 2025: Wednesdays 5, 12, 19, 26 = 4 occurrences
        const occurrences = page.locator('.fc-event:has-text("Raw RRule")');
        await expect(occurrences).toHaveCount(4);
    });

    // -------------------------------------------------------------------------
    // Entry click counter
    // -------------------------------------------------------------------------

    test('entry click updates counter badge', async ({ page }) => {
        // Verify starting state
        await expect(page.locator('#click-count')).toHaveText('0');

        // Click the interactive entry (safe to click — no URL navigation)
        await page.locator('.fc-event:has-text("Keyboard Event")').first().click();

        // Counter should increment to 1 within 5 seconds
        await expect(page.locator('#click-count')).not.toHaveText('0', { timeout: 5000 });
        await expect(page.locator('#click-count')).toHaveText('1');
    });

});
