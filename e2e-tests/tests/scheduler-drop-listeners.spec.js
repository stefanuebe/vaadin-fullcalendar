// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Drop listeners on the scheduler (#306). A listener added with addEntryDroppedListener fires on a drop in a
 * resource view and receives the scheduler event. With a second listener of the scheduler kind that applies the
 * changes, each listener sees the drop once and the entry is not reverted.
 */
test.describe('Scheduler drop listeners (#306)', () => {

    test('both listener kinds fire once and the applied drop is not reverted', async ({ page }) => {
        await page.goto('/test/scheduler-drop-listeners');
        const entry = page.locator('.fc-event:has-text("Drag Me")').first();
        await expect(entry).toBeVisible({ timeout: 10000 });
        await waitForVaadin(page);

        const before = await entry.boundingBox();
        if (!before) throw new Error('Could not get bounding box for the entry');
        const startX = before.x + before.width / 2;
        const startY = before.y + before.height / 2;

        await page.mouse.move(startX, startY);
        await page.mouse.down();
        await page.mouse.move(startX + 150, startY, { steps: 10 });
        await page.mouse.up();

        // the counters are the visible effect of the round trip, wait for them first. Both listeners run in the same
        // round trip, so a count of 1 in the response means each listener saw the drop once
        await expect(page.locator('#scheduler-count')).toHaveText('1', { timeout: 5000 });
        await expect(page.locator('#plain-count')).toHaveText('1');
        await expect(page.locator('#plain-type')).toHaveText('EntryDroppedSchedulerEvent');
        await expect(page.locator('#server-start')).not.toHaveText('2025-03-03T09:00');
        await waitForVaadin(page);

        // the revert check runs in the response of the drop, so the entry must have stayed at the new place
        const after = await page.locator('.fc-event:has-text("Drag Me")').first().boundingBox();
        if (!after) throw new Error('Could not get bounding box for the entry after the drop');
        expect(after.x).toBeGreaterThan(before.x + 50);
    });
});
