// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/** Clicks Ping and waits until the server counted it, so it handled every event the client sent before. */
async function ping(page, expected) {
    await page.getByRole('button', { name: 'Ping' }).click();
    await expect(page.locator('#ping-count')).toHaveText(String(expected));
}

function dayLink(page) {
    return page.locator('.fc-daygrid-day[data-date="2025-03-12"] .fc-daygrid-day-number').first();
}

test.describe('nav link clicks beside the calendar\'s own handlers', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/nav-link-click');
        await page.waitForSelector('#cal-nav-link .fc-daygrid-day-number');
        await waitForVaadin(page);
    });

    test('a day link click runs the field callback, and the server still gets the event', async ({ page }) => {
        await dayLink(page).click();

        await expect(page.locator('#day-count')).toHaveText('1');
        await expect(page.locator('#day-server-count')).toHaveText('1');
        await expect(page.locator('#week-count')).toHaveText('0');
        await expect(page.locator('#week-server-count')).toHaveText('0');
    });

    test('a week link click runs the field callback, and the server still gets the event', async ({ page }) => {
        await page.locator('#cal-nav-link .fc-daygrid-week-number').first().click();

        await expect(page.locator('#week-count')).toHaveText('1');
        await expect(page.locator('#week-server-count')).toHaveText('1');
        await expect(page.locator('#day-count')).toHaveText('0');
        await expect(page.locator('#day-server-count')).toHaveText('0');
    });

    test('after clearing the field, the server still gets the event', async ({ page }) => {
        await page.getByRole('button', { name: 'Clear day callback' }).click();
        await ping(page, 1);

        await dayLink(page).click();

        await expect(page.locator('#day-server-count')).toHaveText('1');
        await expect(page.locator('#day-count')).toHaveText('0');
    });

    test('a raw option replaces the calendar\'s own handler', async ({ page }) => {
        await page.getByRole('button', { name: 'Set raw day callback' }).click();
        await ping(page, 1);

        await dayLink(page).click();
        await expect(page.locator('#raw-day-count')).toHaveText('1');
        await ping(page, 2);

        await expect(page.locator('#day-server-count')).toHaveText('0');
        await expect(page.locator('#day-count')).toHaveText('0');
    });
});
