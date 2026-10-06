// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Stable v6 class names (UC-025, ADR 0001).
 *
 * FullCalendar 7 renders no semantic fc-* classes. The addon adds a documented subset through
 * FullCalendar's class name options. These tests check a representative class per element family
 * and that the classes are joined with the theme's own classes instead of replacing them.
 */

test.describe('Stable class names in the day grid', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/listener-data');
        await page.waitForSelector('.fc-view', { timeout: 10000 });
        await waitForVaadin(page);
    });

    test('root, view and day cells carry the v6 classes', async ({ page }) => {
        await expect(page.locator('vaadin-full-calendar.fc')).toBeVisible();
        await expect(page.locator('.fc-view.fc-dayGridMonth-view')).toBeVisible();
        await expect(page.locator('.fc-col-header-cell.fc-day-mon').first()).toBeVisible();
        await expect(page.locator('.fc-daygrid-day.fc-day[data-date="2025-03-10"]')).toBeVisible();
        await expect(page.locator('.fc-daygrid-day[data-date="2025-03-10"] .fc-daygrid-day-number')).toHaveText('10');
    });

    test('day number class also when the text is more than the bare number', async ({ page }) => {
        // Japanese renders "10日", not "10". v6 marked that text as day number, too.
        await page.evaluate(() => document.querySelector('vaadin-full-calendar').calendar.setOption('locale', 'ja'));
        await expect(page.locator('.fc-daygrid-day[data-date="2025-03-10"] .fc-daygrid-day-number')).toHaveText('10日');
    });

    test('entries carry the v6 classes next to the theme classes', async ({ page }) => {
        const entry = page.locator('.fc-event.fc-daygrid-event').first();
        await expect(entry).toBeVisible();
        // the classic theme's own classes are still there, so the classes were joined, not replaced
        await expect(entry).toHaveClass(/fc-classic-/);
    });

    test('more-link popover carries the v6 classes', async ({ page }) => {
        const moreLink = page.locator('.fc-daygrid-more-link.fc-more-link').first();
        await expect(moreLink).toBeVisible();
        await moreLink.click();

        const popover = page.locator('.fc-popover.fc-more-popover');
        await expect(popover).toBeVisible();
        await expect(popover.locator('.fc-popover-header')).toBeVisible();
        await expect(popover.locator('.fc-popover-body .fc-event').first()).toBeVisible();
    });
});

test.describe('Stable class names in the time grid', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/interaction-callbacks');
        await page.waitForSelector('.fc-view', { timeout: 10000 });
        await waitForVaadin(page);
    });

    test('slots, lanes and timed entries carry the v6 classes', async ({ page }) => {
        await expect(page.locator('.fc-view.fc-timeGridWeek-view')).toBeVisible();
        await expect(page.locator('.fc-timegrid-slot-label').first()).toBeVisible();
        await expect(page.locator('.fc-timegrid-slot-lane[data-time="09:00:00"]')).toBeVisible();
        await expect(page.locator('.fc-timegrid-col.fc-day-mon')).toBeVisible();
        await expect(page.locator('.fc-event.fc-timegrid-event.fc-v-event:has-text("Drag Me")')).toBeVisible();
    });

    test('dragged entry outside the calendar carries the v6 classes', async ({ page }) => {
        const entry = page.locator('.fc-event:has-text("Drag Me")').first();
        const box = await entry.boundingBox();
        if (!box) throw new Error('no bounding box for "Drag Me"');

        await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
        await page.mouse.down();
        await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2 + 60, { steps: 5 });

        try {
            // FullCalendar attaches the dragged element to the body, outside the calendar element
            await expect(page.locator('body > .fc-event.fc-event-dragging')).toBeAttached();
        } finally {
            await page.mouse.up();
        }
    });

    test('resize mirror carries fc-event-mirror', async ({ page }) => {
        const entry = page.locator('.fc-event:has-text("Resize Me")').first();
        await entry.hover();
        const box = await entry.locator('.fc-event-resizer-end').boundingBox();
        if (!box) throw new Error('no resizer for "Resize Me"');

        await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
        await page.mouse.down();
        await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2 + 60, { steps: 5 });

        try {
            await expect(page.locator('.fc-event.fc-event-mirror.fc-event-resizing')).toBeAttached();
        } finally {
            await page.mouse.up();
        }
    });
});
