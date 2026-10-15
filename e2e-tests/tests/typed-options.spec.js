// @ts-check
const { test, expect } = require('./fixtures');

test.describe('Typed options of calendar.options()', () => {

    test('an initial date set before attach takes effect at creation', async ({ page }) => {
        await page.goto('/test/typed-options');

        await expect(page.locator('#cal-typed-options [data-date="2025-03-15"]').first()).toBeVisible({ timeout: 10000 });
    });

    test('a value-or-callback option applies in both forms', async ({ page }) => {
        await page.goto('/test/typed-options');
        const cell = page.locator('#cal-typed-options .fc-daygrid-day[data-date="2025-03-15"]');
        await expect(cell).toBeVisible({ timeout: 10000 });

        await page.getByRole('button', { name: 'Day cell class as text' }).click();
        await expect(cell).toHaveClass(/\btyped-day\b/);

        await page.getByRole('button', { name: 'Day cell class from callback' }).click();
        await expect(cell).toHaveClass(/\bcallback-day\b/);
        await expect(cell).not.toHaveClass(/\btyped-day\b/);
    });
});
