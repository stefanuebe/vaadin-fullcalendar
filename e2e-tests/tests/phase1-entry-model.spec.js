// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the Phase 1 test view and wait for the calendar to render.
 */
async function gotoPhase1View(page) {
    await page.goto('/test/phase1-entry-model');
    await page.waitForSelector('.fc', { timeout: 30000 });
    // dayGridMonth renders day-grid cells
    await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 15000 });
    // Wait for entries to be rendered before running assertions
    await page.waitForSelector('.fc-event', { timeout: 15000 });
    await waitForVaadin(page);
}

// =============================================================================
// Phase 1 — Entry Model
// =============================================================================

test.describe('Phase 1 — Entry Model', () => {

    test.beforeEach(async ({ page }) => {
        await gotoPhase1View(page);
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

        // FullCalendar wraps URL events in an <a> tag — verify the href is set
        const href = await page.locator('.fc-event:has-text("Visit Homepage") a').first().getAttribute('href');
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
