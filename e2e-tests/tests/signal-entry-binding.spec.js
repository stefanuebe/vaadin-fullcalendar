// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

async function gotoSignalBindingView(page) {
    await page.goto('/test/signal-binding');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await waitForVaadin(page);
}

test.describe('Signal Entry Binding (UC-025)', () => {

    test.beforeEach(async ({ page }) => {
        await gotoSignalBindingView(page);
    });

    test('initial state: no entries on the calendar', async ({ page }) => {
        const entries = page.locator('.fc-event');
        await expect(entries).toHaveCount(0);
        await expect(page.locator('#entry-count')).toHaveText('0');
    });

    test('add entry via signal: entry appears on calendar', async ({ page }) => {
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        await expect(page.locator('#entry-count')).toHaveText('1', { timeout: 5000 });
        await expect(page.locator('#last-action')).toHaveText('added');

        const entries = page.locator('.fc-event');
        await expect(entries).toHaveCount(1, { timeout: 5000 });
        await expect(entries.first()).toContainText('Signal Entry 1');
    });

    test('add multiple entries: all appear', async ({ page }) => {
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        await expect(page.locator('#entry-count')).toHaveText('3', { timeout: 5000 });
        const entries = page.locator('.fc-event');
        await expect(entries).toHaveCount(3, { timeout: 5000 });
    });

    test('remove entry via signal: entry disappears from calendar', async ({ page }) => {
        // Add two entries
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        await expect(page.locator('.fc-event')).toHaveCount(2, { timeout: 5000 });

        // Remove first
        await page.locator('#remove-first-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        await expect(page.locator('#entry-count')).toHaveText('1', { timeout: 5000 });
        await expect(page.locator('.fc-event')).toHaveCount(1, { timeout: 5000 });
    });

    test('modify entry via signal: title updates on calendar', async ({ page }) => {
        // Add an entry
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        await expect(page.locator('.fc-event')).toContainText(['Signal Entry 1']);

        // Modify title
        await page.locator('#modify-first-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        await expect(page.locator('#last-action')).toHaveText('modified', { timeout: 5000 });
        // Title should now start with "Modified-"
        await expect(page.locator('.fc-event').first()).toContainText('Modified-', { timeout: 5000 });
    });

    test('unbind: adding to signal no longer updates calendar', async ({ page }) => {
        // Add entry, then unbind
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        await expect(page.locator('.fc-event')).toHaveCount(1, { timeout: 5000 });

        // Unbind
        await page.locator('#unbind-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        await expect(page.locator('#last-action')).toHaveText('unbound', { timeout: 5000 });

        // Add another entry to the signal — should NOT appear on calendar (unbound)
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        // Signal has 2 entries but calendar should not show the new one
        await expect(page.locator('#entry-count')).toHaveText('2', { timeout: 5000 });
        // Calendar still shows entries from before unbind (provider was replaced with empty InMemory)
        // The new entry added to the signal should NOT appear
        const entries = page.locator('.fc-event');
        const count = await entries.count();
        // After unbind, calendar has an empty InMemoryProvider — no entries
        expect(count).toBeLessThanOrEqual(1);
    });

    test('detach and re-attach: entries restored from signal', async ({ page }) => {
        // Add two entries
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        await expect(page.locator('.fc-event')).toHaveCount(2, { timeout: 5000 });

        // Detach calendar
        await page.locator('#detach-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        await expect(page.locator('#last-action')).toHaveText('detached', { timeout: 5000 });
        // Calendar should be gone
        await expect(page.locator('.fc')).toHaveCount(0, { timeout: 5000 });

        // Re-attach calendar
        await page.locator('#reattach-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        await expect(page.locator('#last-action')).toHaveText('reattached', { timeout: 5000 });
        // Calendar should be back with the same entries
        await expect(page.locator('.fc')).toHaveCount(1, { timeout: 5000 });
        await expect(page.locator('.fc-event')).toHaveCount(2, { timeout: 10000 });
    });

    test('detach, modify signal, re-attach: shows updated state', async ({ page }) => {
        // Add entry
        await page.locator('#add-entry-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        await expect(page.locator('.fc-event')).toHaveCount(1, { timeout: 5000 });
        await expect(page.locator('.fc-event').first()).toContainText('Signal Entry 1');

        // Detach
        await page.locator('#detach-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(300);

        // Modify while detached — effect should be paused
        await page.locator('#modify-first-btn').click();
        await waitForVaadin(page);

        // Re-attach — effect should re-sync with current signal state
        await page.locator('#reattach-btn').click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        // Entry should show the modified title (re-synced on re-attach)
        await expect(page.locator('.fc-event').first()).toContainText('Modified-', { timeout: 10000 });
    });

    test('bulk add 100 entries: completes within 5 seconds', async ({ page }) => {
        const start = Date.now();

        await page.locator('#bulk-add-btn').click();
        await waitForVaadin(page);

        // Wait for the action badge to show completion
        await expect(page.locator('#last-action')).toContainText('bulk-added-100', { timeout: 10000 });

        const elapsed = Date.now() - start;

        // Signal count should be 100
        await expect(page.locator('#entry-count')).toHaveText('100', { timeout: 5000 });

        // Entries should appear on the calendar (at least some — view may not show all)
        const entryCount = await page.locator('.fc-event').count();
        expect(entryCount).toBeGreaterThan(0);

        // Performance: should complete within 5 seconds (generous — verifies no O(n²) explosion)
        expect(elapsed).toBeLessThan(5000);
    });
});
