// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the scheduler reattach test view and wait for the timeline to render.
 */
async function gotoReattachView(page) {
    await page.goto('/test/scheduler-reattach');
    await page.waitForSelector('.fc', { timeout: 30000 });
    await page.waitForSelector('.fc-timeline', { timeout: 30000 });
    await waitForVaadin(page);
}

test.describe('Scheduler Resource Reattach', () => {

    test.beforeEach(async ({ page }) => {
        await gotoReattachView(page);
    });

    test('resources are visible before reattach', async ({ page }) => {
        await expect(page.locator('.fc-datagrid-cell:has-text("Alice")')).toBeVisible();
        await expect(page.locator('.fc-datagrid-cell:has-text("Bob")')).toBeVisible();
    });

    test('resources survive detach and reattach', async ({ page }) => {
        // Verify resources are there initially
        await expect(page.locator('.fc-datagrid-cell:has-text("Alice")')).toBeVisible();
        await expect(page.locator('.fc-datagrid-cell:has-text("Bob")')).toBeVisible();

        // Click the detach & reattach button
        await page.locator('#reattach-button').click();

        // Wait for calendar to re-render after reattach
        await page.waitForSelector('.fc-timeline', { timeout: 30000 });
        await waitForVaadin(page);

        // Resources must still be visible
        await expect(page.locator('.fc-datagrid-cell:has-text("Alice")')).toBeVisible();
        await expect(page.locator('.fc-datagrid-cell:has-text("Bob")')).toBeVisible();
    });

    test('entries survive detach and reattach', async ({ page }) => {
        // Verify entries are there initially
        await expect(page.locator('.fc-event:has-text("Alice Task")')).toBeVisible();
        await expect(page.locator('.fc-event:has-text("Bob Task")')).toBeVisible();

        // Click the detach & reattach button
        await page.locator('#reattach-button').click();

        // Wait for calendar to re-render after reattach
        await page.waitForSelector('.fc-timeline', { timeout: 30000 });
        await waitForVaadin(page);

        // Entries must still be visible
        await expect(page.locator('.fc-event:has-text("Alice Task")')).toBeVisible();
        await expect(page.locator('.fc-event:has-text("Bob Task")')).toBeVisible();
    });

});
