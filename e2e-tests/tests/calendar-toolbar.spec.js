// @ts-check
const { test, expect, closeDialog, clickEntriesMenuItem, openSettingsMenu } = require('./fixtures');

test.describe('Calendar Toolbar', () => {

  test.describe('Entries Menu', () => {

    test('should open Entries menu', async ({ page }) => {
      // Entries is a MenuBar button
      const entriesButton = page.locator('vaadin-menu-bar-button:has-text("Entries")');
      await entriesButton.click();
      await page.waitForTimeout(500);

      // Menu should be open - check for visible list-box with menu items
      const menuListBox = page.locator('vaadin-menu-bar-list-box');
      await expect(menuListBox).toBeVisible({ timeout: 3000 });

      await page.keyboard.press('Escape');
    });

    test('should have Add single entry option', async ({ page }) => {
      const entriesButton = page.locator('vaadin-menu-bar-button:has-text("Entries")');
      await entriesButton.click();
      await page.waitForTimeout(500);

      const addSingleEntry = page.locator('vaadin-menu-bar-list-box vaadin-menu-bar-item:has-text("Add single entry")');
      await expect(addSingleEntry).toBeVisible({ timeout: 3000 });

      await page.keyboard.press('Escape');
    });

    test('should create entry via Add single entry menu', async ({ page }) => {
      // Count entries before
      const initialCount = await page.locator('.fc-event').count();

      const result = await clickEntriesMenuItem(page, 'Add single entry');
      expect(result).toBe(true);

      await page.waitForTimeout(1000);

      // "Add single entry" creates an entry directly (no dialog)
      // Verify a new entry was created
      const newCount = await page.locator('.fc-event').count();
      expect(newCount).toBeGreaterThan(initialCount);
    });

    test('should have Add recurring entries option', async ({ page }) => {
      const entriesButton = page.locator('vaadin-menu-bar-button:has-text("Entries")');
      await entriesButton.click();
      await page.waitForTimeout(500);

      const addRecurring = page.locator('vaadin-menu-bar-list-box vaadin-menu-bar-item:has-text("Add recurring entries")');
      await expect(addRecurring).toBeVisible({ timeout: 3000 });

      await page.keyboard.press('Escape');
    });

    test('should have Add random entries option', async ({ page }) => {
      const entriesButton = page.locator('vaadin-menu-bar-button:has-text("Entries")');
      await entriesButton.click();
      await page.waitForTimeout(500);

      const addRandom = page.locator('vaadin-menu-bar-list-box vaadin-menu-bar-item:has-text("Add random entries")');
      await expect(addRandom).toBeVisible({ timeout: 3000 });

      await page.keyboard.press('Escape');
    });

    test('should have Remove all entries option', async ({ page }) => {
      const entriesButton = page.locator('vaadin-menu-bar-button:has-text("Entries")');
      await entriesButton.click();
      await page.waitForTimeout(500);

      const removeAll = page.locator('vaadin-menu-bar-list-box vaadin-menu-bar-item:has-text("Remove all entries")');
      await expect(removeAll).toBeVisible({ timeout: 3000 });

      await page.keyboard.press('Escape');
    });
  });

  test.describe('Settings Menu', () => {

    test('should open Settings menu', async ({ page }) => {
      await openSettingsMenu(page);

      // Settings menu contains the calendar theme submenu
      const themeItem = page.getByRole('menuitem', { name: 'Calendar theme' });
      await expect(themeItem).toBeVisible({ timeout: 3000 });

      await page.keyboard.press('Escape');
    });
  });

  test.describe('Vaadin Theme and Color Scheme', () => {

    /** A variable of the page root, empty when no stylesheet defines it. */
    const rootVariable = (page, name) => page.evaluate((n) =>
        getComputedStyle(document.documentElement).getPropertyValue(n).trim(), name);
    const rootColorScheme = (page) => page.evaluate(() => getComputedStyle(document.documentElement).colorScheme);

    async function choose(page, label, item) {
      const select = page.locator('vaadin-select').filter({ has: page.locator(`label:has-text("${label}")`) });
      // a click shortly after the previous choice, while its overlay still closes, does not open the select
      await expect(async () => {
        await select.click();
        await expect(select).toHaveAttribute('opened', '', { timeout: 1000 });
      }).toPass();
      await page.locator(`vaadin-select-item:has-text("${item}")`).click();
    }

    test('the app starts with Lumo and the system color scheme', async ({ page }) => {
      await expect(page.locator('vaadin-select').filter({ hasText: 'LUMO' })).toBeVisible();
      await expect(page.locator('vaadin-select').filter({ hasText: 'SYSTEM' })).toBeVisible();
      expect(await rootVariable(page, '--lumo-base-color')).not.toBe('');
      expect(await rootVariable(page, '--aura-accent-color')).toBe('');
    });

    test('the theme select switches the app to Aura', async ({ page }) => {
      await choose(page, 'Theme', 'AURA');

      await expect(page).toHaveURL(/theme=aura/);
      await expect.poll(() => rootVariable(page, '--aura-accent-color')).not.toBe('');
      expect(await rootVariable(page, '--lumo-base-color')).toBe('');
    });

    test('the mode select switches the color scheme to dark and to light', async ({ page }) => {
      await choose(page, 'Mode', 'DARK');
      await expect(page).toHaveURL(/scheme=dark/);
      await expect.poll(() => rootColorScheme(page)).toBe('dark');

      await choose(page, 'Mode', 'LIGHT');
      await expect(page).toHaveURL(/scheme=light/);
      await expect.poll(() => rootColorScheme(page)).toBe('light');
    });
  });

  test.describe('View Selection', () => {

    test('should have view dropdown (View: ...)', async ({ page }) => {
      // View dropdown is a MenuBar button showing "View: Day Grid Month"
      const viewDropdown = page.locator('vaadin-menu-bar-button:has-text("View:")');
      await expect(viewDropdown).toBeVisible();
    });

    test('should show available views in dropdown', async ({ page }) => {
      const viewDropdown = page.locator('vaadin-menu-bar-button:has-text("View:")');
      await viewDropdown.click();
      await page.waitForTimeout(500);

      // Should have multiple view options in the list box
      const menuListBox = page.locator('vaadin-menu-bar-list-box');
      await expect(menuListBox).toBeVisible({ timeout: 3000 });

      const options = page.locator('vaadin-menu-bar-list-box vaadin-menu-bar-item');
      const count = await options.count();
      expect(count).toBeGreaterThanOrEqual(3);

      await page.keyboard.press('Escape');
    });
  });
});
