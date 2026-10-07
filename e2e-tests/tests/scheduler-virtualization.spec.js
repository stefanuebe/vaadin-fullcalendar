// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

const RESOURCE_COUNT = 300;
const PRINT_MAX_ROWS = 20;
const ROWS = '.hook-resource-row';

test.describe('Scheduler VIRTUALIZATION, PRINT_MAX_ROWS and RESOURCE_ROW_CLASS', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/test/scheduler-virtualization');
    await page.waitForSelector('.fc-timeline', { timeout: 15000 });
    await waitForVaadin(page);
  });

  test('with virtualization the DOM holds only the visible resource rows', async ({ page }) => {
    await expect(page.locator(ROWS).first()).toBeVisible();
    const count = await page.locator(ROWS).count();
    expect(count).toBeGreaterThan(0);
    expect(count).toBeLessThan(RESOURCE_COUNT / 3);
  });

  test('without virtualization the DOM holds all resource rows', async ({ page }) => {
    await page.locator('#virtualization-off').click();
    await expect(page.locator(ROWS)).toHaveCount(RESOURCE_COUNT, { timeout: 15000 });
  });

  test('RESOURCE_ROW_CLASS reaches the resource rows', async ({ page }) => {
    await expect(page.locator(ROWS).first()).toBeVisible();
    await expect(page.locator(ROWS).first()).toContainText('Resource');
  });

  // the print rendering comes from FullCalendar's adaptive plugin, which the scheduler loads. It switches on with the
  // browser's beforeprint event, so dispatching it shows whether the plugin and PRINT_MAX_ROWS reached the client
  test('printing renders at most PRINT_MAX_ROWS resource rows', async ({ page }) => {
    await page.locator('#virtualization-off').click();
    await expect(page.locator(ROWS)).toHaveCount(RESOURCE_COUNT, { timeout: 15000 });

    await page.evaluate(() => window.dispatchEvent(new Event('beforeprint')));

    // count the rows that show a resource title, the print layout may repeat the row class on other elements
    await expect(page.locator(ROWS).filter({ hasText: /Resource \d+/ })).toHaveCount(PRINT_MAX_ROWS, { timeout: 15000 });
  });
});
