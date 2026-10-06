// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the scheduler features test view and wait for the timeline to render.
 */
async function gotoSchedulerFeaturesView(page) {
    await page.goto('/test/scheduler-features');
    await page.waitForSelector('.fc', { timeout: 10000 });
    // Resource timeline views render .fc-timeline, not .fc-timegrid-slot
    await page.waitForSelector('.fc-timeline', { timeout: 10000 });
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
    // Resource columns
    // -------------------------------------------------------------------------

    test('resource columns show their headers', async ({ page }) => {
        await expect(page.locator('.fc-datagrid-cell[role="columnheader"]')).toHaveText(['Resource Name', 'Dept']);
    });

    test('resource column headerClass and cellClass reach the column cells', async ({ page }) => {
        await expect(page.locator('.fc-datagrid-cell[role="columnheader"].dept-header')).toHaveText('Dept');
        await expect(page.locator('.fc-datagrid-cell.dept-cell').first()).toBeVisible();
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
    // Resource group header class callback
    // -------------------------------------------------------------------------

    test('resourceGroupHeaderClass: group header cells have the custom-group class', async ({ page }) => {
        // The view sets RESOURCE_GROUP_HEADER_CLASS to a callback returning 'custom-group'.
        await expect(page.locator('.fc-datagrid-cell.fc-resource-group.custom-group'))
            .toHaveText(['Engineering', 'Design']);
    });

    // -------------------------------------------------------------------------
    // Stable DOM element check
    // -------------------------------------------------------------------------

    test('group-label-area span is present', async ({ page }) => {
        await expect(page.locator('#group-label-area')).toBeVisible();
        await expect(page.locator('#group-label-area')).toHaveText('Resource Groups:');
    });

});
