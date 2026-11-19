// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/** Clicks Ping and waits until the server counted it, so it handled every event the client sent before. */
async function ping(page, expected) {
    await page.getByRole('button', { name: 'Ping' }).click();
    await expect(page.locator('#ping-count')).toHaveText(String(expected));
}

test.describe('viewDidMount beside the calendar\'s own handler', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/view-did-mount');
        await page.waitForSelector('#cal-field .fc-view');
        await page.waitForSelector('#cal-raw .fc-view');
        await waitForVaadin(page);
    });

    test('a field callback set initially runs, and the server still gets the event', async ({ page }) => {
        await expect(page.locator('#field-count')).toHaveText('1');
        await expect(page.locator('#field-server-count')).toHaveText('1');

        await page.getByRole('button', { name: 'Show week' }).click();

        await expect(page.locator('#field-count')).toHaveText('2');
        await expect(page.locator('#field-server-count')).toHaveText('2');
    });

    test('a field callback set later runs, and the server still gets the event', async ({ page }) => {
        await expect(page.locator('#field-server-count')).toHaveText('1');
        await page.getByRole('button', { name: 'Set other callback' }).click();
        await ping(page, 1);

        await page.getByRole('button', { name: 'Show week' }).click();

        await expect(page.locator('#other-count')).toHaveText('1');
        await expect(page.locator('#field-server-count')).toHaveText('2');
        await expect(page.locator('#field-count')).toHaveText('1');
    });

    test('after clearing the field, the server still gets the event', async ({ page }) => {
        await expect(page.locator('#field-server-count')).toHaveText('1');
        await page.getByRole('button', { name: 'Clear callback' }).click();
        await ping(page, 1);

        await page.getByRole('button', { name: 'Show week' }).click();

        await expect(page.locator('#field-server-count')).toHaveText('2');
        await expect(page.locator('#field-count')).toHaveText('1');
    });

    test('a raw option set later replaces the calendar\'s own handler', async ({ page }) => {
        await expect(page.locator('#field-server-count')).toHaveText('1');
        await page.getByRole('button', { name: 'Set raw callback' }).click();
        await ping(page, 1);

        await page.getByRole('button', { name: 'Show week' }).click();
        await expect(page.locator('#raw-count')).toHaveText('1');
        await ping(page, 2);

        await expect(page.locator('#field-server-count')).toHaveText('1');
        await expect(page.locator('#field-count')).toHaveText('1');
    });

    test('a raw option set initially replaces the calendar\'s own handler', async ({ page }) => {
        await expect(page.locator('#initial-raw-count')).toHaveText('1');
        await ping(page, 1);

        await expect(page.locator('#raw-server-count')).toHaveText('0');
    });
});
