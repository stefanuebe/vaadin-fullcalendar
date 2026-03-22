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

    test('ENTRY_DID_MOUNT callback sets data-did-mount attribute via setCallbackOption', async ({ page }) => {
        const entry = page.locator('.fc-event[data-did-mount="true"]').first();
        await expect(entry).toBeVisible({ timeout: 10000 });
    });

    test('native click listener increments counter on entry click', async ({ page }) => {
        await expect(page.locator('#click-count')).toHaveText('0');

        const entry = page.locator('.fc-event:has-text("Click Me")').first();
        await expect(entry).toBeVisible({ timeout: 10000 });
        await entry.click();

        await expect(page.locator('#click-count')).not.toHaveText('0', { timeout: 5000 });
    });
});
