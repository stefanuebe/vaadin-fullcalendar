// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the accessibility test view and wait for the calendar to render.
 */
async function gotoAccessibilityView(page) {
    await page.goto('/test/accessibility');
    await page.waitForSelector('.fc', { timeout: 30000 });
    await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 15000 });
    await waitForVaadin(page);
}

// =============================================================================

// =============================================================================

test.describe('Accessibility and Touch', () => {

    test.beforeEach(async ({ page }) => {
        await gotoAccessibilityView(page);
    });

    // -------------------------------------------------------------------------
    // Calendar renders correctly
    // -------------------------------------------------------------------------

    test('calendar renders in dayGridMonth view', async ({ page }) => {
        await expect(page.locator('.fc-dayGridMonth-view')).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // eventInteractive: events gain tabindex="0"
    // -------------------------------------------------------------------------

    test('events have tabindex=0 when eventInteractive is enabled', async ({ page }) => {
        // With setEventInteractive(true) FullCalendar adds tabindex="0" to every
        // .fc-event element so they are reachable via keyboard Tab navigation.
        const tabindex = await page
            .locator('.fc-event:has-text("Interactive Event A")')
            .first()
            .getAttribute('tabindex');
        expect(tabindex).toBe('0');
    });

    test('interactive event is keyboard focusable', async ({ page }) => {
        // At least one .fc-event must carry tabindex="0"
        const count = await page.locator('.fc-event[tabindex="0"]').count();
        expect(count).toBeGreaterThan(0);
    });

    // -------------------------------------------------------------------------
    // navLinks: day-number anchors are present
    // -------------------------------------------------------------------------

    test('nav link day numbers are present when navLinks is enabled', async ({ page }) => {
        // When navLinks=true FullCalendar renders day numbers as anchor (<a>) tags
        // inside .fc-daygrid-day-number. At least one must be visible.
        const dayNumbers = page.locator('.fc-daygrid-day-number');
        await expect(dayNumbers.first()).toBeVisible();
        expect(await dayNumbers.count()).toBeGreaterThan(0);
    });

    // -------------------------------------------------------------------------
    // buttonHints: toolbar buttons carry aria-label
    // -------------------------------------------------------------------------

    test('today button has aria-label set by buttonHints', async ({ page }) => {
        // setButtonHints({ "today": "Jump to today" }) causes FullCalendar to set
        // aria-label="Jump to today" on the today toolbar button.
        const ariaLabel = await page.locator('button.fc-today-button').getAttribute('aria-label');
        expect(ariaLabel).toBe('Jump to today');
    });

    test('prev button has aria-label set by buttonHints', async ({ page }) => {
        // setButtonHints({ "prev": "Go to previous period" })
        const ariaLabel = await page.locator('button.fc-prev-button').getAttribute('aria-label');
        expect(ariaLabel).toBe('Go to previous period');
    });

    // -------------------------------------------------------------------------
    // moreLinkHint: +N more overflow link is present for overflowing events
    // -------------------------------------------------------------------------

    test('more link is present when day has overflow events', async ({ page }) => {
        // March 5 has 5 all-day events and setDayMaxEventRows(2) is set in the
        // demo view, so exactly 3 events are hidden and a "+3 more" link must appear.
        await expect(page.locator('.fc-daygrid-more-link')).toBeVisible({ timeout: 5000 });
    });

    // -------------------------------------------------------------------------
    // Entry click listener wired through server side
    // -------------------------------------------------------------------------

    test('click on interactive event updates counter', async ({ page }) => {
        // Verify counter starts at 0
        await expect(page.locator('#click-count')).toHaveText('0');

        // Click a specific event (no URL, so no page navigation)
        await page.locator('.fc-event:has-text("Interactive Event A")').first().click();

        // The server-side addEntryClickedListener increments the counter
        await expect(page.locator('#click-count')).not.toHaveText('0', { timeout: 5000 });
        await expect(page.locator('#click-count')).toHaveText('1');
    });

});
