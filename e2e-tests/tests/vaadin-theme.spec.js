// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * The FullCalendar Vaadin theme (UC-023) against VaadinThemeTestView: the default theme takes its colors from Lumo or
 * Aura, follows the application's color scheme, and the option colorScheme overrides it per calendar.
 *
 * Expected colors are resolved in the page, inside the element under test, from the application theme's own
 * variables. So a test checks that the calendar uses the theme's color, whatever value Lumo or Aura give it in a later
 * version. The classic calendar on the page loads classic's default palette, whose dark colors must not reach a dark
 * Vaadin calendar.
 */

const APP_THEMES = [
    { name: 'Lumo', query: '', accent: '--lumo-primary-color', background: '--lumo-base-color' },
    { name: 'Aura', query: 'theme=aura', accent: '--aura-accent-color', background: '--aura-surface-color-solid' },
];

const query = (...parts) => parts.filter(Boolean).join('&');

async function gotoView(page, parameters) {
    await page.goto(`/test/vaadin-theme${parameters ? '?' + parameters : ''}`);
    await page.waitForSelector('#cal-vaadin .fc-event', { timeout: 10000 });
    await waitForVaadin(page);
}

/** The color a CSS value resolves to inside the given element, so in that element's color scheme. */
const colorIn = (page, selector, value) => page.locator(selector).first().evaluate((el, v) => {
    const probe = document.createElement('div');
    probe.style.color = v;
    el.appendChild(probe);
    const color = getComputedStyle(probe).color;
    probe.remove();
    return color;
}, value);

/**
 * The calendar's background color, from the variable the classic theme paints it with. The value comes from the
 * variable rather than from one painted element, so the check does not depend on classic's markup. That the theme's
 * rule applies at all is also covered by the entry color checks.
 */
const calendarBackground = (page, selector) => colorIn(page, selector, 'var(--fc-classic-background)');

const backgroundOf = (locator) => locator.evaluate((el) => getComputedStyle(el).backgroundColor);

/**
 * Whether a color is dark. The browser converts it to sRGB on a canvas, because Aura gives its colors in oklch. A
 * transparent color is neither light nor dark, so it fails the test.
 */
const isDark = (page, color) => page.evaluate((c) => {
    const context = document.createElement('canvas').getContext('2d');
    context.fillStyle = c;
    context.fillRect(0, 0, 1, 1);
    const [r, g, b, alpha] = context.getImageData(0, 0, 1, 1).data;
    if (alpha === 0) {
        throw new Error(`transparent color ${c}`);
    }
    return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255 < 0.5;
}, color);

