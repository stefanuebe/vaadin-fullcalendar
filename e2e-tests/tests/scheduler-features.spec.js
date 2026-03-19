// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the scheduler features test view and wait for the timeline to render.
 */
async function gotoSchedulerFeaturesView(page) {
    await page.goto('/test/scheduler-features');
    await page.waitForSelector('.fc', { timeout: 30000 });
    // Resource timeline views render .fc-timeline, not .fc-timegrid-slot
    await page.waitForSelector('.fc-timeline', { timeout: 30000 });
    await waitForVaadin(page);
}

// =============================================================================

// =============================================================================

test.describe('Scheduler Resource Features', () => {

    test.beforeEach(async ({ page }) => {
        await gotoSchedulerFeaturesView(page);
    });

    // -------------------------------------------------------------------------
    // Basic rendering
    // -------------------------------------------------------------------------

    test('calendar renders in resource timeline view', async ({ page }) => {
        await expect(page.locator('.fc-timeline')).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // Resource area columns
    // -------------------------------------------------------------------------

    test('resource area shows Resource Name column header', async ({ page }) => {
        await expect(
            page.locator('.fc-datagrid-header').getByText('Resource Name')
        ).toBeVisible();
    });

    test('resource area shows Dept column header', async ({ page }) => {
        await expect(
            page.locator('.fc-datagrid-header').getByText('Dept')
        ).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // Resources listed in the resource area
    // -------------------------------------------------------------------------

    test('resource Alice is visible in the resource list', async ({ page }) => {
        await expect(
            page.locator('.fc-datagrid-cell:has-text("Alice")')
        ).toBeVisible();
    });

    test('resource Bob is visible in the resource list', async ({ page }) => {
        await expect(
            page.locator('.fc-datagrid-cell:has-text("Bob")')
        ).toBeVisible();
    });

    test('resource Carol is visible in the resource list', async ({ page }) => {
        await expect(
            page.locator('.fc-datagrid-cell:has-text("Carol")')
        ).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // Resource grouping — group headers appear for each department
    // -------------------------------------------------------------------------

    test('resource grouping renders Engineering group header', async ({ page }) => {
        await expect(
            page.locator('.fc-datagrid-cell:has-text("Engineering")')
        ).toBeVisible();
    });

    test('resource grouping renders Design group header', async ({ page }) => {
        await expect(
            page.locator('.fc-datagrid-cell:has-text("Design")')
        ).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // Entries rendered in the timeline
    // -------------------------------------------------------------------------

    test('Alice Task entry is visible in the timeline', async ({ page }) => {
        await expect(
            page.locator('.fc-event:has-text("Alice Task")')
        ).toBeVisible();
    });

    test('Bob Task entry is visible in the timeline', async ({ page }) => {
        await expect(
            page.locator('.fc-event:has-text("Bob Task")')
        ).toBeVisible();
    });

    // -------------------------------------------------------------------------
    // Stable DOM element check
    // -------------------------------------------------------------------------

    test('group-label-area span is present', async ({ page }) => {
        await expect(page.locator('#group-label-area')).toBeVisible();
        await expect(page.locator('#group-label-area')).toHaveText('Resource Groups:');
    });

});
