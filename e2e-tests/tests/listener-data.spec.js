// @ts-check
const { test: base } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the listener data test view.
 */
async function gotoListenerDataView(page) {
    await page.goto('/test/listener-data');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 5000 });
    await page.waitForSelector('.fc-event', { timeout: 5000 });
    await waitForVaadin(page);
}

base.describe('Listener Data — DatesRenderedEvent', () => {

    base.beforeEach(async ({ page }) => {
        await gotoListenerDataView(page);
    });

    base('fires on initial load (count >= 1)', async ({ page }) => {
        // DatesRenderedEvent fires when the calendar first renders
        await expect(page.locator('#dates-rendered-count')).not.toHaveText('0', { timeout: 5000 });
    });

    base('interval start contains 2025-03-01', async ({ page }) => {
        await expect(page.locator('#dates-interval-start')).toHaveText(/2025-03-01/, { timeout: 5000 });
    });

    base('interval end contains 2025-04-01', async ({ page }) => {
        // Month view interval end is first day of next month
        await expect(page.locator('#dates-interval-end')).toHaveText(/2025-04-01/, { timeout: 5000 });
    });

    base('navigating to next month increments count', async ({ page }) => {
        await expect(page.locator('#dates-rendered-count')).not.toHaveText('0', { timeout: 5000 });
        const initialCount = parseInt(await page.locator('#dates-rendered-count').textContent());

        // Navigate to next month via FC JS API (VAADIN theme hides native toolbar)
        await page.evaluate(() => {
            const fcEl = document.querySelector('vaadin-full-calendar');
            if (fcEl && fcEl.calendar) {
                fcEl.calendar.next();
            }
        });
        await waitForVaadin(page);

        // Count should have increased
        await expect(page.locator('#dates-rendered-count')).not.toHaveText(String(initialCount), { timeout: 5000 });
        const newCount = parseInt(await page.locator('#dates-rendered-count').textContent());
        expect(newCount).toBeGreaterThan(initialCount);
    });
});

base.describe('Listener Data — MoreLinkClickedEvent', () => {

    base.beforeEach(async ({ page }) => {
        await gotoListenerDataView(page);
    });

    base('clicking +more link increments counter', async ({ page }) => {
        const moreLink = page.locator('.fc-daygrid-more-link').first();
        await expect(moreLink).toBeVisible({ timeout: 10000 });

        await expect(page.locator('#more-link-count')).toHaveText('0');
        await moreLink.click();
        await waitForVaadin(page);

        await expect(page.locator('#more-link-count')).toHaveText('1', { timeout: 5000 });
    });

    base('more-link-date contains 2025-03-10', async ({ page }) => {
        const moreLink = page.locator('.fc-daygrid-more-link').first();
        await expect(moreLink).toBeVisible({ timeout: 10000 });

        await moreLink.click();
        await waitForVaadin(page);

        await expect(page.locator('#more-link-date')).toHaveText('2025-03-10', { timeout: 5000 });
    });
});

base.describe('Listener Data — TimeslotClickedEvent', () => {

    base.beforeEach(async ({ page }) => {
        await gotoListenerDataView(page);
    });

    base('clicking empty day cell increments counter', async ({ page }) => {
        await expect(page.locator('#timeslot-click-count')).toHaveText('0');

        // Click an empty day cell (March 12 should be empty)
        const emptyCell = page.locator('.fc-daygrid-day[data-date="2025-03-12"] .fc-daygrid-day-frame');
        await emptyCell.click();
        await waitForVaadin(page);

        await expect(page.locator('#timeslot-click-count')).toHaveText('1', { timeout: 5000 });
    });

    base('timeslot-click-date contains the clicked date', async ({ page }) => {
        const emptyCell = page.locator('.fc-daygrid-day[data-date="2025-03-12"] .fc-daygrid-day-frame');
        await emptyCell.click();
        await waitForVaadin(page);

        await expect(page.locator('#timeslot-click-date')).toHaveText('2025-03-12', { timeout: 5000 });
    });

    base('timeslot-click-allday is true in dayGrid month view', async ({ page }) => {
        const emptyCell = page.locator('.fc-daygrid-day[data-date="2025-03-12"] .fc-daygrid-day-frame');
        await emptyCell.click();
        await waitForVaadin(page);

        await expect(page.locator('#timeslot-click-allday')).toHaveText('true', { timeout: 5000 });
    });
});

base.describe('Listener Data — EntryClickedEvent (data)', () => {

    base.beforeEach(async ({ page }) => {
        await gotoListenerDataView(page);
    });

    base('clicking entry increments counter', async ({ page }) => {
        await expect(page.locator('#entry-click-count')).toHaveText('0');

        const entry = page.locator('.fc-event:has-text("Clickable Event")').first();
        await entry.click();
        await waitForVaadin(page);

        await expect(page.locator('#entry-click-count')).toHaveText('1', { timeout: 5000 });
    });

    base('entry-click-title contains the clicked entry title', async ({ page }) => {
        const entry = page.locator('.fc-event:has-text("Clickable Event")').first();
        await entry.click();
        await waitForVaadin(page);

        await expect(page.locator('#entry-click-title')).toHaveText('Clickable Event', { timeout: 5000 });
    });

    base('entry-click-start contains the entry start date', async ({ page }) => {
        const entry = page.locator('.fc-event:has-text("Clickable Event")').first();
        await entry.click();
        await waitForVaadin(page);

        await expect(page.locator('#entry-click-start')).toHaveText('2025-03-05', { timeout: 5000 });
    });
});

