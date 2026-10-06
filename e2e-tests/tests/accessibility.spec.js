// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the accessibility test view and wait for the calendar to render.
 */
async function gotoAccessibilityView(page) {
    await page.goto('/test/accessibility');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 5000 });
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
    // todayHint / prevHint: toolbar buttons carry aria-label
    // -------------------------------------------------------------------------

    test('today button has aria-label set by todayHint', async ({ page }) => {
        // TODAY_HINT "Jump to today" makes FullCalendar set aria-label="Jump to today" on the today button.
        await expect(page.locator('button.fc-today-button')).toHaveAttribute('aria-label', 'Jump to today');
    });

    test('prev button has aria-label set by prevHint', async ({ page }) => {
        // PREV_HINT "Go to previous $0" makes FullCalendar fill in the unit text of the current view ("Month").
        await expect(page.locator('button.fc-prev-button')).toHaveAttribute('aria-label', 'Go to previous Month');
    });

    // -------------------------------------------------------------------------
    // navLinkHint: day number anchors carry an aria-label from the hint template
    // -------------------------------------------------------------------------

    test('nav link day numbers carry aria-label from navLinkHint', async ({ page }) => {
        // NAV_LINK_HINT "Open $0" makes FC set the aria-label of each day number link from the template
        // (e.g. "Open March 1, 2025"). FC's default is "Go to $0", so "Open" proves our value arrived.
        const firstDayNum = page.locator('.fc-daygrid-day-number').first();
        await expect(firstDayNum).toBeVisible();
        await expect(firstDayNum).toHaveAttribute('aria-label', /^Open /);
    });

    // -------------------------------------------------------------------------
    // moreLinkHint: +N more overflow link is present and has aria-label
    // -------------------------------------------------------------------------

    test('more link is present when day has overflow events', async ({ page }) => {
        // March 5 has 5 all-day events and setDayMaxEventRows(2) is set in the
        // demo view, so exactly 3 events are hidden and a "+3 more" link must appear.
        await expect(page.locator('.fc-daygrid-more-link')).toBeVisible({ timeout: 5000 });
    });

    test('more link carries title from moreLinkHint', async ({ page }) => {
        // setMoreLinkHint("$0 more events. Click to expand") causes FC to set
        // title="N more events. Click to expand" on the "+N more" link (FC uses title, not aria-label).
        const moreLink = page.locator('.fc-daygrid-more-link').first();
        await expect(moreLink).toBeVisible({ timeout: 5000 });
        const title = await moreLink.getAttribute('title');
        expect(title).not.toBeNull();
        expect(title).toMatch(/more events/i);
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
