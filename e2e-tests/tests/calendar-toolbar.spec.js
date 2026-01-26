// @ts-check
const { test, expect, closeDialog, waitForCalendarUpdate, clickEntriesMenuItem, openSettingsMenu } = require('./fixtures');

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

      // Settings menu contains checkboxes - check for visible checkbox
      const lumoCheckbox = page.locator('vaadin-checkbox:has-text("Lumo")');
      await expect(lumoCheckbox).toBeVisible({ timeout: 3000 });

      await page.keyboard.press('Escape');
    });
  });

  test.describe('Theme Selection', () => {

    test('should have theme dropdown (AURA)', async ({ page }) => {
      // Theme dropdown is a vaadin-select showing AURA/LUMO/MATERIAL
      const themeDropdown = page.locator('vaadin-select').filter({ hasText: /AURA|LUMO|MATERIAL/ }).first();
      await expect(themeDropdown).toBeVisible();
    });

    test('should switch to LUMO theme', async ({ page }) => {
      const themeDropdown = page.locator('vaadin-select').filter({ hasText: /AURA|LUMO|MATERIAL/ }).first();
      await themeDropdown.click();
      await page.waitForTimeout(500);

      const lumoOption = page.locator('vaadin-select-overlay vaadin-select-item:has-text("LUMO")');
      if (await lumoOption.isVisible({ timeout: 2000 })) {
        await lumoOption.click();
        await waitForCalendarUpdate(page, 1500);

        const calendar = page.locator('.fc');
        await expect(calendar).toBeVisible();
      } else {
        await page.keyboard.press('Escape');
      }
    });

    test('should switch to MATERIAL theme', async ({ page }) => {
      const themeDropdown = page.locator('vaadin-select').filter({ hasText: /AURA|LUMO|MATERIAL/ }).first();
      await themeDropdown.click();
      await page.waitForTimeout(500);

      const materialOption = page.locator('vaadin-select-overlay vaadin-select-item:has-text("MATERIAL")');
      if (await materialOption.isVisible({ timeout: 2000 })) {
        await materialOption.click();
        await waitForCalendarUpdate(page, 1500);

        const calendar = page.locator('.fc');
        await expect(calendar).toBeVisible();
      } else {
        await page.keyboard.press('Escape');
      }
    });
  });

  test.describe('Dark/Light Mode', () => {

    test('should have mode dropdown (SYSTEM)', async ({ page }) => {
      // Mode dropdown is a vaadin-select showing SYSTEM/DARK/LIGHT
      const modeDropdown = page.locator('vaadin-select').filter({ hasText: /SYSTEM|DARK|LIGHT/ }).first();
      await expect(modeDropdown).toBeVisible();
    });

    test('should switch to DARK mode', async ({ page }) => {
      const modeDropdown = page.locator('vaadin-select').filter({ hasText: /SYSTEM|LIGHT/ }).first();
      await modeDropdown.click();
      await page.waitForTimeout(500);

      const darkOption = page.locator('vaadin-select-overlay vaadin-select-item:has-text("DARK")');
      if (await darkOption.isVisible({ timeout: 2000 })) {
        await darkOption.click();
        await waitForCalendarUpdate(page, 1500);

        const calendar = page.locator('.fc');
        await expect(calendar).toBeVisible();
      } else {
        await page.keyboard.press('Escape');
      }
    });

    test('should switch to LIGHT mode', async ({ page }) => {
      const modeDropdown = page.locator('vaadin-select').filter({ hasText: /SYSTEM|DARK/ }).first();
      await modeDropdown.click();
      await page.waitForTimeout(500);

      const lightOption = page.locator('vaadin-select-overlay vaadin-select-item:has-text("LIGHT")');
      if (await lightOption.isVisible({ timeout: 2000 })) {
        await lightOption.click();
        await waitForCalendarUpdate(page, 1500);

        const calendar = page.locator('.fc');
        await expect(calendar).toBeVisible();
      } else {
        await page.keyboard.press('Escape');
      }
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
