// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the advanced options test view and wait for the calendar to render.
 */
async function gotoAdvancedOptionsView(page) {
    await page.goto('/test/advanced-options');
    await page.waitForSelector('.fc', { timeout: 30000 });
    await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 15000 });
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
    // Custom button renders in the toolbar
    // -------------------------------------------------------------------------

    test('custom button is present in the toolbar', async ({ page }) => {
        // The toolbar button with text "Schedule" should be visible
        const schedBtn = page.locator('.fc-scheduleWizard-button');
        await expect(schedBtn).toBeVisible();
    });

    test('custom button has correct text', async ({ page }) => {
        await expect(page.locator('.fc-scheduleWizard-button')).toHaveText('Schedule');
    });

    test('custom button has aria-label from hint', async ({ page }) => {
        // setHint("Open scheduling wizard") → aria-label on the button
        const ariaLabel = await page.locator('.fc-scheduleWizard-button').getAttribute('aria-label');
        expect(ariaLabel).toBe('Open scheduling wizard');
    });

    // -------------------------------------------------------------------------
    // Custom button click fires server-side listener
    // -------------------------------------------------------------------------

    test('click count starts at 0', async ({ page }) => {
        await expect(page.locator('#custom-btn-click-count')).toHaveText('0');
    });

    test('clicking custom button increments click counter to 1', async ({ page }) => {
        await page.locator('.fc-scheduleWizard-button').click();
        await waitForVaadin(page);
        await expect(page.locator('#custom-btn-click-count')).toHaveText('1', { timeout: 5000 });
    });

    test('clicking custom button sets correct button name', async ({ page }) => {
        await page.locator('.fc-scheduleWizard-button').click();
        await waitForVaadin(page);
        await expect(page.locator('#custom-btn-name')).toHaveText('scheduleWizard', { timeout: 5000 });
    });

    test('clicking custom button multiple times accumulates count', async ({ page }) => {
        await page.locator('.fc-scheduleWizard-button').click();
        await waitForVaadin(page);
        await page.locator('.fc-scheduleWizard-button').click();
        await waitForVaadin(page);
        await page.locator('.fc-scheduleWizard-button').click();
        await waitForVaadin(page);
        await expect(page.locator('#custom-btn-click-count')).toHaveText('3', { timeout: 5000 });
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
