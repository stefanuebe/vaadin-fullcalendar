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

    test('calendar shows the local time of the browser before the browser has reported its time zone', async ({ page }) => {
        // holds the time zone report of the browser, so the server cannot answer it yet. Vaadin names the called
        // server method in clear text in the request body.
        const held = [];
        await page.route(/v-r=uidl/, route => {
            if ((route.request().postData() || '').includes('setBrowserTimezone')) {
                held.push(route);
            } else {
                route.continue();
            }
        });

        await page.goto('/test/auto-browser-timezone');
        await page.waitForSelector('.fc', { timeout: 15000 });
        await expect.poll(() => held.length, { message: 'the time zone report is held' }).toBeGreaterThan(0);

        const timeZone = await page.evaluate(() => {
            const calendarEl = document.querySelector('[data-testid="calendar"]');
            // @ts-ignore — custom element exposes FC's internal Calendar instance
            return calendarEl.calendar.getOption('timeZone');
        });
        expect(timeZone).toBe('local');

        await Promise.all(held.map(route => route.continue()));
        await page.unroute(/v-r=uidl/);
        await expect(page.locator('#calendar-timezone')).toHaveText('America/New_York');
    });
});
