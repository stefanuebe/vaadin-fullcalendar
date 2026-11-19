// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

// The entry events of an entry from a remote entry source (#456). The server does not know such an entry, so it fires
// the RemoteEntry...Events instead of the entry events.

async function gotoView(page) {
    await page.goto('/test/remote-entry-events');
    await page.waitForSelector('.fc-timegrid-slot', { timeout: 10000 });
    await expect(page.locator('.fc-event:has-text("Remote entry")')).toBeVisible();
    await waitForVaadin(page);
}

const remoteEntry = page => page.locator('.fc-event:has-text("Remote entry")');

test.describe('Entry events of a remote entry source', () => {

    test.beforeEach(async ({ page }) => {
        await gotoView(page);
    });

    test('a click fires the RemoteEntryClickedEvent, a click on a server entry still the EntryClickedEvent', async ({ page }) => {
        await remoteEntry(page).click();
        await expect(page.locator('#event-log')).toContainText('click:remote-1:feed');

        await page.locator('.fc-event:has-text("Server entry")').click();
        await expect(page.locator('#event-log')).toContainText('serverclick:server-1');

        // both round trips are done, so each click has fired only its own event
        const tokens = (await page.locator('#event-log').textContent() ?? '').split(' ');
        expect(tokens).not.toContain('serverclick:remote-1');
        expect(tokens.filter(t => t.startsWith('click:'))).toEqual(['click:remote-1:feed']);
    });

    test('the mouse fires the RemoteEntryMouseEnterEvent and the RemoteEntryMouseLeaveEvent', async ({ page }) => {
        await remoteEntry(page).hover();
        await expect(page.locator('#event-log')).toContainText('mouseenter:remote-1:feed');

        await page.locator('#event-log').hover();
        await expect(page.locator('#event-log')).toContainText('mouseleave:remote-1:feed');
    });

    test('a drag fires the RemoteEntryDragStartEvent and the RemoteEntryDragStopEvent', async ({ page }) => {
        const box = await remoteEntry(page).boundingBox();
        if (!box) throw new Error('No bounding box for the remote entry');

        await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
        await page.mouse.down();
        await page.mouse.move(box.x + box.width / 2 + 120, box.y + box.height / 2, { steps: 10 });
        await page.mouse.up();

        await expect(page.locator('#event-log')).toContainText('dragstart:remote-1:feed');
        await expect(page.locator('#event-log')).toContainText('dragstop:remote-1:feed');
    });

    test('a resize fires the RemoteEntryResizeStartEvent and the RemoteEntryResizeStopEvent', async ({ page }) => {
        await remoteEntry(page).hover();
        const resizer = remoteEntry(page).locator('.fc-event-resizer-end');
        await expect(resizer).toBeVisible();
        const box = await resizer.boundingBox();
        if (!box) throw new Error('No bounding box for the resizer');

        await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
        await page.mouse.down();
        await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2 + 60, { steps: 10 });
        await page.mouse.up();

        await expect(page.locator('#event-log')).toContainText('resizestart:remote-1:feed');
        await expect(page.locator('#event-log')).toContainText('resizestop:remote-1:feed');
    });
});
