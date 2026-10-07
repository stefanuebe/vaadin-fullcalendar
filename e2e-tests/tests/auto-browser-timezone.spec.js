// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

/**
 * FullCalendar.withAutoBrowserTimezone(): the time zone the browser reports becomes the calendar's time zone (#273).
 */
test.describe('Auto browser time zone', () => {
    test.use({ timezoneId: 'America/New_York' });

    test('browser time zone becomes the calendar time zone on server and client', async ({ page }) => {
        await page.goto('/test/auto-browser-timezone');
        await page.waitForSelector('.fc', { timeout: 15000 });
        await waitForVaadin(page);

        await expect(page.locator('#calendar-timezone')).toHaveText('America/New_York');
        await expect.poll(() => page.evaluate(() => {
            const calendarEl = document.querySelector('[data-testid="calendar"]');
            // @ts-ignore — custom element exposes FC's internal Calendar instance
            return calendarEl.calendar.getOption('timeZone');
        })).toBe('America/New_York');
    });
});
