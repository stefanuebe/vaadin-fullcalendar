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
        await page.waitForSelector('.fc', { timeout: 15000 });
        await page.waitForSelector('.fc-event', { timeout: 15000 });
        await waitForVaadin(page);
    });

    test('no entry is draggable when calendar-level editable is false', async ({ page }) => {
        // Both entries exist in the DOM
        const events = page.locator('.fc-event');
        await expect(events).toHaveCount(2);

        // None carries the draggable marker class
        const draggable = page.locator('.fc-event.fc-event-draggable');
        await expect(draggable).toHaveCount(0);
    });
});
