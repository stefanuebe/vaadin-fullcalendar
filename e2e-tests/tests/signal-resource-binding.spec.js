// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

async function gotoSignalResourceView(page) {
    await page.goto('/test/signal-resource-binding');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await waitForVaadin(page);
}

test.describe('Signal Resource Binding (Phase 2)', () => {

    test.beforeEach(async ({ page }) => {
        await gotoSignalResourceView(page);
    });

    test('initial state: no resources on the scheduler', async ({ page }) => {
        // Timeline view should be rendered but with no resource rows
        await expect(page.locator('#resource-count')).toHaveText('0');
    });

    test('add resource via signal: resource appears in timeline', async ({ page }) => {
        await page.locator('#add-resource-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        await expect(page.locator('#resource-count')).toHaveText('1', { timeout: 5000 });
        await expect(page.locator('#last-action')).toHaveText('resource-added');

        // Resource should appear in the resource area
        const resourceCells = page.locator('.fc-datagrid-cell-main:has-text("Room 1")');
        await expect(resourceCells.first()).toBeVisible({ timeout: 5000 });
    });

    test('add multiple resources: all appear', async ({ page }) => {
        await page.locator('#add-resource-btn').click();
        await waitForVaadin(page);
        await page.locator('#add-resource-btn').click();
        await waitForVaadin(page);
        await page.locator('#add-resource-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        await expect(page.locator('#resource-count')).toHaveText('3', { timeout: 5000 });

        await expect(page.locator('.fc-datagrid-cell-main:has-text("Room 1")').first()).toBeVisible({ timeout: 5000 });
        await expect(page.locator('.fc-datagrid-cell-main:has-text("Room 2")').first()).toBeVisible({ timeout: 5000 });
        await expect(page.locator('.fc-datagrid-cell-main:has-text("Room 3")').first()).toBeVisible({ timeout: 5000 });
    });

    test('remove resource via signal: resource disappears', async ({ page }) => {
        // Add two resources
        await page.locator('#add-resource-btn').click();
        await waitForVaadin(page);
        await page.locator('#add-resource-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        await expect(page.locator('#resource-count')).toHaveText('2', { timeout: 5000 });

        // Remove first
        await page.locator('#remove-first-resource-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        await expect(page.locator('#resource-count')).toHaveText('1', { timeout: 5000 });
        // Room 1 should be gone, Room 2 should remain
        await expect(page.locator('.fc-datagrid-cell-main:has-text("Room 2")').first()).toBeVisible({ timeout: 5000 });
    });

    test('modify resource title via signal: title updates in display', async ({ page }) => {
        // Add a resource
        await page.locator('#add-resource-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        await expect(page.locator('.fc-datagrid-cell-main:has-text("Room 1")').first()).toBeVisible({ timeout: 5000 });

        // Modify title
        await page.locator('#modify-first-resource-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        await expect(page.locator('#last-action')).toHaveText('resource-modified', { timeout: 5000 });
        // Title should start with "Modified-"
        await expect(page.locator('.fc-datagrid-cell-main:has-text("Modified-")').first()).toBeVisible({ timeout: 5000 });
    });

    test('add entry to resource: entry appears in correct resource row', async ({ page }) => {
        // Add resource first
        await page.locator('#add-resource-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        // Add entry to that resource
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        await expect(page.locator('#last-action')).toHaveText('entry-added', { timeout: 5000 });
        // Entry should be visible
        const entries = page.locator('.fc-event:has-text("Meeting 1")');
        await expect(entries.first()).toBeVisible({ timeout: 5000 });
    });

    // -------------------------------------------------------------------------
    // Hierarchical resources
    // -------------------------------------------------------------------------

    test('add child resource via modify: child appears in timeline', async ({ page }) => {
        // Add a parent resource first
        await page.locator('#add-resource-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        await expect(page.locator('.fc-datagrid-cell-main:has-text("Room 1")').first()).toBeVisible({ timeout: 5000 });

        // Add child via modify on parent
        await page.locator('#add-child-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        await expect(page.locator('#last-action')).toHaveText('child-added', { timeout: 5000 });
        await expect(page.locator('#child-count')).toHaveText('1', { timeout: 5000 });

        // Child should appear in the timeline
        await expect(page.locator('.fc-datagrid-cell-main:has-text("Child 1")').first()).toBeVisible({ timeout: 5000 });
    });

    test('remove parent resource: children are also removed', async ({ page }) => {
        // Add parent
        await page.locator('#add-resource-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        // Add child
        await page.locator('#add-child-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        // Both should be visible
        await expect(page.locator('.fc-datagrid-cell-main:has-text("Room 1")').first()).toBeVisible({ timeout: 5000 });
        await expect(page.locator('.fc-datagrid-cell-main:has-text("Child 1")').first()).toBeVisible({ timeout: 5000 });

        // Remove parent
        await page.locator('#remove-first-resource-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        await expect(page.locator('#last-action')).toHaveText('resource-removed', { timeout: 5000 });
        await expect(page.locator('#resource-count')).toHaveText('0', { timeout: 5000 });

        // Both parent and child should be gone
        await expect(page.locator('.fc-datagrid-cell-main:has-text("Room 1")')).toHaveCount(0, { timeout: 5000 });
        await expect(page.locator('.fc-datagrid-cell-main:has-text("Child 1")')).toHaveCount(0, { timeout: 5000 });
    });
});
