// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the advanced options test view and wait for the calendar to render.
 */
async function gotoAdvancedOptionsView(page) {
    await page.goto('/test/advanced-options');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 5000 });
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
        await expect(page.locator('.fc-dayGridMonth-view')).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // View-specific option: dayMaxEventRows=2 for dayGrid → +N more link
    // -------------------------------------------------------------------------

    test('more link is present due to view-specific dayMaxEventRows=2', async ({ page }) => {
        // 5 events on 2025-03-05 with dayMaxEventRows=2 → at least "+3 more"
        await expect(page.locator('.fc-daygrid-more-link')).toBeVisible({ timeout: 5000 });
    });

    // -------------------------------------------------------------------------
    // Standard toolbar buttons still present
    // -------------------------------------------------------------------------

    test('prev button is still in the toolbar', async ({ page }) => {
        await expect(page.locator('button.fc-prev-button')).toBeVisible();
    });

    test('next button is still in the toolbar', async ({ page }) => {
        await expect(page.locator('button.fc-next-button')).toBeVisible();
    });

    test('today button is still in the toolbar', async ({ page }) => {
        await expect(page.locator('button.fc-today-button')).toBeVisible();
    });

});
