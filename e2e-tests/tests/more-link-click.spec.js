// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

/**
 * Option.MORE_LINK_CLICK decides what the "+N more" link does, and the server receives every click (#259).
 *
 * The test view sets MoreLinkClickAction.DAY before attach, or with the parameter "callback" a JsCallback returning
 * "week". A button sets that callback after attach.
 */
test.describe('More link click option', () => {

    async function gotoView(page, path) {
        await page.goto(path);
        await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 15000 });
        await waitForVaadin(page);
    }

    test('action set before attach is applied and the server receives the click', async ({ page }) => {
        await gotoView(page, '/test/more-link-click');

        await page.locator('.fc-daygrid-more-link').first().click();

        await expect(page.locator('.fc-dayGridDay-view, .fc-timeGridDay-view')).toHaveCount(1);
        await expect(page.locator('.fc-popover')).toHaveCount(0);
        await expect(page.locator('#more-link-count')).toHaveText('1');
    });

    test('callback set before attach is applied and the server receives the click', async ({ page }) => {
        await gotoView(page, '/test/more-link-click/callback');

        await page.locator('.fc-daygrid-more-link').first().click();

        await expect(page.locator('.fc-timeGridWeek-view, .fc-dayGridWeek-view')).toHaveCount(1);
        await expect(page.locator('#more-link-count')).toHaveText('1');
    });

    test('callback set after attach is applied and the server receives the click', async ({ page }) => {
        await gotoView(page, '/test/more-link-click');

        await page.locator('[data-testid="btn-callback"]').click();
        await waitForVaadin(page);

        await page.locator('.fc-daygrid-more-link').first().click();

        await expect(page.locator('.fc-dayGridMonth-view')).toHaveCount(0);
        await expect(page.locator('.fc-timeGridWeek-view, .fc-dayGridWeek-view')).toHaveCount(1);
        await expect(page.locator('#more-link-count')).toHaveText('1');
    });
});
