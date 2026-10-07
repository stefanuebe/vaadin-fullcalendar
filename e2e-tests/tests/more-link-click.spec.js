// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

/**
 * Option.MORE_LINK_CLICK decides what the "+N more" link does, and the server receives every click (#259).
 *
 * The test view sets MoreLinkClickAction.DAY before attach. A button sets a JsCallback returning "week".
 */
test.describe('More link click option', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/more-link-click');
        await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 15000 });
        await waitForVaadin(page);
    });

    test('action set before attach is applied and the server receives the click', async ({ page }) => {
        await page.locator('.fc-daygrid-more-link').first().click();

        await expect(page.locator('.fc-dayGridMonth-view')).toHaveCount(0);
        await expect(page.locator('.fc-popover')).toHaveCount(0);
        await expect(page.locator('#more-link-count')).toHaveText('1');
    });

    test('callback result is applied and the server receives the click', async ({ page }) => {
        await page.locator('[data-testid="btn-callback"]').click();
        await waitForVaadin(page);

        await page.locator('.fc-daygrid-more-link').first().click();

        await expect(page.locator('.fc-dayGridMonth-view')).toHaveCount(0);
        await expect(page.locator('.fc-timeGridWeek-view, .fc-dayGridWeek-view')).toHaveCount(1);
        await expect(page.locator('#more-link-count')).toHaveText('1');
    });
});
