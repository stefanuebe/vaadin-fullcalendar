// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the advanced options test view and wait for the calendar to render.
 */
async function gotoAdvancedOptionsView(page) {
    await page.goto('/test/advanced-options');
    await page.waitForSelector('.vfc-view', { timeout: 10000 });
    await page.waitForSelector('.vfc-view-dayGridMonth', { timeout: 5000 });
    await waitForVaadin(page);
}

// =============================================================================

// =============================================================================

test.describe('Advanced Options', () => {

    test.beforeEach(async ({ page }) => {
        await gotoAdvancedOptionsView(page);
    });

    // -------------------------------------------------------------------------
    // Calendar renders correctly
    // -------------------------------------------------------------------------

    test('calendar renders in dayGridMonth view', async ({ page }) => {
        await expect(page.locator('.vfc-view-dayGridMonth')).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // View-specific option: dayMaxEventRows=2 for dayGrid → +N more link
    // -------------------------------------------------------------------------

    test('more link is present due to view-specific dayMaxEventRows=2', async ({ page }) => {
        // 5 events on 2025-03-05 with dayMaxEventRows=2 → at least "+3 more"
        await expect(page.locator('.vfc-more-link')).toBeVisible({ timeout: 5000 });
    });

    // -------------------------------------------------------------------------
    // Standard toolbar buttons still present
    // -------------------------------------------------------------------------

    test('prev button is still in the toolbar', async ({ page }) => {
        // v7: FC uses native <button> elements (not vaadin-button). Prev button is icon-only.
        const buttons = await page.locator('button').all();
        expect(buttons.length).toBeGreaterThanOrEqual(1);
        const prevAriaLabel = await buttons[0].getAttribute('aria-label');
        expect(prevAriaLabel).not.toBeNull();
    });

    test('next button is still in the toolbar', async ({ page }) => {
        // v7: FC uses native <button> elements. Next button is icon-only.
        const buttons = await page.locator('button').all();
        expect(buttons.length).toBeGreaterThanOrEqual(2);
        const nextAriaLabel = await buttons[1].getAttribute('aria-label');
        expect(nextAriaLabel).not.toBeNull();
    });

    test('today button is still in the toolbar', async ({ page }) => {
        // v7: FC uses native <button> elements. Today button (any locale text).
        const buttons = await page.locator('button').all();
        expect(buttons.length).toBeGreaterThanOrEqual(3);
        const todayText = await buttons[2].textContent();
        expect((todayText || '').trim().length).toBeGreaterThan(0);
    });

});
