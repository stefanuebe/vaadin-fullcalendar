// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

async function gotoNativeEventListenerView(page) {
    await page.goto('/test/native-event-listener');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await page.waitForSelector('.fc-event', { timeout: 5000 });
    await waitForVaadin(page);
}

test.describe('Native Entry Event Listener', () => {

    test.beforeEach(async ({ page }) => {
        await gotoNativeEventListenerView(page);
    });

    test('entryDidMount callback and native listener both run on the same entry', async ({ page }) => {
        const entry = page.locator('.fc-event:has-text("Click Me")').first();
        await expect(entry).toHaveAttribute('data-did-mount', 'true', { timeout: 10000 });
        await expect(page.locator('#click-count')).toHaveText('0');

        await entry.click();

        await expect(page.locator('#click-count')).toHaveText('1');
    });
});
