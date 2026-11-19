// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

// External drops onto a time grid (#454, #459). The view shows the week of 2025-03-10 in the UTC time zone.

async function gotoView(page) {
    await page.goto('/test/external-drop-cases');
    await page.waitForSelector('.fc-timegrid-slot', { timeout: 10000 });
    await waitForVaadin(page);
}

/** Drags the source element onto the 10:00 slot of Wednesday, 2025-03-12. */
async function dragToWednesdayTen(page, source) {
    const slot = page.locator('.fc-timegrid-slot-lane[data-time="10:00:00"]');
    await slot.scrollIntoViewIfNeeded();
    const column = page.locator('.fc-timegrid-col[data-date="2025-03-12"]');

    const srcBox = await source.boundingBox();
    const slotBox = await slot.boundingBox();
    const colBox = await column.boundingBox();
    if (!srcBox || !slotBox || !colBox) throw new Error('Could not get bounding boxes');

    await page.mouse.move(srcBox.x + srcBox.width / 2, srcBox.y + srcBox.height / 2);
    await page.mouse.down();
    await page.mouse.move(colBox.x + colBox.width / 2, slotBox.y + slotBox.height / 2, { steps: 15 });
    await page.mouse.up();
    await waitForVaadin(page);
}

test.describe('External drops onto a time grid', () => {

    test.beforeEach(async ({ page }) => {
        await gotoView(page);
    });

    test('a draggable without entry data fires only the DropEvent, at the time of the slot', async ({ page }) => {
        await dragToWednesdayTen(page, page.locator('#plain-draggable'));

        await expect(page.locator('#drop-count')).toHaveText('1');
        await expect(page.locator('#drop-datetime')).toHaveText('2025-03-12T10:00');
        await expect(page.locator('#drop-component')).toHaveText('plain-draggable');
        await expect(page.locator('#drop-created-start')).toHaveText('2025-03-12T10:00');

        // "no EntryReceiveEvent" cannot be read from the page directly, because it could still be on its way. A second
        // drop that does fire one arrives after it, so once its event is there, the count shows whether the first
        // drop fired one, too.
        await dragToWednesdayTen(page, page.locator('#task-a'));
        await expect(page.locator('#receive-title')).toHaveText('Task A');
        await expect(page.locator('#receive-count')).toHaveText('1');
    });

    test('an item of a container draggable resolves to the draggable and carries the callback data', async ({ page }) => {
        await dragToWednesdayTen(page, page.locator('#task-a'));

        await expect(page.locator('#drop-count')).toHaveText('1');
        await expect(page.locator('#drop-component')).toHaveText('task-list');
        await expect(page.locator('#drop-created-title')).toHaveText('Task A');

        await expect(page.locator('#receive-count')).toHaveText('1');
        await expect(page.locator('#receive-title')).toHaveText('Task A');
    });

    test('an item of a container draggable takes the entry template of the container', async ({ page }) => {
        await dragToWednesdayTen(page, page.locator('#template-item'));

        await expect(page.locator('#drop-count')).toHaveText('1');
        await expect(page.locator('#drop-component')).toHaveText('template-list');
        await expect(page.locator('#drop-created-title')).toHaveText('Template');

        await expect(page.locator('#receive-count')).toHaveText('1');
        await expect(page.locator('#receive-title')).toHaveText('Template');
    });
});
