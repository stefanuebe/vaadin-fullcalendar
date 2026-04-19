// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

/**
 * Regression test for issue #230: Scheduler.updateResource must propagate extended
 * property changes to the client.
 *
 * Before the fix, updateResource only applied known built-in props (title, color, …)
 * via FC's setProp. Extended props were silently dropped, so the client state diverged
 * from the server after any update.
 */
test.describe('Scheduler updateResource — extended props', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/test/update-resource-extended-props');
        await page.waitForSelector('.fc', { timeout: 15000 });
        await page.waitForSelector('.fc-timeline', { timeout: 15000 });
        await waitForVaadin(page);
    });

    test('updateResource syncs extended prop to client', async ({ page }) => {
        // Helper: read the department extended prop off the FC client resource
        const readDepartment = async () =>
            page.evaluate(() => {
                const calendarEl = document.querySelector('[data-testid="calendar"]');
                // @ts-ignore — custom element exposes FC's internal Calendar instance
                const calendar = calendarEl && calendarEl.calendar;
                const resource = calendar && calendar.getResourceById('r1');
                return resource ? resource.extendedProps.department : null;
            });

        // Initial value set on server during resource construction
        expect(await readDepartment()).toBe('Engineering');

        // Trigger server-side update + updateResource round-trip
        await page.locator('[data-testid="btn-change-dept"]').click();
        await waitForVaadin(page);

        // Client-side extended prop must reflect the new value
        expect(await readDepartment()).toBe('Marketing');
    });
});
