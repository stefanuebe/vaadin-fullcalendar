// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

test.describe('End of an all-day entry without end', () => {

    test('is the next day on the 25-hour DST day', async ({ page }) => {
        await page.goto('/test/all-day-dst');
        await page.waitForSelector('.fc-event', { timeout: 10000 });
        await waitForVaadin(page);

        await page.locator('.fc-event', { hasText: 'DST day' }).click();

        // Adding 24 hours to the start gives 23:00 on 2025-11-02 in New York, so the end was the start day
        await expect(page.locator('#clicked-end')).toHaveText('2025-11-03');
    });
});
