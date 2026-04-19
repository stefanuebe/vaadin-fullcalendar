// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

/**
 * Regression test for issue #202: FullCalendar assigns id="entry-<entryId>" to the
 * start segment of each rendered entry by default, so server-side components such as
 * Popover can anchor to a specific entry via document.getElementById.
 *
 * Verifies:
 *  - default on → single-day entry has id="entry-simple"
 *  - default on → multi-day (3 days) entry has exactly one DOM element with id="entry-multi"
 *    (the start segment; continuation segments remain id-free to preserve HTML uniqueness)
 *
 * Toggle behaviour is covered by unit tests (setter roundtrip + applyEntryDidMountMerge
 * side effect); DOM-level removal only takes effect on re-mount, which is FC's standard
 * behaviour and not an API guarantee worth pinning down in an E2E.
 */
test.describe('Auto-assign entry IDs (#202)', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/test/auto-assign-entry-ids');
        await page.waitForSelector('.fc', { timeout: 15000 });
        await page.waitForSelector('.fc-event', { timeout: 15000 });
        await waitForVaadin(page);
    });

    test('default-on: start segment gets id, continuation segments do not', async ({ page }) => {
        const simple = page.locator('#entry-simple');
        await expect(simple).toHaveCount(1);

        // Multi-day entry spans 3 days in month view → 3 DOM segments, but only 1 carries the id.
        const multi = page.locator('#entry-multi');
        await expect(multi).toHaveCount(1);
    });
});
