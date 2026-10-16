// @ts-check
const { test, expect } = require('./fixtures');

test.describe('Typed options of calendar.options()', () => {

    test('an initial date set before attach takes effect at creation', async ({ page }) => {
        await page.goto('/test/typed-options');

        await expect(page.locator('#cal-typed-options [data-date="2025-03-15"]').first()).toBeVisible({ timeout: 10000 });
    });

    test('a custom view with a per-view option renders with both', async ({ page }) => {
        await page.goto('/test/typed-options');
        const cells = page.locator('#cal-custom-view .fc-daygrid-day');

        // the definition: three days from the initial date
        await expect(cells).toHaveCount(3, { timeout: 10000 });
        await expect(cells.first()).toHaveAttribute('data-date', '2025-03-15');
        // the per-view option
        await expect(page.locator('#cal-custom-view .fc-daygrid-day.three-day')).toHaveCount(3);
    });

    test('a value-or-callback option applies in both forms', async ({ page }) => {
        await page.goto('/test/typed-options');
        const cell = page.locator('#cal-typed-options .fc-daygrid-day[data-date="2025-03-15"]');
        await expect(cell).toBeVisible({ timeout: 10000 });

        await page.getByRole('button', { name: 'Day cell class as text' }).click();
        await expect(cell).toHaveClass(/\btyped-day\b/);

        await page.getByRole('button', { name: 'Day cell class from callback' }).click();
        await expect(cell).toHaveClass(/\bcallback-day\b/);
        await expect(cell).not.toHaveClass(/\btyped-day\b/);
    });

    test('content and header alignment apply in both forms', async ({ page }) => {
        await page.goto('/test/typed-options');
        const calendar = page.locator('#cal-header-hooks');
        await expect(calendar.locator('.fc-timegrid')).toBeVisible({ timeout: 10000 });
        // the value FullCalendar holds proves our wiring. The alignment classes it renders are internal to the library.
        const dayHeaderAlign = () => calendar.evaluate((el) => {
            // @ts-ignore — custom element exposes FC's internal Calendar instance
            const align = el.calendar.getOption('dayHeaderAlign');
            return typeof align === 'function' ? 'function returning ' + align({}) : align;
        });

        await page.getByRole('button', { name: 'All-day header as text' }).click();
        await expect(calendar.getByText('Text all-day', { exact: true })).toBeVisible();

        await page.getByRole('button', { name: 'All-day header from callback' }).click();
        await expect(calendar.getByText('Callback all-day', { exact: true })).toBeVisible();
        await expect(calendar.getByText('Text all-day', { exact: true })).toHaveCount(0);

        await page.getByRole('button', { name: 'Header align center' }).click();
        await expect.poll(dayHeaderAlign).toBe('center');

        await page.getByRole('button', { name: 'Header align from callback' }).click();
        await expect.poll(dayHeaderAlign).toBe('function returning end');
    });

    test('the initial view and the current date take effect', async ({ page }) => {
        await page.goto('/test/typed-options');
        const calendar = page.locator('#cal-initial-view-now');

        // the initial view: the time grid instead of the default month grid
        await expect(calendar.locator('.fc-timegrid')).toBeVisible({ timeout: 10000 });
        // now: the library marks 2025-03-12 as today, not the real current date
        await expect(calendar.locator('.fc-day-today[data-date="2025-03-12"]').first()).toBeVisible();
    });
});