base.describe('Listener Data — EntryMouseEnterEvent', () => {

    base.beforeEach(async ({ page }) => {
        await gotoListenerDataView(page);
    });

    base('hovering over entry increments mouse-enter-count', async ({ page }) => {
        await expect(page.locator('#mouse-enter-count')).toHaveText('0');

        const entry = page.locator('.fc-event:has-text("Hover Me")').first();
        await entry.hover();
        await waitForVaadin(page);

        await expect(page.locator('#mouse-enter-count')).toHaveText('1', { timeout: 5000 });
    });

    base('mouse-enter-title contains the hovered entry title', async ({ page }) => {
        const entry = page.locator('.fc-event:has-text("Hover Me")').first();
        await entry.hover();
        await waitForVaadin(page);

        await expect(page.locator('#mouse-enter-title')).toHaveText('Hover Me', { timeout: 5000 });
    });
});

base.describe('Listener Data — EntryMouseLeaveEvent', () => {

    base.beforeEach(async ({ page }) => {
        await gotoListenerDataView(page);
    });

    base('moving mouse away from entry increments mouse-leave-count', async ({ page }) => {
        await expect(page.locator('#mouse-leave-count')).toHaveText('0');

        // Hover the entry first
        const entry = page.locator('.fc-event:has-text("Hover Me")').first();
        await entry.hover();
        await waitForVaadin(page);

        // Move mouse away to an empty area
        const emptyCell = page.locator('.fc-daygrid-day[data-date="2025-03-20"] .fc-daygrid-day-frame');
        await emptyCell.hover();
        await waitForVaadin(page);

        await expect(page.locator('#mouse-leave-count')).toHaveText('1', { timeout: 5000 });
    });
});

base.describe('Listener Data — TimeslotsSelectedEvent', () => {

    base.beforeEach(async ({ page }) => {
        await gotoListenerDataView(page);
    });

    base('selecting date range increments counter and provides start/end', async ({ page }) => {
        await expect(page.locator('#timeslots-selected-count')).toHaveText('0');

        // Programmatically select a range via FC JS API
        await page.evaluate(() => {
            const fcEl = document.querySelector('vaadin-full-calendar');
            if (fcEl && fcEl.calendar) {
                fcEl.calendar.select('2025-03-10', '2025-03-13');
            }
        });
        await waitForVaadin(page);

        await expect(page.locator('#timeslots-selected-count')).toHaveText('1', { timeout: 5000 });
        await expect(page.locator('#timeslots-selected-start')).toHaveText('2025-03-10', { timeout: 5000 });
        await expect(page.locator('#timeslots-selected-end')).toHaveText('2025-03-13', { timeout: 5000 });
        await expect(page.locator('#timeslots-selected-allday')).toHaveText('true', { timeout: 5000 });
    });
});

base.describe('Listener Data — DayNumberClickedEvent', () => {

    base.beforeEach(async ({ page }) => {
        await gotoListenerDataView(page);
    });

    base('clicking a day number fires event with correct date', async ({ page }) => {
        await expect(page.locator('#day-number-count')).toHaveText('0');

        // navLinks is enabled, so day numbers are clickable links
        // Click day number for March 15
        const dayLink = page.locator('.fc-daygrid-day[data-date="2025-03-15"] .fc-daygrid-day-number').first();
        await expect(dayLink).toBeVisible();
        await dayLink.click();
        await waitForVaadin(page);

        await expect(page.locator('#day-number-count')).toHaveText('1', { timeout: 5000 });
        await expect(page.locator('#day-number-date')).toHaveText('2025-03-15', { timeout: 5000 });
    });
});

base.describe('Listener Data — WeekNumberClickedEvent', () => {

    base.beforeEach(async ({ page }) => {
        await gotoListenerDataView(page);
    });

    base('clicking a week number fires event with date', async ({ page }) => {
        await expect(page.locator('#week-number-count')).toHaveText('0');

        // weekNumbers is enabled, click first week number
        const weekLink = page.locator('.fc-daygrid-week-number').first();
        await expect(weekLink).toBeVisible();
        await weekLink.click();
        await waitForVaadin(page);

        await expect(page.locator('#week-number-count')).toHaveText('1', { timeout: 5000 });
        // Week number date should be a valid date string
        const dateText = await page.locator('#week-number-date').textContent();
        expect(dateText).toMatch(/2025-/);
    });
});

base.describe('Listener Data — BrowserTimezoneObtainedEvent', () => {

    base.beforeEach(async ({ page }) => {
        await gotoListenerDataView(page);
    });

    base('browser timezone is obtained on load', async ({ page }) => {
        // BrowserTimezoneObtainedEvent fires automatically on attach
        await expect(page.locator('#browser-tz-count')).not.toHaveText('0', { timeout: 5000 });
        // Timezone value should be a non-empty string like "Europe/Berlin" or "UTC"
        const tzValue = await page.locator('#browser-tz-value').textContent();
        expect(tzValue.length).toBeGreaterThan(0);
    });
});
