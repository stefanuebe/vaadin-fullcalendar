// @ts-check
const { test: base, expect } = require('@playwright/test');

/**
 * Custom test fixtures for FullCalendar tests
 */
const test = base.extend({
  /**
   * Auto-navigate to the playground page before each test
   */
  page: async ({ page }, use) => {
    // Navigate to the app and wait for it to load
    await page.goto('/');

    // Wait for the calendar to be visible
    await page.waitForSelector('.fc', { timeout: 30000 });

    // Wait for Vaadin to fully initialize - wait for the calendar entries to load
    await page.waitForFunction(() => {
      const calendar = document.querySelector('.fc');
      const entries = document.querySelectorAll('.fc-event');
      return calendar && entries.length > 0;
    }, { timeout: 10000 });

    await use(page);
  },
});

/**
 * Helper to wait for Vaadin client-server round trip to complete
 */
async function waitForVaadin(page) {
  // Wait for any pending Vaadin requests to complete
  await page.waitForFunction(() => {
    // Check if Vaadin is idle (no pending requests)
    const vaadin = window.Vaadin;
    if (vaadin && vaadin.Flow && vaadin.Flow.clients) {
      const clients = Object.values(vaadin.Flow.clients);
      return clients.every(client => !client.isActive || !client.isActive());
    }
    return true;
  }, { timeout: 5000 }).catch(() => {
    // Fallback: if Vaadin object not accessible, just wait a bit
  });
}

/**
 * Helper to close ALL open dialogs
 */
async function closeDialog(page) {
  // Close all open dialogs by clicking Cancel or pressing Escape
  let closed = false;

  // First, try to close via Cancel button on ALL visible dialogs
  const dialogs = page.locator('vaadin-dialog-overlay');
  const dialogCount = await dialogs.count();

  for (let i = dialogCount - 1; i >= 0; i--) {
    try {
      // Try clicking Cancel on the topmost dialog first (last in DOM)
      const cancelBtn = page.locator('vaadin-button:has-text("Cancel")').last();
      if (await cancelBtn.isVisible({ timeout: 300 })) {
        await cancelBtn.click();
        closed = true;
        // Wait for dialog to close
        await page.waitForTimeout(200);
      }
    } catch (e) {
      // Try Escape key as fallback
      await page.keyboard.press('Escape');
      await page.waitForTimeout(200);
    }
  }

  // Final cleanup: press Escape to close any remaining dialogs/popovers
  try {
    await page.keyboard.press('Escape');
    await page.waitForTimeout(100);
  } catch (e) {
    // Ignore
  }

  return closed;
}

/**
 * Helper to close all dialogs and wait for them to be gone
 */
async function closeAllDialogs(page) {
  let attempts = 0;
  while (attempts < 5) {
    const dialogCount = await page.locator('vaadin-dialog-overlay').count();
    if (dialogCount === 0) break;

    await closeDialog(page);
    attempts++;
  }
}

/**
 * Helper to wait for calendar to stabilize after an action
 */
async function waitForCalendarUpdate(page, timeout = 500) {
  // Wait for Vaadin to complete any pending requests
  await waitForVaadin(page);
  // Small additional wait for DOM updates
  await page.waitForTimeout(Math.min(timeout, 300));
}

/**
 * Get all visible calendar entries
 */
async function getCalendarEntries(page) {
  return page.locator('.fc-event').all();
}

/**
 * Click on the first entry matching the text
 */
async function clickEntry(page, text) {
  const entry = page.locator(`.fc-event:has-text("${text}")`).first();
  await entry.click();
  await page.waitForTimeout(500);
}

/**
 * Navigate to next/previous month using the toolbar buttons
 */
async function navigateMonth(page, direction = 'next') {
  // Navigation buttons are vaadin-button with vaadin-icon
  const iconName = direction === 'next' ? 'angle-right' : 'angle-left';
  const btn = page.locator(`vaadin-button:has(vaadin-icon[icon="vaadin:${iconName}"])`).first();
  await btn.click();
  await waitForCalendarUpdate(page);
}

/**
 * Click the Today button
 */
async function clickToday(page) {
  await page.locator('vaadin-button:has-text("Today")').click();
  await waitForCalendarUpdate(page);
}

/**
 * Change the calendar view using the View MenuBar dropdown
 * The View selector is a MenuBar with MenuItem, NOT a Select
 */
async function changeView(page, viewName) {
  // Click the View menu bar button (shows "View: Day Grid Month")
  const viewMenuButton = page.locator('vaadin-menu-bar-button:has-text("View:")').first();
  await viewMenuButton.click();
  await page.waitForTimeout(500);

  // Click the view option using exact text match to avoid partial matches
  // (e.g., "Time Grid Week" vs "Resource Time Grid Week")
  const option = page.locator('vaadin-menu-bar-list-box').getByText(viewName, { exact: true });
  if (await option.isVisible({ timeout: 2000 })) {
    await option.click();
    await waitForCalendarUpdate(page, 1500);
  } else {
    await page.keyboard.press('Escape');
  }
}

/**
 * Open the Entries menu and click an item
 */
async function clickEntriesMenuItem(page, itemText) {
  const entriesButton = page.locator('vaadin-menu-bar-button:has-text("Entries")').first();
  await entriesButton.click();
  await page.waitForTimeout(500);

  // Scope to list-box to avoid matching items from other menus
  const menuItem = page.locator(`vaadin-menu-bar-list-box vaadin-menu-bar-item:has-text("${itemText}")`).first();
  if (await menuItem.isVisible({ timeout: 2000 })) {
    await menuItem.click();
    await page.waitForTimeout(500);
    return true;
  }
  await page.keyboard.press('Escape');
  return false;
}

/**
 * Open the Settings menu
 */
async function openSettingsMenu(page) {
  const settingsButton = page.locator('vaadin-menu-bar-button:has-text("Settings")').first();
  await settingsButton.click();
  await page.waitForTimeout(500);
}

module.exports = {
  test,
  expect,
  closeDialog,
  closeAllDialogs,
  waitForCalendarUpdate,
  waitForVaadin,
  getCalendarEntries,
  clickEntry,
  navigateMonth,
  clickToday,
  changeView,
  clickEntriesMenuItem,
  openSettingsMenu,
};
