// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

/**
 * Value a callback stored in window.cbThis. True means that its this was the expected object.
 */
const recorded = (page, key) => page.evaluate((k) => window.cbThis?.[k], key);

/**
 * Calls the option's function the way a caller without a this would, so the result depends only on the binding.
 */
const callOption = (page, calendarId, option) => page.evaluate(([id, name]) => {
    const calendar = document.getElementById(id).calendar;
    const callback = calendar.getOption(name);
    callback({});
}, [calendarId, option]);

test.describe('Callback this', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/callback-this');
        await page.waitForSelector('#cal-before .fc-event', { timeout: 10000 });
        await waitForVaadin(page);
    });

    test('entry render hook set before attach has the calendar as this', async ({ page }) => {
        await expect.poll(() => recorded(page, 'before-mount')).toBe(true);
    });

    test('selectOverlap set before attach has the calendar as this', async ({ page }) => {
        await callOption(page, 'cal-before', 'selectOverlap');
        expect(await recorded(page, 'before-selectOverlap')).toBe(true);
    });

    test('entry render hook set after attach has the calendar as this', async ({ page }) => {
        await page.locator('#set-after').click();
        await expect.poll(() => page.evaluate(
            () => typeof document.getElementById('cal-after').calendar.getOption('eventDidMount'))).toBe('function');
        // entries that are already mounted are not mounted again, so a navigation mounts them anew
        await page.evaluate(() => {
            const calendar = document.getElementById('cal-after').calendar;
            calendar.next();
            calendar.prev();
        });
        await expect.poll(() => recorded(page, 'after-mount')).toBe(true);
    });

    test('selectOverlap set after attach has the calendar as this', async ({ page }) => {
        await page.locator('#set-after').click();
        // the option arrives after the round trip
        await expect.poll(() => page.evaluate(
            () => typeof document.getElementById('cal-after').calendar.getOption('selectOverlap'))).toBe('function');
        await callOption(page, 'cal-after', 'selectOverlap');
        expect(await recorded(page, 'after-selectOverlap')).toBe(true);
    });

    test('click of a custom button in the buttons option has the calendar as this', async ({ page }) => {
        await page.locator('#cal-before button:has-text("Custom")').click();
        await expect.poll(() => recorded(page, 'before-button')).toBe(true);
    });

    // guards the fallback to the library's own this while the calendar is created. Reading the element's calendar
    // there would create a second calendar and recurse, and a plain undefined would lose the calendar.
    test('a callback the library calls while creating the calendar has the calendar as this', async ({ page }) => {
        const isCalendar = await page.evaluate(
            () => window.cbThis?.['before-validRange'] === document.getElementById('cal-before').calendar);
        expect(isCalendar).toBe(true);
    });

    test('an arrow function callback keeps its lexical this and works', async ({ page }) => {
        await callOption(page, 'cal-before', 'eventAllow');
        expect(await recorded(page, 'before-arrow')).toBe(true);
    });
});
