// @ts-check
const { test, expect } = require('./fixtures');

test.describe('Typed options of calendar.options()', () => {

    test('an initial date set before attach takes effect at creation', async ({ page }) => {
        await page.goto('/test/typed-options');

        await expect(page.locator('#cal-typed-options [data-date="2025-03-15"]').first()).toBeVisible({ timeout: 10000 });
    });
});
