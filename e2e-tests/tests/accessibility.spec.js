// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the accessibility test view and wait for the calendar to render.
 */
async function gotoAccessibilityView(page) {
    await page.goto('/test/accessibility');
    await page.waitForSelector('.vfc-view', { timeout: 10000 });
    await page.waitForSelector('.vfc-view-dayGridMonth', { timeout: 5000 });
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
        await expect(page.locator('.vfc-view-dayGridMonth')).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // eventInteractive: events gain tabindex="0"
    // -------------------------------------------------------------------------

    test('events have tabindex=0 when eventInteractive is enabled', async ({ page }) => {
        // With setEventInteractive(true) FullCalendar adds tabindex="0" to every
        // .vfc-event element so they are reachable via keyboard Tab navigation.
        const tabindex = await page
            .locator('.vfc-event:has-text("Interactive Event A")')
            .first()
            .getAttribute('tabindex');
        expect(tabindex).toBe('0');
    });

    test('interactive event is keyboard focusable', async ({ page }) => {
        // At least one .vfc-event must carry tabindex="0"
        const count = await page.locator('.vfc-event[tabindex="0"]').count();
        expect(count).toBeGreaterThan(0);
    });

    // -------------------------------------------------------------------------
    // navLinks: day-number anchors are present
    // -------------------------------------------------------------------------

    test('nav link day numbers are present when navLinks is enabled', async ({ page }) => {
        // When navLinks=true FullCalendar renders day numbers as anchor (<a>) tags
        // inside .vfc-day-number. At least one must be visible.
        const dayNumbers = page.locator('.vfc-day-number');
        await expect(dayNumbers.first()).toBeVisible();
        expect(await dayNumbers.count()).toBeGreaterThan(0);
    });

    // -------------------------------------------------------------------------
    // buttonHints: toolbar buttons carry aria-label
    // -------------------------------------------------------------------------

    test('today button has title set by buttonHints', async ({ page }) => {
        // v7: FC uses native <button> elements (not vaadin-button); buttonHints sets aria-label.
        // The button text may be localized (e.g. "Vandag" in Afrikaans). Check that a today
        // button exists with a non-empty aria-label from FC.
        const todayBtn = page.locator('button').filter({ hasText: /today|vandag|heute|aujourd/i }).first();
        await expect(todayBtn).toBeVisible();
        const ariaLabel = await todayBtn.getAttribute('aria-label');
        expect(ariaLabel).not.toBeNull();
        expect((ariaLabel || '').length).toBeGreaterThan(0);
    });

    test('prev button has title set by buttonHints', async ({ page }) => {
        // v7: FC uses native <button>; prev button has an aria-label (localized or from buttonHints).
        const buttons = await page.locator('button').all();
        // Find prev button (icon-only, first nav button) — aria-label set by FC
        const prevBtn = buttons.find ? buttons[0] : page.locator('button').first();
        const ariaLabel = await prevBtn.getAttribute('aria-label');
        expect(ariaLabel).not.toBeNull();
        expect((ariaLabel || '').length).toBeGreaterThan(0);
    });

    // -------------------------------------------------------------------------
    // navLinkHint: day number anchors carry an aria-label from the hint template
    // -------------------------------------------------------------------------

    test('nav link day numbers carry title from navLinkHint', async ({ page }) => {
        // v7: navLinkHint sets aria-label (not title) on day number nav links.
        // setNavLinkHint("Go to $0") → aria-label="Go to 1 March 2025" etc.
        const firstDayNum = page.locator('.vfc-day-number').first();
        await expect(firstDayNum).toBeVisible();
        const ariaLabel = await firstDayNum.getAttribute('aria-label');
        expect(ariaLabel).not.toBeNull();
        expect(ariaLabel).toMatch(/go to/i);
    });

    // -------------------------------------------------------------------------
    // moreLinkHint: +N more overflow link is present and has aria-label
    // -------------------------------------------------------------------------

    test('more link is present when day has overflow events', async ({ page }) => {
        // March 5 has 5 all-day events and setDayMaxEventRows(2) is set in the
        // demo view, so exactly 3 events are hidden and a "+3 more" link must appear.
        await expect(page.locator('.vfc-more-link')).toBeVisible({ timeout: 5000 });
    });

    test('more link carries title from moreLinkHint', async ({ page }) => {
        // setMoreLinkHint("$0 more events. Click to expand") causes FC to set
        // title="N more events. Click to expand" on the "+N more" link (FC uses title, not aria-label).
        const moreLink = page.locator('.vfc-more-link').first();
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
        await page.locator('.vfc-event:has-text("Interactive Event A")').first().click();

        // The server-side addEntryClickedListener increments the counter
        await expect(page.locator('#click-count')).not.toHaveText('0', { timeout: 5000 });
        await expect(page.locator('#click-count')).toHaveText('1');
    });

});
