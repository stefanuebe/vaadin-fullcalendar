// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Auto-revert without drop or resize listeners (#440). Without a listener for a gesture, the browser reverts it
 * itself, because the server never learns of it.
 */

function dragMe(page) {
    return page.locator('.fc-event:has-text("Drag Me")').first();
}

async function boxOf(page) {
    const entry = dragMe(page);
    await expect(entry).toBeVisible({ timeout: 10000 });
    const box = await entry.boundingBox();
    if (!box) throw new Error('Could not get bounding box for Drag Me entry');
    return box;
}

/** Drags the entry into the next day column. Returns its box before the drag. */
async function drag(page) {
    const box = await boxOf(page);
    const x = box.x + box.width / 2;
    const y = box.y + box.height / 2;
    await page.mouse.move(x, y);
    await page.mouse.down();
    await page.mouse.move(x + box.width + 10, y, { steps: 10 });
    // the gesture has started, so an entry at its old place afterwards was reverted
    await expect(page.locator('.fc-event-mirror, .fc-event-dragging').first()).toBeAttached();
    await page.mouse.up();
    // the gesture has ended, so the entry shows where the browser left it
    await expect(page.locator('.fc-event-mirror, .fc-event-dragging')).toHaveCount(0);
    return box;
}

/** Drags the end of the entry down by about two slots. Returns its box before the resize. */
async function resize(page) {
    const box = await boxOf(page);
    await dragMe(page).hover();
    const resizer = dragMe(page).locator('.fc-event-resizer-end');
    await expect(resizer).toBeVisible();
    const handle = await resizer.boundingBox();
    if (!handle) throw new Error('no resizer for "Drag Me"');
    const x = handle.x + handle.width / 2;
    const y = handle.y + handle.height / 2;
    await page.mouse.move(x, y);
    await page.mouse.down();
    await page.mouse.move(x, y + 60, { steps: 5 });
    await expect(page.locator('.fc-event-mirror.fc-event-resizing')).toBeAttached();
    await page.mouse.up();
    await expect(page.locator('.fc-event-mirror, .fc-event-resizing')).toHaveCount(0);
    return box;
}

async function expectAtX(page, x) {
    await expect.poll(async () => Math.abs((await boxOf(page)).x - x)).toBeLessThan(5);
}

async function expectAwayFromX(page, x) {
    await expect.poll(async () => Math.abs((await boxOf(page)).x - x)).toBeGreaterThan(20);
}

async function expectHeight(page, height) {
    await expect.poll(async () => Math.abs((await boxOf(page)).height - height)).toBeLessThan(5);
}

async function expectTallerThan(page, height) {
    await expect.poll(async () => (await boxOf(page)).height - height).toBeGreaterThan(20);
}

/** Clicks a control and waits until the server has shown its effect. */
async function clickAndWait(page, buttonId, counterId, expected) {
    await page.locator('#' + buttonId).click();
    await expect(page.locator('#' + counterId)).toHaveText(expected);
}

test.describe('Auto revert without listener (#440)', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/auto-revert-without-listener');
        await page.waitForSelector('.fc-timegrid-slot', { timeout: 10000 });
        await waitForVaadin(page);
    });

    test('without listeners, a drag is reverted', async ({ page }) => {
        const before = await drag(page);
        await expectAtX(page, before.x);
    });

    test('without listeners, a resize is reverted', async ({ page }) => {
        const before = await resize(page);
        await expectHeight(page, before.height);
    });

    test('auto-revert off, without listeners the drag stays', async ({ page }) => {
        await clickAndWait(page, 'disable-auto-revert', 'auto-revert', 'false');

        const before = await drag(page);
        await waitForVaadin(page);
        await expectAwayFromX(page, before.x);
    });

    test('only a drop listener: the drop stays, a resize is reverted', async ({ page }) => {
        await clickAndWait(page, 'add-drop-listener', 'drop-listeners', '1');

        const beforeDrag = await drag(page);
        await expect(page.locator('#applied-count')).toHaveText('1');
        await waitForVaadin(page);
        await expectAwayFromX(page, beforeDrag.x);

        const beforeResize = await resize(page);
        await expectHeight(page, beforeResize.height);
    });

    test('only a resize listener: the resize stays, a drag is reverted', async ({ page }) => {
        await clickAndWait(page, 'add-resize-listener', 'resize-listeners', '1');

        const beforeResize = await resize(page);
        await expect(page.locator('#applied-count')).toHaveText('1');
        await waitForVaadin(page);
        await expectTallerThan(page, beforeResize.height);

        const beforeDrag = await drag(page);
        await expectAtX(page, beforeDrag.x);
    });

    test('removing one of two drop listeners keeps the drop, removing the last reverts it', async ({ page }) => {
        await clickAndWait(page, 'add-drop-listener', 'drop-listeners', '1');
        await clickAndWait(page, 'add-drop-listener', 'drop-listeners', '2');
        await clickAndWait(page, 'remove-drop-listener', 'drop-listeners', '1');

        const first = await drag(page);
        await expect(page.locator('#applied-count')).toHaveText('1');
        await waitForVaadin(page);
        await expectAwayFromX(page, first.x);

        await clickAndWait(page, 'remove-drop-listener', 'drop-listeners', '0');

        const second = await drag(page);
        await expectAtX(page, second.x);
    });
});
