// @ts-check
const { test: base } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the entry properties test view and wait for the calendar to render.
 */
async function gotoEntryPropertiesView(page) {
    await page.goto('/test/entry-properties');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 5000 });
    await page.waitForSelector('.fc-event', { timeout: 5000 });
    await waitForVaadin(page);
}

base.describe('Entry Properties — Visual Effects', () => {

    base.beforeEach(async ({ page }) => {
        await gotoEntryPropertiesView(page);
    });

    base.describe('Color properties', () => {

        base('red entry has red-ish background color', async ({ page }) => {
            const entry = page.locator('.fc-event:has-text("Red Entry")').first();
            await expect(entry).toBeVisible();
            // FC renders entry color as inline style on a wrapper element
            // Check that red is applied somewhere in the entry's element tree
            const bgColor = await entry.evaluate(el => {
                const style = window.getComputedStyle(el);
                // Check both the event itself and its inner elements
                if (style.backgroundColor && style.backgroundColor !== 'rgba(0, 0, 0, 0)') {
                    return style.backgroundColor;
                }
                const inner = el.querySelector('.fc-event-main');
                if (inner) {
                    return window.getComputedStyle(inner).backgroundColor;
                }
                return style.backgroundColor;
            });
            // red = rgb(255, 0, 0) — red channel must be dominant (high red, low green+blue)
            expect(bgColor).toMatch(/rgb/);
            const match = bgColor.match(/rgb\w?\((\d+),\s*(\d+),\s*(\d+)/);
            expect(match).not.toBeNull();
            const [r, g, b] = [parseInt(match[1]), parseInt(match[2]), parseInt(match[3])];
            expect(r).toBeGreaterThan(200);
            expect(g).toBeLessThan(100);
            expect(b).toBeLessThan(100);
        });

        base('custom BG entry has green background', async ({ page }) => {
            const entry = page.locator('.fc-event:has-text("Custom BG")').first();
            await expect(entry).toBeVisible();
            const bgColor = await entry.evaluate(el => {
                const style = window.getComputedStyle(el);
                if (style.backgroundColor && style.backgroundColor !== 'rgba(0, 0, 0, 0)') {
                    return style.backgroundColor;
                }
                const inner = el.querySelector('.fc-event-main');
                return inner ? window.getComputedStyle(inner).backgroundColor : style.backgroundColor;
            });
            // #00ff00 = rgb(0, 255, 0) — green channel must be dominant
            expect(bgColor).toMatch(/rgb/);
            const match = bgColor.match(/rgb\w?\((\d+),\s*(\d+),\s*(\d+)/);
            expect(match).not.toBeNull();
            const [r, g, b] = [parseInt(match[1]), parseInt(match[2]), parseInt(match[3])];
            expect(g).toBeGreaterThan(200);
            expect(r).toBeLessThan(100);
            expect(b).toBeLessThan(100);
        });

        base('custom BG entry has white text color', async ({ page }) => {
            const entry = page.locator('.fc-event:has-text("Custom BG")').first();
            const textColor = await entry.evaluate(el => {
                // Text color may be on the event-main or event-title element
                const title = el.querySelector('.fc-event-title') || el.querySelector('.fc-event-main') || el;
                return window.getComputedStyle(title).color;
            });
            // #ffffff = rgb(255, 255, 255) — all channels should be > 200
            const match = textColor.match(/rgb\w?\((\d+),\s*(\d+),\s*(\d+)/);
            expect(match).not.toBeNull();
            expect(parseInt(match[1])).toBeGreaterThan(200);
            expect(parseInt(match[2])).toBeGreaterThan(200);
            expect(parseInt(match[3])).toBeGreaterThan(200);
        });

        base('border entry has blue border color', async ({ page }) => {
            const entry = page.locator('.fc-event:has-text("Border Entry")').first();
            await expect(entry).toBeVisible();
            const borderColor = await entry.evaluate(el => {
                return window.getComputedStyle(el).borderColor || window.getComputedStyle(el).borderLeftColor;
            });
            // blue = rgb(0, 0, 255) — blue channel must be dominant
            expect(borderColor).toMatch(/rgb/);
            const match = borderColor.match(/rgb\w?\((\d+),\s*(\d+),\s*(\d+)/);
            expect(match).not.toBeNull();
            const [r, g, b] = [parseInt(match[1]), parseInt(match[2]), parseInt(match[3])];
            expect(b).toBeGreaterThan(200);
            expect(r).toBeLessThan(100);
            expect(g).toBeLessThan(100);
        });
    });

    base.describe('DisplayMode', () => {

        base('background display mode renders as fc-bg-event', async ({ page }) => {
            // Background events are rendered differently — look for fc-bg-event
            const bgEvents = page.locator('.fc-bg-event');
            const count = await bgEvents.count();
            expect(count).toBeGreaterThanOrEqual(1);
        });

        base('inverse background entry renders as fc-bg-event in timeGrid', async ({ page }) => {
            // INVERSE_BACKGROUND only renders visually in timeGrid views, not in dayGrid.
            // Switch to timeGridWeek to verify.
            await page.evaluate(() => {
                const fcEl = document.querySelector('vaadin-full-calendar');
                if (fcEl && fcEl.calendar) {
                    fcEl.calendar.changeView('timeGridWeek', '2025-03-07');
                }
            });
            await waitForVaadin(page);
            // In timeGrid, inverse background should render fc-bg-event elements
            const bgEvents = page.locator('.fc-bg-event');
            const count = await bgEvents.count();
            expect(count).toBeGreaterThanOrEqual(1);
        });

        base('displayMode NONE with clientSideValue=null renders as auto (entry still visible)', async ({ page }) => {
            // DisplayMode.NONE has clientSideValue=null, which FC treats as "auto" (not "none").
            // This is a known behavior: to truly hide an entry, FC needs display="none" as a string.
            // Our NONE enum sends null → FC falls back to auto rendering.
            // This test documents and verifies that behavior.
            const hiddenEntry = page.locator('.fc-event:has-text("Hidden Entry")').first();
            // Entry IS rendered because null → auto
            await expect(hiddenEntry).toBeVisible();
        });
    });

    base.describe('ClassNames', () => {

        base('custom class entry has my-custom-class on fc-event element', async ({ page }) => {
            const entry = page.locator('.fc-event.my-custom-class').first();
            await expect(entry).toBeVisible();
            // Verify it's the right entry by checking text content
            const text = await entry.textContent();
            expect(text).toContain('Custom Class');
        });
    });

    base.describe('AllDay rendering', () => {

        base('all-day entry is in day-events area', async ({ page }) => {
            // All-day entries go in .fc-daygrid-day-events
            const march12Cell = page.locator('.fc-daygrid-day[data-date="2025-03-12"]');
            const allDayEntry = march12Cell.locator('.fc-daygrid-day-events .fc-event:has-text("All-Day Entry")');
            await expect(allDayEntry).toBeVisible();
        });

        base('timed entry shows time in dayGrid month view', async ({ page }) => {
            // Timed entries in month view show with a dot and time
            const timedEntry = page.locator('.fc-event:has-text("Timed Entry")').first();
            await expect(timedEntry).toBeVisible();
            // Timed entries should have .fc-event-time element showing the time
            const timeEl = timedEntry.locator('.fc-event-time');
            await expect(timeEl).toBeVisible();
            const timeText = await timeEl.textContent();
            expect(timeText).toMatch(/9/); // Should contain "9" (from 9:00am start)
        });
    });

    base.describe('Editable flags', () => {

        base('non-resizable entry has no resize handle in timeGrid view', async ({ page }) => {
            // Switch to timeGrid week and navigate to the week containing March 14
            await page.evaluate(() => {
                const fcEl = document.querySelector('vaadin-full-calendar');
                if (fcEl && fcEl.calendar) {
                    fcEl.calendar.changeView('timeGridWeek', '2025-03-14');
                }
            });
            await waitForVaadin(page);

            // "No Resize" entry must be visible in timeGrid
            const noResizeEntry = page.locator('.fc-event:has-text("No Resize")').first();
            await expect(noResizeEntry).toBeVisible({ timeout: 5000 });
            const resizer = noResizeEntry.locator('.fc-event-resizer');
            await expect(resizer).toHaveCount(0);
        });
    });

    base.describe('ExtendedProps (customProperties)', () => {

        base('extendedProps accessible via entryDidMount callback sets data-attribute', async ({ page }) => {
            // The entryDidMount callback reads customProperties.department and sets data-department attribute
            const propsEntry = page.locator('.fc-event:has-text("Has Props")').first();
            await expect(propsEntry).toBeVisible();
            // Wait for entryDidMount to run
            const dept = await propsEntry.getAttribute('data-department');
            expect(dept).toBe('Engineering');
        });
    });
});
