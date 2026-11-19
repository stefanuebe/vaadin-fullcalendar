// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * FullCalendar theme selection (UC-027): stock themes loaded on demand, two themes on one page, a custom theme
 * registered in the browser, and the console error for a name nobody registered.
 *
 * A stock theme's plugin puts classes named fc-<theme>-* on the elements, and its stylesheet lands in
 * style[data-fc-theme="<theme>"]. Both are what the tests look for.
 */

const STOCK_THEMES = ['classic', 'monarch', 'breezy', 'forma', 'pulse'];

const firstEntry = (page) => page.locator('#cal-first .fc-event').first();
const secondEntry = (page) => page.locator('#cal-second .fc-event').first();
const themeStyle = (page, theme) => page.locator(`head style[data-fc-theme="${theme}"]`);

/**
 * The color a palette variable resolves to on the page. Resolved through an element, because the build may minify
 * the declared value (e.g. to #010203).
 */
const resolvedColor = (page, variable) => page.evaluate((name) => {
    const probe = document.createElement('div');
    probe.style.color = `var(${name})`;
    document.body.appendChild(probe);
    const color = getComputedStyle(probe).color;
    probe.remove();
    return color;
}, variable);

test.describe('FullCalendar themes', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/theme');
        await page.waitForSelector('#cal-first .fc-event', { timeout: 10000 });
        await waitForVaadin(page);
    });

    test('two calendars on one page keep their own theme', async ({ page }) => {
        await expect(firstEntry(page)).toHaveClass(/fc-monarch-/);
        await expect(firstEntry(page)).not.toHaveClass(/fc-pulse-/);

        await expect(secondEntry(page)).toHaveClass(/fc-pulse-/);
        await expect(secondEntry(page)).not.toHaveClass(/fc-monarch-/);
    });

    test('a stock theme is loaded only when a calendar selects it', async ({ page }) => {
        await expect(themeStyle(page, 'monarch')).toHaveCount(1);
        await expect(themeStyle(page, 'breezy')).toHaveCount(0);

        await page.click('#theme-breezy');

        await expect(firstEntry(page)).toHaveClass(/fc-breezy-/);
        await expect(themeStyle(page, 'breezy')).toHaveCount(1);
    });

    for (const theme of STOCK_THEMES) {
        test(`stock theme ${theme} renders with its default palette`, async ({ page }) => {
            if (theme === 'monarch') {
                // monarch is the initial theme, switch away first so the click below changes something
                await page.click('#theme-classic');
                await expect(firstEntry(page)).toHaveClass(/fc-classic-/);
            }
            await page.click(`#theme-${theme}`);

            await expect(firstEntry(page)).toHaveClass(new RegExp(`fc-${theme}-`));
            // the other calendar is not affected
            await expect(secondEntry(page)).toHaveClass(/fc-pulse-/);

            // the default palette defines the theme's colors. Classic has no background variable, so check its primary.
            const paletteVariable = theme === 'classic' ? '--fc-classic-primary' : `--fc-${theme}-background`;
            const value = await page.evaluate((name) => getComputedStyle(document.documentElement)
                .getPropertyValue(name).trim(), paletteVariable);
            expect(value).not.toBe('');
        });
    }

    test('a stock theme switched to twice is loaded once', async ({ page }) => {
        await page.click('#theme-forma');
        await expect(firstEntry(page)).toHaveClass(/fc-forma-/);
        await page.click('#theme-classic');
        await expect(firstEntry(page)).toHaveClass(/fc-classic-/);
        await page.click('#theme-forma');
        await expect(firstEntry(page)).toHaveClass(/fc-forma-/);

        await expect(themeStyle(page, 'forma')).toHaveCount(1);
    });

    test('vaadin builds on classic, classic still loads its own stylesheet and palette', async ({ page }) => {
        await page.click('#theme-vaadin');
        await expect(firstEntry(page)).toHaveClass(/fc-vaadin/);
        await expect(firstEntry(page)).toHaveClass(/fc-classic-/);
        await expect(themeStyle(page, 'classic')).toHaveCount(0);

        await page.click('#theme-classic');
        await expect(firstEntry(page)).not.toHaveClass(/fc-vaadin/);
        await expect(themeStyle(page, 'classic')).toHaveCount(1);
    });

    test('the application palette override wins over the lazily loaded default palette', async ({ page }) => {
        await page.click('#theme-forma');
        await expect(themeStyle(page, 'forma')).toHaveCount(1);

        expect(await resolvedColor(page, '--fc-forma-background')).toBe('rgb(1, 2, 3)');
    });

    test('a stock palette imported by the application wins over the lazily loaded default palette', async ({ page }) => {
        await page.click('#theme-breezy');
        // the default palette has loaded, inside its layer
        await expect(themeStyle(page, 'breezy')).toHaveCount(1);
        expect(await themeStyle(page, 'breezy').textContent()).toMatch(/@layer fc-palette\s*\{[^}]*--fc-breezy-primary/);

        // emerald's primary. The default palette indigo would give rgb(79, 70, 229).
        expect(await resolvedColor(page, '--fc-breezy-primary')).toBe('rgb(5, 150, 105)');
    });

    test('a custom theme registered in the browser can be selected from Java', async ({ page }) => {
        await page.click('#theme-test-custom');

        await expect(firstEntry(page)).toHaveClass(/test-custom-theme-entry/);
        await expect(firstEntry(page)).not.toHaveClass(/fc-monarch-/);
        // stable classes stay
        await expect(firstEntry(page)).toHaveClass(/fc-event/);
    });

    test('a theme change keeps an option set after attach', async ({ page }) => {
        await page.click('#first-week-numbers-off');
        await waitForVaadin(page);
        await page.click('#theme-forma');
        await expect(firstEntry(page)).toHaveClass(/fc-forma-/);

        const weekNumbers = await page.locator('#cal-first')
            .evaluate((el) => el.calendar.getOption('weekNumbers'));
        expect(weekNumbers).toBe(false);
    });

    test('a calendar keeps its theme after detach and reattach', async ({ page }) => {
        await page.click('#theme-forma');
        await expect(firstEntry(page)).toHaveClass(/fc-forma-/);

        await page.click('#first-detach');
        await expect(page.locator('#cal-first')).toHaveCount(0);
        await page.click('#first-attach');

        await expect(firstEntry(page)).toHaveClass(/fc-forma-/);
        await expect(page.locator('#cal-first')).toHaveCSS('visibility', 'visible');
    });

    test('a theme change on a scheduler keeps its resource view', async ({ page }) => {
        const scheduler = page.locator('#cal-scheduler');
        await expect(scheduler.getByText('Room A')).toBeVisible();

        await page.click('#scheduler-theme-forma');

        await expect(scheduler.locator('[class*="fc-forma-"]').first()).toBeAttached();
        await expect(scheduler.getByText('Room A')).toBeVisible();
    });

    test('a calendar stays invisible until its initial theme has loaded', async ({ page }) => {
        const gated = page.locator('#cal-gated');
        // rendered, but not shown without its theme
        await expect(gated.locator('.fc-event')).toHaveCount(1);
        await expect(gated).toHaveCSS('visibility', 'hidden');

        await page.evaluate(() => window.releaseGatedTheme());

        await expect(gated).toHaveCSS('visibility', 'visible');
        await expect(gated.locator('.fc-event')).toHaveClass(/test-custom-theme-entry/);
    });

    test('a calendar waiting for its initial theme becomes visible when switched to an unknown theme', async ({ page }) => {
        const gated = page.locator('#cal-gated');
        await expect(gated).toHaveCSS('visibility', 'hidden');

        await page.click('#gated-theme-unknown');

        await expect(gated).toHaveCSS('visibility', 'visible');
    });

    test('a theme that finishes loading after the calendar switched away changes nothing', async ({ page }) => {
        const gated = page.locator('#cal-gated');
        await page.click('#gated-theme-monarch');
        await expect(gated.locator('.fc-event')).toHaveClass(/fc-monarch-/);

        await page.evaluate(() => window.releaseGatedTheme());
        // The released load has no visible effect on the gated calendar. The first calendar loads the same plugin
        // module, and once it shows the plugin the released load has finished too.
        await page.click('#theme-test-custom');
        await expect(firstEntry(page)).toHaveClass(/test-custom-theme-entry/);

        await expect(gated.locator('.fc-event')).toHaveClass(/fc-monarch-/);
        await expect(gated.locator('.fc-event')).not.toHaveClass(/test-custom-theme-entry/);
    });

    test('calendars whose initial theme fails to load become visible and log the failure', async ({ page }) => {
        const errors = [];
        page.on('console', (message) => {
            if (message.type() === 'error') {
                errors.push(message.text());
            }
        });
        // two calendars wait on the same load
        const failing = page.locator('#cal-failing');
        const failingToo = page.locator('#cal-failing-too');
        await expect(failing).toHaveCSS('visibility', 'hidden');
        await expect(failingToo).toHaveCSS('visibility', 'hidden');

        await page.evaluate(() => window.failFailingTheme());

        await expect(failing).toHaveCSS('visibility', 'visible');
        await expect(failingToo).toHaveCSS('visibility', 'visible');
        await expect.poll(() => errors.join('\n')).toContain('could not load theme "test-failing"');
        // a failure is not mistaken for a replaced loader, so nobody loads again
        expect(await page.evaluate(() => window.failingThemeLoads)).toBe(1);

        // selecting the theme again after the failure loads it again
        await page.click('#failing-monarch');
        await expect(failing.locator('.fc-event')).toHaveClass(/fc-monarch-/);
        await page.click('#failing-again');
        await expect.poll(() => page.evaluate(() => window.failingThemeLoads)).toBe(2);
    });

    test('a theme whose loader is replaced while loading ends up with the new loader', async ({ page }) => {
        const swapped = page.locator('#cal-swapped');
        await expect(swapped).toHaveCSS('visibility', 'hidden');

        await page.evaluate(() => window.replaceSwappedTheme());

        await expect(swapped.locator('.fc-event')).toHaveClass(/test-custom-theme-entry/);
        await expect(swapped.locator('.fc-event')).not.toHaveClass(/test-old-theme-entry/);
        await expect(swapped).toHaveCSS('visibility', 'visible');
    });

    test('a replaced loader that fails afterwards does not count, the new loader applies', async ({ page }) => {
        const errors = [];
        page.on('console', (message) => {
            if (message.type() === 'error') {
                errors.push(message.text());
            }
        });
        const swapped = page.locator('#cal-swapped-failing');
        await expect(swapped).toHaveCSS('visibility', 'hidden');

        await page.evaluate(() => window.replaceSwappedFailingTheme());

        await expect(swapped.locator('.fc-event')).toHaveClass(/test-custom-theme-entry/);
        await expect(swapped).toHaveCSS('visibility', 'visible');
        // the new loader only runs after the failure handler took the replaced-loader branch, so a wrong error
        // would already be logged by now
        expect(errors.join('\n')).not.toContain('could not load theme "test-swapped-failing"');
    });

    test('an unknown theme logs a console error and keeps the previous theme', async ({ page }) => {
        const errors = [];
        page.on('console', (message) => {
            if (message.type() === 'error') {
                errors.push(message.text());
            }
        });

        await page.click('#theme-unknown-theme');

        await expect.poll(() => errors.join('\n')).toContain('unknown theme "unknown-theme"');
        await waitForVaadin(page);
        await expect(firstEntry(page)).toHaveClass(/fc-monarch-/);
    });
});
