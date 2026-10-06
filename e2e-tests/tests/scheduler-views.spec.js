// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Scheduler options under their FullCalendar 7 names reach the three resource views (UC-015),
 * and the scheduler views carry the stable classes (UC-025).
 *
 * The test view starts in resourceTimelineDay. Vertical resource views are reached with
 * calendar.changeView on the client; the options were all set from the server before.
 */

async function gotoSchedulerViews(page) {
    await page.goto('/test/scheduler-views');
    await page.waitForSelector('.fc-event', { timeout: 10000 });
    await waitForVaadin(page);
}

async function changeView(page, viewName) {
    await page.evaluate(view =>
        document.querySelector('vaadin-full-calendar-scheduler').calendar.changeView(view), viewName);
}

/** Resource "Room A" sets eventColor #00ff00, eventContrastColor #ff0000 and eventClass room-a-entry. */
async function expectRoomAColors(entry) {
    const colors = await entry.evaluate(el => ({
        background: getComputedStyle(el).backgroundColor,
        text: getComputedStyle(el.querySelector('.fc-event-title') || el).color,
    }));
    expect(colors.background).toBe('rgb(0, 255, 0)');
    expect(colors.text).toBe('rgb(255, 0, 0)');
}

test.describe('Resource timeline view', () => {

    test.beforeEach(async ({ page }) => {
        await gotoSchedulerViews(page);
    });

    test('carries the scheduler stable classes', async ({ page }) => {
        await expect(page.locator('.fc-view.fc-timeline.fc-resource-timeline')).toBeVisible();
        await expect(page.locator('.fc-datagrid-cell.fc-resource').first()).toBeVisible();
        await expect(page.locator('.fc-timeline-lane.fc-resource')).toHaveCount(2);
        await expect(page.locator('.fc-timeline-slot.fc-timeline-slot-lane').first()).toBeAttached();
        await expect(page.locator('.fc-event.fc-timeline-event')).toHaveCount(2);
    });

    test('resourceColumnHeaderContent fills the column header', async ({ page }) => {
        await expect(page.locator('.fc-datagrid-cell[role="columnheader"]')).toHaveText('Rooms');
    });

    test('resourceColumnsWidth sets the width of the resource area', async ({ page }) => {
        const box = await page.locator('.fc-datagrid-cell[role="columnheader"]').boundingBox();
        expect(Math.round(box.width)).toBe(200);
    });

    test('resourceCellClass reaches the title cells', async ({ page }) => {
        await expect(page.locator('.fc-datagrid-cell.hook-resource-cell')).toHaveText(['Room A', 'Room B']);
    });

    test('resourceLaneClass and resourceLaneTopContent reach the lanes', async ({ page }) => {
        const lanes = page.locator('.fc-timeline-lane.fc-resource.hook-lane');
        await expect(lanes).toHaveCount(2);
        await expect(lanes.first()).toContainText('Top Room A');
        await expect(lanes.nth(1)).toContainText('Top Room B');
    });

    test('resource eventColor, eventContrastColor and eventClass reach its entries', async ({ page }) => {
        const entry = page.locator('.fc-timeline-event.room-a-entry');
        await expect(entry).toHaveText(/Room A Meeting/);
        await expectRoomAColors(entry);
    });

    test('updateResource sends eventClass and eventContrastColor under their FullCalendar 7 keys', async ({ page }) => {
        // Checks the payload only: FullCalendar's Resource.setProp stores event style props without
        // re-deriving the styles of the resource's entries, so they do not repaint.
        await page.evaluate(() => {
            const el = document.querySelector('vaadin-full-calendar-scheduler');
            const original = el.updateResource.bind(el);
            window.__updateResourceCalls = [];
            el.updateResource = json => {
                window.__updateResourceCalls.push(JSON.parse(json));
                return original(json);
            };
        });

        await page.click('#restyle-room-b');
        // wait for the first call, let the round trip finish, then count: batching sends exactly one
        await expect.poll(() => page.evaluate(() => window.__updateResourceCalls.length)).toBeGreaterThan(0);
        await waitForVaadin(page);
        expect(await page.evaluate(() => window.__updateResourceCalls.length)).toBe(1);

        const calls = await page.evaluate(() => window.__updateResourceCalls);
        expect(calls[0]).toMatchObject({
            id: 'b',
            eventColor: '#0000ff',
            eventContrastColor: '#ff0000',
            eventClass: 'room-b-restyled',
        });
        expect(calls[0]).not.toHaveProperty('eventClassNames');
        expect(calls[0]).not.toHaveProperty('eventTextColor');

        // the client does not leak them into the extended props
        const extendedProps = await page.evaluate(() =>
            document.querySelector('vaadin-full-calendar-scheduler').calendar.getResourceById('b').extendedProps);
        expect(extendedProps).not.toHaveProperty('eventClass');
        expect(extendedProps).not.toHaveProperty('eventContrastColor');
    });
});

test.describe('Resource time grid view', () => {

    test.beforeEach(async ({ page }) => {
        await gotoSchedulerViews(page);
        await changeView(page, 'resourceTimeGridDay');
        await page.waitForSelector('.fc-timegrid-event');
    });

    test('resourceDayHeaderClass and resourceDayHeaderContent reach the resource headers', async ({ page }) => {
        await expect(page.locator('.fc-resource.hook-day-header')).toHaveText(['Res Room A', 'Res Room B']);
    });

    test('resource entry styles reach the time grid entry', async ({ page }) => {
        const entry = page.locator('.fc-timegrid-event.room-a-entry');
        await expect(entry).toHaveText(/Room A Meeting/);
        await expectRoomAColors(entry);
    });
});

test.describe('Resource day grid view', () => {

    test.beforeEach(async ({ page }) => {
        await gotoSchedulerViews(page);
        await changeView(page, 'resourceDayGridDay');
        await page.waitForSelector('.fc-daygrid-event');
    });

    test('resourceDayHeaderClass and resourceDayHeaderContent reach the resource headers', async ({ page }) => {
        await expect(page.locator('.fc-resource.hook-day-header')).toHaveText(['Res Room A', 'Res Room B']);
    });

    test('resource eventClass reaches the day grid entry', async ({ page }) => {
        // A timed entry renders as a dot in the day grid, without a background, so only the class is checked.
        await expect(page.locator('.fc-daygrid-event.room-a-entry')).toHaveText(/Room A Meeting/);
    });
});
