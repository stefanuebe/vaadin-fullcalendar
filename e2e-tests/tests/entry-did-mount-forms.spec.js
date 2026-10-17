// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * The calendar merges the entry id, the entryDidMount callback and the native entry listeners into one function.
 * Each form of the callback runs, and the id and the listeners are still set.
 */
test.describe('entryDidMount in any function form', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/entry-did-mount-forms');
        // Wait for a count, not for visibility, because an entry whose callback throws may stay invisible
        await expect(page.locator('.fc-event')).toHaveCount(3, { timeout: 15000 });
        await waitForVaadin(page);
    });

    test('an arrow function with a block body runs and the entry id is set', async ({ page }) => {
        await expect(page.locator('#entry-block')).toHaveAttribute('data-mounted', 'block');
    });

    test('an arrow function with an expression body runs and the id and the native listener are set', async ({ page }) => {
        const entry = page.locator('#entry-expression');
        await expect(entry).toHaveAttribute('data-mounted', 'expression');

        await entry.click();

        await expect(page.locator('#click-count')).toHaveText('1');
    });

    test('the callback overrides the entry id', async ({ page }) => {
        await expect(page.locator('#custom-override')).toHaveCount(1);
        await expect(page.locator('#entry-override')).toHaveCount(0);
    });
});
