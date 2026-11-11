// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * A rule without dtstart starts at the start of its entry, in the time zone of the calendar (#451), also when the
 * rule has a positional weekday and goes to the client as text (#452). See RRuleEntryStartTestView.
 */
test.describe('RRule entry start', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/rrule-entry-start');
        await page.waitForSelector('.fc-timeGridWeek-view', { timeout: 10000 });
        await page.waitForSelector('.plain-entry', { timeout: 5000 });
        await waitForVaadin(page);
    });

    test('entry without rule is shown at 15:00 Berlin time', async ({ page }) => {
        await expect(page.locator('.plain-entry')).toContainText('15:00');
    });

    test('rule without dtstart starts at the time the entry shows without rule', async ({ page }) => {
        const occurrence = page.locator('.rule-entry');
        await expect(occurrence).toHaveCount(1);
        await expect(occurrence).toContainText('15:00');
        await expect(page.locator('[data-date="2025-01-07"] .rule-entry')).toHaveCount(1);
    });

    test('positional rule without dtstart shows its occurrence', async ({ page }) => {
        const occurrence = page.locator('.positional-entry');
        await expect(occurrence).toHaveCount(1);
        await expect(occurrence).toHaveText(/(^|\D)9:00/); // German format, no leading zero
        await expect(page.locator('[data-date="2025-01-09"] .positional-entry')).toHaveCount(1);
    });
});

// With Timezone.LOCAL the server knows the zone only once the browser has reported it. The first fetch must already use it.
test.describe('RRule entry start, calendar following the browser', () => {
    test.use({ timezoneId: 'Europe/Berlin' });

    test.beforeEach(async ({ page }) => {
        await page.goto('/test/rrule-entry-start?local');
        await page.waitForSelector('.fc-timeGridWeek-view', { timeout: 10000 });
        await page.waitForSelector('.plain-entry', { timeout: 5000 });
        await waitForVaadin(page);
    });

    test('rule without dtstart starts at the time the entry shows without rule', async ({ page }) => {
        await expect(page.locator('.plain-entry')).toContainText('15:00');
        await expect(page.locator('.rule-entry')).toContainText('15:00');
        await expect(page.locator('[data-date="2025-01-07"] .rule-entry')).toHaveCount(1);
    });
});
