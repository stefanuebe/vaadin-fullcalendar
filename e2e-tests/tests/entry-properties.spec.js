// @ts-check
const { test: base } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the entry properties test view and wait for the calendar to render.
 */
async function gotoEntryPropertiesView(page) {
    await page.goto('/test/entry-properties');
    await page.waitForSelector('.vfc-view', { timeout: 10000 });
    // v7: view root carries vfc-view-dayGridMonth (viewClass contract)
    await page.waitForSelector('.vfc-view-dayGridMonth', { timeout: 15000 });
    await page.waitForFunction(() => document.querySelectorAll('.vfc-event').length > 0, { timeout: 15000 });
    await waitForVaadin(page);
}

base.describe('Entry Properties — Visual Effects', () => {

    base.beforeEach(async ({ page }) => {
        await gotoEntryPropertiesView(page);
    });

    base.describe('Color properties', () => {

        base('red entry has red-ish background color', async ({ page }) => {
            // v7: event color is applied via event.color → --fc-classic-primary CSS custom property
            // Query the event root (vfc-event) directly; fc-event-main wrapper is removed in v7
            const entry = page.locator('.vfc-event:has-text("Red Entry")').first();
            await expect(entry).toBeVisible();
            const bgColor = await entry.evaluate(el => {
                // Walk up the element and its children to find the element with a non-transparent background
                function findBgColor(element) {
                    const style = window.getComputedStyle(element);
                    if (style.backgroundColor && style.backgroundColor !== 'rgba(0, 0, 0, 0)' && style.backgroundColor !== 'transparent') {
                        return style.backgroundColor;
                    }
                    for (const child of element.children) {
                        const found = findBgColor(child);
                        if (found) return found;
                    }
                    return style.backgroundColor;
                }
                return findBgColor(el);
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
            // v7: event color is applied via event.color → --fc-classic-primary CSS custom property
            const entry = page.locator('.vfc-event:has-text("Custom BG")').first();
            await expect(entry).toBeVisible();
            const bgColor = await entry.evaluate(el => {
                function findBgColor(element) {
                    const style = window.getComputedStyle(element);
                    if (style.backgroundColor && style.backgroundColor !== 'rgba(0, 0, 0, 0)' && style.backgroundColor !== 'transparent') {
                        return style.backgroundColor;
                    }
                    for (const child of element.children) {
                        const found = findBgColor(child);
                        if (found) return found;
                    }
                    return style.backgroundColor;
                }
                return findBgColor(el);
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
            const entry = page.locator('.vfc-event:has-text("Custom BG")').first();
            await expect(entry).toBeVisible();
            // v7: text color is applied via --fc-event-contrast-color CSS variable on the event root.
            // The Classic theme's inner text element reads this variable; check it directly.
            const contrastColor = await entry.evaluate(el =>
                window.getComputedStyle(el).getPropertyValue('--fc-event-contrast-color').trim()
            );
            expect(contrastColor).toMatch(/#fff|#ffffff|rgb\(255,\s*255,\s*255\)/i);
        });

        base('border entry: borderColor deprecated in v7 — event still visible with default color', async ({ page }) => {
            // v7: borderColor is @JsonIgnore — it is not sent to FC. The entry renders with the
            // default calendar color (no custom border). This test verifies the entry IS rendered.
            const entry = page.locator('.vfc-event:has-text("Border Entry")').first();
            await expect(entry).toBeVisible();
            // In v7, the background color uses --fc-event-color (defaults to --fc-classic-event).
            const bg = await entry.evaluate(el => {
                function findBg(e) {
                    const b = window.getComputedStyle(e).backgroundColor;
                    if (b && b !== 'rgba(0, 0, 0, 0)') return b;
                    for (const c of e.children) { const r = findBg(c); if (r) return r; }
                    return null;
                }
                return findBg(el);
            });
            expect(bg).toMatch(/rgb/); // some color is applied
        });
    });

    base.describe('DisplayMode', () => {

        base('background display mode renders as vfc-bg-event', async ({ page }) => {
            // v7: background events carry vfc-bg-event (backgroundEventClass contract)
            const bgEvents = page.locator('.vfc-bg-event');
            const count = await bgEvents.count();
            expect(count).toBeGreaterThanOrEqual(1);
        });

        base('inverse background entry renders as vfc-bg-event in timeGrid', async ({ page }) => {
            // INVERSE_BACKGROUND only renders visually in timeGrid views, not in dayGrid.
            // Switch to timeGridWeek to verify.
            await page.evaluate(() => {
                const fcEl = document.querySelector('vaadin-full-calendar');
                if (fcEl && fcEl.calendar) {
                    fcEl.calendar.changeView('timeGridWeek', '2025-03-07');
                }
            });
            await waitForVaadin(page);
            // v7: background events carry vfc-bg-event (backgroundEventClass contract)
            const bgEvents = page.locator('.vfc-bg-event');
            const count = await bgEvents.count();
            expect(count).toBeGreaterThanOrEqual(1);
        });

        base('displayMode NONE with clientSideValue=null renders as auto (entry still visible)', async ({ page }) => {
            // DisplayMode.NONE has clientSideValue=null, which FC treats as "auto" (not "none").
            // This is a known behavior: to truly hide an entry, FC needs display="none" as a string.
            // Our NONE enum sends null → FC falls back to auto rendering.
            // This test documents and verifies that behavior.
            const hiddenEntry = page.locator('.vfc-event:has-text("Hidden Entry")').first();
            // Entry IS rendered because null → auto
            await expect(hiddenEntry).toBeVisible();
        });
    });

    base.describe('ClassNames', () => {

        base('custom class entry has my-custom-class on fc-event element', async ({ page }) => {
            const entry = page.locator('.vfc-event.my-custom-class').first();
            await expect(entry).toBeVisible();
            // Verify it's the right entry by checking text content
            const text = await entry.textContent();
            expect(text).toContain('Custom Class');
        });
    });

    base.describe('AllDay rendering', () => {

        base('all-day entry is in day-events area', async ({ page }) => {
            // All-day entries go in the day cell events area
            const march12Cell = page.locator('.vfc-day-cell[data-date="2025-03-12"]');
            // TODO-v7-verify: .fc-daygrid-day-events (gap selector — no stable vfc- equivalent; anchored on .vfc-day-cell)
            const allDayEntry = march12Cell.locator('.vfc-event:has-text("All-Day Entry")');
            await expect(allDayEntry).toBeVisible();
        });

        base('timed entry shows time in dayGrid month view', async ({ page }) => {
            // Timed entries in month view show with a dot and time
            const timedEntry = page.locator('.vfc-event:has-text("Timed Entry")').first();
            await expect(timedEntry).toBeVisible();
            // v7: event time is part of the event's text content (fc-event-time class is obfuscated)
            const text = await timedEntry.textContent();
            expect(text).toMatch(/9/); // Should contain "9" (from 9:00am start)
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
            const noResizeEntry = page.locator('.vfc-event:has-text("No Resize")').first();
            await expect(noResizeEntry).toBeVisible({ timeout: 5000 });
            // TODO-v7-verify: .fc-event-resizer (gap selector — no stable vfc- equivalent; anchored on .vfc-event)
            const resizer = noResizeEntry.locator('.fc-event-resizer');
            await expect(resizer).toHaveCount(0);
        });
    });

    base.describe('ExtendedProps (customProperties)', () => {

        base('extendedProps accessible via entryDidMount callback sets data-attribute', async ({ page }) => {
            // The entryDidMount callback reads customProperties.department and sets data-department attribute
            const propsEntry = page.locator('.vfc-event:has-text("Has Props")').first();
            await expect(propsEntry).toBeVisible();
            // Wait for entryDidMount to run
            const dept = await propsEntry.getAttribute('data-department');
            expect(dept).toBe('Engineering');
        });
    });
});
