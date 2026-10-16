// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

/**
 * A calendar with no options set gets the library defaults where the add-on used to set its own: no week
 * numbers, no draggable entries, no sticky headers or scrollbars, and the browser's time zone. The calendar keeps its
 * full height. View: /test/library-defaults, March 2025 with one timed entry, only the initial date set.
 */

const calendarOf = (page) => page.locator('#cal-library-defaults');

test.describe('A calendar with no options set', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/test/library-defaults');
        await page.waitForSelector('#cal-library-defaults .fc-event', { timeout: 10000 });
        await waitForVaadin(page);
    });

    test('shows no week numbers', async ({ page }) => {
        await expect(calendarOf(page).locator('.fc-daygrid-week-number')).toHaveCount(0);
    });

    test('has no draggable entries', async ({ page }) => {
        await expect(calendarOf(page).locator('.fc-event')).toHaveCount(1);
        await expect(calendarOf(page).locator('.fc-event.fc-event-draggable')).toHaveCount(0);
    });

    test('leaves the sticky options to the library and fills the height of its container', async ({ page }) => {
        const state = await calendarOf(page).evaluate((el) => ({
            tableHeaderSticky: el.calendar.getOption('tableHeaderSticky'),
            footerScrollbarSticky: el.calendar.getOption('footerScrollbarSticky'),
            height: el.calendar.getOption('height'),
        }));

        // unset, the library treats them as "auto", which sticks only when the height is automatic
        expect(state.tableHeaderSticky).toBeUndefined();
        expect(state.footerScrollbarSticky).toBeUndefined();
        expect(state.height).toBe('100%');
    });

    test('shows the times in the browser\'s time zone', async ({ page }) => {
        expect(await calendarOf(page).evaluate((el) => el.calendar.getOption('timeZone'))).toBe('local');
    });
});
