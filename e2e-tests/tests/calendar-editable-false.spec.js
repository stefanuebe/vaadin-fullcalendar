// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

/**
 * Regression test for issue #212: before 7.2, a calendar-level Option.EDITABLE=false
 * was silently overridden by each entry's implicit editable:true default. From 7.2,
 * an un-set editable field is no longer serialized, so the calendar-level option
 * actually takes effect.
 *
 * This spec verifies the end-to-end DOM behaviour: on a calendar with EDITABLE=false
 * and entries that never called setEditable, FullCalendar must NOT add the
 * fc-event-draggable class to any entry.
 */
test.describe('Calendar-level editable=false (#212)', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/test/calendar-editable-false');
        await page.waitForSelector('.vfc-view', { timeout: 15000 });
        await page.waitForFunction(
            () => document.querySelectorAll('.vfc-event').length > 0,
            { timeout: 15000 }
        );
        await waitForVaadin(page);
    });

    test('no entry is draggable when calendar-level editable is false', async ({ page }) => {
        // Both entries exist in the DOM
        const events = page.locator('.vfc-event');
        await expect(events).toHaveCount(2);

        // v7: draggable entries carry fc-DD (internalEventDraggable) — none should have it when editable=false
        const draggable = page.locator('.vfc-event.fc-DD');
        await expect(draggable).toHaveCount(0);
    });
});
