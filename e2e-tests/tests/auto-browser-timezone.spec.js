// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

/**
 * Holds the time zone report of the browser, so the server cannot answer it yet. Vaadin names the called server method
 * in clear text in the request body. Returns the held requests.
 */
async function holdBrowserReport(page) {
    const held = [];
    await page.route(/v-r=uidl/, route => {
        if ((route.request().postData() || '').includes('setBrowserTimezone')) {
            held.push(route);
        } else {
            route.continue();
        }
    });
    return held;
}

/**
 * Timezone.LOCAL: the time zone the browser reports becomes the server's time zone (#273).
 */
test.describe('Auto browser time zone', () => {
    test.use({ timezoneId: 'America/New_York' });

    test('browser time zone becomes the server\'s time zone, the client keeps showing local time', async ({ page }) => {
        await page.goto('/test/auto-browser-timezone');
        await page.waitForSelector('.fc', { timeout: 15000 });
        await waitForVaadin(page);

        await expect(page.locator('#calendar-timezone')).toHaveText('America/New_York');
        // the report changes nothing on the client, which already shows the browser's zone. A call that changed it
        // would come with the same response, so the calls of that response must have run.
        await waitForVaadin(page);
        expect(await page.evaluate(() => {
            const calendarEl = document.querySelector('[data-testid="calendar"]');
            // @ts-ignore — custom element exposes FC's internal Calendar instance
            return calendarEl.calendar.getOption('timeZone');
        })).toBe('local');
    });

    test('calendar shows the local time of the browser before the browser has reported its time zone', async ({ page }) => {
        const held = await holdBrowserReport(page);

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

    test('the browser\'s report fetches no entries', async ({ page }) => {
        // the fetch counter is in place before the server answers the report
        const held = await holdBrowserReport(page);
        await page.goto('/test/auto-browser-timezone');
        await page.waitForSelector('.fc', { timeout: 15000 });
        await expect.poll(() => held.length, { message: 'the time zone report is held' }).toBeGreaterThan(0);
        await page.evaluate(() => {
            const calendarEl = document.querySelector('[data-testid="calendar"]');
            // counts the fetches where the client makes them
            // @ts-ignore
            const fetch = calendarEl.$server.fetchEntriesFromServer;
            window.reportFetches = 0;
            // @ts-ignore
            calendarEl.$server.fetchEntriesFromServer = (...args) => {
                window.reportFetches++;
                // @ts-ignore
                return fetch.apply(calendarEl.$server, args);
            };
        });

        await Promise.all(held.map(route => route.continue()));
        await page.unroute(/v-r=uidl/);
        await expect(page.locator('#calendar-timezone')).toHaveText('America/New_York');
        // a change the report caused would come with the response that shows the zone. waitForVaadin waits until the
        // client calls of that response have run.
        await waitForVaadin(page);

        expect(await page.evaluate(() => window.reportFetches)).toBe(0);
    });
});
