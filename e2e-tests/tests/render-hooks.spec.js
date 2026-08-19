// @ts-check
const { test: base } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

async function gotoRenderHooksView(page) {
    await page.goto('/test/render-hooks');
    await page.waitForSelector('.vfc-view', { timeout: 15000 });
    await page.waitForFunction(
        () => document.querySelectorAll('.vfc-event').length > 0,
        { timeout: 20000 }
    );
    await waitForVaadin(page);
}

/**
 * entryClassNames shares FC's `eventClass` option with the addon's own vfc-* class contract.
 * Both have to survive, on the initial-options route as well as on the setOption route.
 */
base.describe('Entry class names', () => {

    base.beforeEach(async ({ page }) => {
        await gotoRenderHooksView(page);
    });

    base('callback set before attach applies and keeps vfc-event', async ({ page }) => {
        const entry = page.locator('.vfc-event').first();
        await expect(entry).toBeVisible();

        const classes = await entry.getAttribute('class');
        expect(classes).toContain('hook-entry');
        expect(classes).toContain('vfc-event');
        // The JsCallback marker must be evaluated, never emitted as a class name
        expect(classes).not.toContain('__jsCallback');
    });

    base('callback set after attach applies and keeps vfc-event', async ({ page }) => {
        await page.locator('#set-entry-class-callback').click();
        await waitForVaadin(page);

        const entry = page.locator('.vfc-event').first();
        await expect(entry).toHaveClass(/hook-entry-runtime/, { timeout: 5000 });
        await expect(entry).toHaveClass(/vfc-event/);
    });

    base('plain string set after attach applies and keeps vfc-event', async ({ page }) => {
        const pageErrors = [];
        page.on('pageerror', (err) => pageErrors.push(err.message));

        await page.locator('#set-entry-class-string').click();
        await waitForVaadin(page);

        const entry = page.locator('.vfc-event').first();
        await expect(entry).toHaveClass(/hook-entry-string/, { timeout: 5000 });
        await expect(entry).toHaveClass(/vfc-event/);
        expect(pageErrors).toEqual([]);
    });
});
