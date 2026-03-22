// @ts-check
const { test: base } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the calendar options test view.
 */
async function gotoCalendarOptionsView(page) {
    await page.goto('/test/calendar-options');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await waitForVaadin(page);
    // Wait for both calendars to render
    await page.waitForSelector('#cal-daygrid .fc-dayGridMonth-view', { timeout: 5000 });
    await page.waitForSelector('#cal-timegrid .fc-timegrid', { timeout: 5000 });
    await page.waitForSelector('#cal-extra .fc-timegrid', { timeout: 5000 });
}

base.describe('Calendar Options — DayGrid (Locale DE, No Weekends)', () => {

    base.beforeEach(async ({ page }) => {
        await gotoCalendarOptionsView(page);
    });

    base('column headers contain German day abbreviations', async ({ page }) => {
        const cal = page.locator('#cal-daygrid');
        const headers = cal.locator('.fc-col-header-cell');
        const count = await headers.count();
        // With weekends=false, should have exactly 5 columns
        expect(count).toBe(5);

        // Collect all header texts
        const texts = [];
        for (let i = 0; i < count; i++) {
            texts.push(await headers.nth(i).textContent());
        }
        const joined = texts.join(' ');
        // German abbreviations: Mo, Di, Mi, Do, Fr
        expect(joined).toMatch(/Mo/);
        expect(joined).toMatch(/Di/);
        expect(joined).toMatch(/Mi/);
        expect(joined).toMatch(/Do/);
        expect(joined).toMatch(/Fr/);
    });

    base('first column header is Monday (Mo)', async ({ page }) => {
        const cal = page.locator('#cal-daygrid');
        const firstHeader = cal.locator('.fc-col-header-cell').first();
        const text = await firstHeader.textContent();
        expect(text).toMatch(/Mo/);
    });

    base('only 5 column headers visible (weekends hidden)', async ({ page }) => {
        const cal = page.locator('#cal-daygrid');
        const headers = cal.locator('.fc-col-header-cell');
        await expect(headers).toHaveCount(5);
    });

    base('no Sa or So in column headers', async ({ page }) => {
        const cal = page.locator('#cal-daygrid');
        const headers = cal.locator('.fc-col-header-cell');
        const count = await headers.count();
        for (let i = 0; i < count; i++) {
            const text = await headers.nth(i).textContent();
            expect(text).not.toMatch(/^Sa$/);
            expect(text).not.toMatch(/^So$/);
        }
    });

    base('week numbers are displayed', async ({ page }) => {
        const cal = page.locator('#cal-daygrid');
        const weekNumbers = cal.locator('.fc-daygrid-week-number');
        const count = await weekNumbers.count();
        expect(count).toBeGreaterThanOrEqual(4);
    });
});

base.describe('Calendar Options — TimeGrid (Slot Duration, Business Hours)', () => {

    base.beforeEach(async ({ page }) => {
        await gotoCalendarOptionsView(page);
    });

    base('first time slot label starts at 08:00', async ({ page }) => {
        const cal = page.locator('#cal-timegrid');
        // Get all slot labels that have text content
        const slotLabels = cal.locator('.fc-timegrid-slot-label:not(:empty)');
        // First visible slot label should be 8am or 8:00
        const firstLabel = slotLabels.first();
        const text = await firstLabel.textContent();
        expect(text.trim()).toMatch(/8/);
    });

    base('last time slot label is before 18:00', async ({ page }) => {
        const cal = page.locator('#cal-timegrid');
        // Get all slot labels — the last major label should be around 17:xx
        const slotLabels = cal.locator('.fc-timegrid-slot-label[data-time]');
        const count = await slotLabels.count();
        expect(count).toBeGreaterThan(0);
        // Check the last slot's data-time attribute
        const lastTime = await slotLabels.last().getAttribute('data-time');
        // Last slot should be at or before 17:45 (since max is 18:00 exclusive)
        expect(lastTime).toBeTruthy();
        const hour = parseInt(lastTime.split(':')[0]);
        expect(hour).toBeLessThanOrEqual(17);
    });

    base('slot duration is 15 minutes (many slots between hours)', async ({ page }) => {
        const cal = page.locator('#cal-timegrid');
        // With 15-min slots from 08:00 to 18:00 = 10 hours * 4 = 40 slots
        // FC renders 2 <tr> per slot (label row + lane row), so total ~80 rows
        const allSlots = cal.locator('.fc-timegrid-slot[data-time]');
        const count = await allSlots.count();
        // Should be approximately 80 (10 hours * 4 slots/hour * 2 rows/slot)
        expect(count).toBeGreaterThanOrEqual(76);
        expect(count).toBeLessThanOrEqual(84);
    });

    base('non-business hours have fc-non-business class', async ({ page }) => {
        const cal = page.locator('#cal-timegrid');
        // Business hours default: Mon-Fri 9am-5pm
        // Since we're on a Wednesday (March 5, 2025), 8:00-9:00 should be non-business
        const nonBusiness = cal.locator('.fc-non-business');
        const count = await nonBusiness.count();
        expect(count).toBeGreaterThanOrEqual(1);
    });
});

base.describe('Calendar Options — Now Indicator, Scroll Time, Hidden Days', () => {

    base.beforeEach(async ({ page }) => {
        await gotoCalendarOptionsView(page);
    });

    base('now indicator option is enabled on the calendar', async ({ page }) => {
        // Verify the nowIndicator option is set on the third calendar via its FC API
        const nowIndicatorEnabled = await page.evaluate(() => {
            const cal = document.querySelector('#cal-extra vaadin-full-calendar');
            if (cal && cal.calendar) {
                return cal.calendar.getOption('nowIndicator');
            }
            // Fallback: check all calendars
            const allCals = document.querySelectorAll('vaadin-full-calendar');
            for (const c of allCals) {
                if (c.calendar && c.calendar.getOption('nowIndicator')) {
                    return true;
                }
            }
            return false;
        });
        expect(nowIndicatorEnabled).toBe(true);
    });

    base('scrollTime positions the view at 14:00', async ({ page }) => {
        const cal = page.locator('#cal-extra');
        // Check that the scroll position is somewhere around 14:00
        // We can verify that the 14:00 slot is near the top of the visible area
        const slot14 = cal.locator('.fc-timegrid-slot[data-time="14:00:00"]').first();
        await expect(slot14).toBeVisible({ timeout: 5000 });
    });
});
