// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

async function gotoView(page) {
  await page.goto('/test/fc7-options');
  await page.waitForSelector('.fc-daygrid-day', { timeout: 10000 });
  await waitForVaadin(page);
}

/** Switch the view with a Vaadin button of the test view and wait for a view specific element. */
async function switchView(page, buttonId, selector) {
  await page.locator('#' + buttonId).click();
  await page.waitForSelector(selector, { timeout: 10000 });
}

// =============================================================================

test.describe('FullCalendar 7 options', () => {

  test.beforeEach(async ({ page }) => {
    await gotoView(page);
  });

  // ---- BUTTONS -------------------------------------------------------------

  test('BUTTONS before attach: today text is overridden', async ({ page }) => {
    await expect(page.locator('.fc-toolbar button', { hasText: 'Jump to now' })).toBeVisible();
  });

  test('BUTTONS before attach: display shows the text instead of the icon', async ({ page }) => {
    const button = page.locator('.fc-toolbar button', { hasText: 'Text only' });
    await expect(button).toBeVisible();
    await expect(button.locator('.hook-icon')).toHaveCount(0);
    // control: without display the same kind of button shows only its icon
    await expect(page.locator('.fc-toolbar button[aria-label="Icon auto"] .hook-icon')).toHaveCount(1);
  });

  test('TOOLBAR_ELEMENTS: callback content and plain text render where they are placed', async ({ page }) => {
    await expect(page.locator('.fc-toolbar .hook-toolbar-element')).toHaveText('From callback');
    await expect(page.locator('.fc-toolbar', { hasText: 'Plain text element' })).toBeVisible();
  });

  test('BUTTONS before attach: custom button renders and runs its click callback', async ({ page }) => {
    const button = page.locator('.fc-toolbar button', { hasText: 'Custom One' });
    await expect(button).toBeVisible();
    await button.click();
    await expect(page.locator('body')).toHaveAttribute('data-custom1', 'clicked');
  });

  test('BUTTONS after attach: new custom button renders and runs its click callback', async ({ page }) => {
    await expect(page.locator('.fc-toolbar button', { hasText: 'Custom Two' })).toHaveCount(0);
    await page.locator('#apply-late-button').click();

    const button = page.locator('.fc-toolbar button', { hasText: 'Custom Two' });
    await expect(button).toBeVisible();
    await button.click();
    await expect(page.locator('body')).toHaveAttribute('data-custom2', 'clicked');
    // the buttons from before attach are still there
    await expect(page.locator('.fc-toolbar button', { hasText: 'Custom One' })).toBeVisible();
  });

  // ---- Class hooks per entry display kind ----------------------------------

  // In the month view FullCalendar 7 renders an all-day entry as both a block entry and a row entry and a timed entry as
  // a list item (dot).
  test('BLOCK_ENTRY_CLASS reaches the all-day entry in the month view', async ({ page }) => {
    await expect(page.locator('.hook-block', { hasText: 'All day' })).toBeVisible();
  });

  test('ROW_ENTRY_CLASS reaches the all-day entry in the month view', async ({ page }) => {
    await expect(page.locator('.hook-row', { hasText: 'All day' })).toBeVisible();
  });

  test('LIST_ITEM_ENTRY_CLASS reaches the timed entry in the month view', async ({ page }) => {
    await expect(page.locator('.hook-list-item', { hasText: 'Timed' })).toBeVisible();
  });

  test('COLUMN_ENTRY_CLASS reaches the timed entry in the time grid view', async ({ page }) => {
    await switchView(page, 'view-time-grid', '.fc-timegrid-slot');
    await expect(page.locator('.hook-column', { hasText: 'Timed' })).toBeVisible();
  });

  test('BACKGROUND_ENTRY_CLASS, _CONTENT and _DID_MOUNT reach the background entry', async ({ page }) => {
    const background = page.locator('.hook-background');
    await expect(background.first()).toBeVisible();
    await expect(background.first()).toHaveAttribute('data-bg-mounted', 'true');
    await expect(page.locator('.hook-background-content').first()).toHaveText('BG');
  });

  // ---- Toolbar, plain options -----------------------------------

  test('TOOLBAR_CLASS and BUTTON_CLASS reach the toolbar and its buttons', async ({ page }) => {
    await expect(page.locator('.fc-toolbar.hook-toolbar')).toBeVisible();
    await expect(page.locator('.hook-button', { hasText: 'Jump to now' })).toBeVisible();
  });

  test('NO_ENTRIES_TEXT is shown in an empty list view', async ({ page }) => {
    // The list view starts in March 2025. Next month has no entries.
    await switchView(page, 'view-list', '.fc-list');
    await page.locator('.fc-next-button').click();
    await expect(page.locator('.fc-list')).toContainText('Nothing planned');
  });

  test('HEADING_LEVEL sets aria-level 3 on the toolbar title', async ({ page }) => {
    await expect(page.locator('[role="heading"][aria-level="3"]')).toContainText('2025');
  });
});
