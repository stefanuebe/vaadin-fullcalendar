// @ts-check
const { test, expect } = require('./fixtures');

test.describe('Options set right before a direct client call', () => {

    test('a date increment set right before next is used by next', async ({ page }) => {
        await page.goto('/test/flush-options');
        await expect(page.locator('#cal-flush-options [data-date="2025-03-15"]').first()).toBeVisible({ timeout: 10000 });

        const button = page.getByRole('button', { name: 'Step a year and go next' });
        await button.click();
        await expect(button).toBeDisabled();

        // next computes the new date when it is called, so it must already know the increment
        await expect(page.locator('#cal-flush-options [data-date="2026-03-15"]').first()).toBeVisible();
    });
});