test.describe('FullCalendar Vaadin theme', () => {

    for (const appTheme of APP_THEMES) {
        for (const scheme of ['light', 'dark']) {
            test(`${appTheme.name} ${scheme}: entries take the accent color, the calendar follows the color scheme`, async ({ page }) => {
                await gotoView(page, query(appTheme.query, `scheme=${scheme}`));

                expect(await backgroundOf(page.locator('#cal-vaadin .fc-event').first()))
                    .toBe(await colorIn(page, '#cal-vaadin', `var(${appTheme.accent})`));
                expect(await calendarBackground(page, '#cal-vaadin'))
                    .toBe(await colorIn(page, '#cal-vaadin', `var(${appTheme.background})`));
                expect(await isDark(page, await calendarBackground(page, '#cal-vaadin'))).toBe(scheme === 'dark');
            });
        }

        test(`${appTheme.name}: the option colorScheme makes one calendar and its popover dark in a light app`, async ({ page }) => {
            await gotoView(page, query(appTheme.query, 'scheme=light'));

            expect(await isDark(page, await calendarBackground(page, '#cal-vaadin'))).toBe(false);
            expect(await isDark(page, await calendarBackground(page, '#cal-vaadin-dark'))).toBe(true);
            expect(await calendarBackground(page, '#cal-vaadin-dark'))
                .toBe(await colorIn(page, '#cal-vaadin-dark', `var(${appTheme.background})`));

            // FullCalendar renders the popover into the body, the theme's class and the color scheme go with it
            await page.locator('#cal-vaadin-dark').getByText('+1 more').click();
            const popover = page.locator('.fc-popover');
            await expect(popover).toHaveClass(/fc-vaadin/);
            const popoverBackground = await backgroundOf(popover);
            expect(await isDark(page, popoverBackground)).toBe(true);
            expect(popoverBackground).toBe(await colorIn(page, '.fc-popover', `var(${appTheme.background})`));
        });

        test(`${appTheme.name}: an entry dragged out of a dark calendar keeps the theme's colors`, async ({ page }) => {
            await gotoView(page, query(appTheme.query, 'scheme=light'));

            const entry = page.locator('#cal-vaadin-dark .fc-event').first();
            const box = await entry.boundingBox();
            await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
            await page.mouse.down();
            try {
                // out of the calendar, where FullCalendar shows a copy of the entry in the body
                await page.mouse.move(box.x + box.width / 2, 5, { steps: 10 });

                const mirror = page.locator('body > .fc-event');
                await expect(mirror).toHaveClass(/fc-vaadin/);
                await expect(mirror).toHaveAttribute('data-color-scheme', 'dark');
                expect(await backgroundOf(mirror))
                    .toBe(await colorIn(page, 'body > .fc-event', `var(${appTheme.accent})`));
            } finally {
                await page.mouse.up();
            }
        });
    }

    test('Aura: the option colorScheme makes one calendar light in a dark app', async ({ page }) => {
        await gotoView(page, 'theme=aura&scheme=dark');

        expect(await isDark(page, await calendarBackground(page, '#cal-vaadin'))).toBe(true);
        expect(await isDark(page, await calendarBackground(page, '#cal-vaadin-light'))).toBe(false);
    });

    test('the default theme shows at once, the calendar is never hidden for loading it', async ({ page }) => {
        // records whether a calendar of the default theme gets visibility: hidden at any time while the page loads
        await page.addInitScript(() => {
            window.hiddenCalendars = [];
            new MutationObserver((mutations) => mutations.forEach((mutation) => {
                const element = /** @type {HTMLElement} */ (mutation.target);
                // the old value as well, the style may have changed again before this callback runs
                const hidden = /visibility:\s*hidden/;
                const wasHidden = hidden.test(mutation.oldValue ?? '') || element.style.visibility === 'hidden';
                if (element.id === 'cal-vaadin' && wasHidden) {
                    window.hiddenCalendars.push(element.id);
                }
            })).observe(document, {
                attributes: true, attributeFilter: ['style'], attributeOldValue: true, subtree: true,
            });
        });
        await gotoView(page, '');

        await expect(page.locator('#cal-vaadin')).toHaveClass(/fc-vaadin/);
        expect(await page.evaluate(() => window.hiddenCalendars)).toEqual([]);
    });

    test('a classic calendar on the same page keeps classic colors', async ({ page }) => {
        await gotoView(page, '');
        await expect(page.locator('#cal-classic')).not.toHaveClass(/fc-vaadin/);
        await expect(page.locator('#cal-classic .fc-event').first()).toBeVisible();

        expect(await backgroundOf(page.locator('#cal-classic .fc-event').first()))
            .toBe(await colorIn(page, '#cal-classic', 'var(--fc-classic-primary)'));
        expect(await backgroundOf(page.locator('#cal-classic .fc-event').first()))
            .not.toBe(await backgroundOf(page.locator('#cal-vaadin .fc-event').first()));
    });

    test('a class of the application on one calendar sets the colors of its entries, over a rule for all calendars', async ({ page }) => {
        await gotoView(page, '');

        // vaadin-theme-override.css sets the entry contrast color for every calendar and both entry colors for the
        // class custom-entry-color
        const customEntry = page.locator('#cal-vaadin-custom .fc-event').first();
        expect(await backgroundOf(customEntry)).toBe('rgb(4, 5, 6)');
        expect(await colorIn(page, '#cal-vaadin-custom .fc-event', 'var(--fc-event-contrast-color)'))
            .toBe('rgb(250, 251, 252)');

        expect(await backgroundOf(page.locator('#cal-vaadin .fc-event').first())).not.toBe('rgb(4, 5, 6)');
        expect(await colorIn(page, '#cal-vaadin .fc-event', 'var(--fc-event-contrast-color)')).toBe('rgb(7, 8, 9)');
    });

    test('an entry of an entry source takes the theme like the calendar\'s own entries', async ({ page }) => {
        await gotoView(page, '');

        const remoteEntry = page.locator('#cal-vaadin-source .fc-event').filter({ hasText: 'Remote entry' });
        await expect(remoteEntry).toHaveClass(/fc-vaadin/);
        expect(await backgroundOf(remoteEntry))
            .toBe(await colorIn(page, '#cal-vaadin-source', 'var(--lumo-primary-color)'));
    });

    test("today's day number is marked as a badge", async ({ page }) => {
        await gotoView(page, '');

        const today = page.locator('#cal-vaadin .fc-vaadin-today');
        await expect(today).toHaveCount(1);
        expect(await today.evaluate((el) => getComputedStyle(el).color))
            .toBe(await colorIn(page, '#cal-vaadin', 'var(--lumo-primary-text-color)'));
    });
});
