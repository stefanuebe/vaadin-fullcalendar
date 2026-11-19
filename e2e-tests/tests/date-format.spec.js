// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Dates the client sends to the server as a day (all-day entries, view ranges, day clicks) must be
 * plain yyyy-MM-dd strings in the calendar's time zone, or the server fails to parse them.
 */
test.describe('Day dates sent to the server', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/listener-data');
        await page.waitForSelector('.fc-view', { timeout: 10000 });
        await waitForVaadin(page);
    });

    // formats the instant as a day in the given calendar time zone
    const formatDay = (page, timeZone, iso) => page.evaluate(([timeZone, iso]) => {
        const el = document.querySelector('vaadin-full-calendar');
        el.calendar.setOption('timeZone', timeZone);
        return el.formatDate(new Date(iso), true);
    }, [timeZone, iso]);

    test('midnight in a named time zone', async ({ page }) => {
        expect(await formatDay(page, 'America/New_York', '2025-11-02T04:00:00Z')).toBe('2025-11-02');
    });

    test('a time that is not midnight in a named time zone', async ({ page }) => {
        // 23:00 in New York. convertToEventData computes this end (start + 24 h) for an all-day entry
        // without end on the 25-hour DST day.
        expect(await formatDay(page, 'America/New_York', '2025-11-03T04:00:00Z')).toBe('2025-11-02');
    });

    test('a time that is not midnight in UTC', async ({ page }) => {
        expect(await formatDay(page, 'UTC', '2025-11-02T23:00:00Z')).toBe('2025-11-02');
    });
});

test.describe('End of an all-day entry without end', () => {

    test('is the next day on the 25-hour DST day', async ({ page }) => {
        await page.goto('/test/all-day-dst');
        await page.waitForSelector('.fc-event', { timeout: 10000 });
        await waitForVaadin(page);

        await page.locator('.fc-event', { hasText: 'DST day' }).click();

        // Adding 24 hours to the start gives 23:00 on 2025-11-02 in New York, so the end was the start day
        await expect(page.locator('#clicked-end')).toHaveText('2025-11-03');
    });
});
