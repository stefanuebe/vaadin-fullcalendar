// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

/**
 * Regression: when the Scheduler's Resource plugin is loaded, FC patches
 * EventApi.prototype.getResources onto every entry — including on plain
 * (non-scheduler) calendars on the same page. Its body is
 * this._def.resourceIds.map(...) and throws when resourceIds is undefined
 * (any plain-calendar entry). The default eventDidMount snippet (#202)
 * must swallow that throw; otherwise the snippet aborts mid-render and
 * entries fail to appear on first attach (only show up after a reload).
 */
test.describe('Entry ID snippet survives Scheduler-plugin-patched getResources', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/test/entry-id-with-scheduler-on-page');
        await page.waitForSelector('[data-testid="plain-calendar"]', { timeout: 15000 });
        await waitForVaadin(page);
    });

    test('plain calendar entries render on first attach, no thrown errors', async ({ page }) => {
        const pageErrors = [];
        page.on('pageerror', err => pageErrors.push(err.message));

        const plain = page.locator('[data-testid="plain-calendar"]');
        // Wait until at least one entry is visible inside the plain calendar.
        await plain.locator('.fc-event').first().waitFor({ timeout: 10000 });

        const entryTitles = await plain.locator('.fc-event .fc-event-title').allTextContents();
        expect(entryTitles).toContain('Entry 1');
        expect(entryTitles).toContain('Entry 2');

        // Default-id on start segment
        await expect(page.locator('#entry-e1')).toHaveCount(1);
        await expect(page.locator('#entry-e2')).toHaveCount(1);

        expect(pageErrors).toEqual([]);
    });
});
