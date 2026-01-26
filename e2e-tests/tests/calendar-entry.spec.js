// @ts-check
const { test, expect, closeDialog, closeAllDialogs, clickEntry, waitForCalendarUpdate } = require('./fixtures');

test.describe('Calendar Entry Interactions', () => {

  test('should display calendar entries on initial load', async ({ page }) => {
    const entries = await page.locator('.fc-event').all();
    expect(entries.length).toBeGreaterThan(0);
  });

  test('should open entry dialog when clicking an entry', async ({ page }) => {
    // Click on the first entry
    await page.locator('.fc-event').first().click();
    await page.waitForTimeout(1000);

    // Verify dialog opened - vaadin-dialog-overlay
    const dialog = page.locator('vaadin-dialog-overlay');
    await expect(dialog).toBeVisible({ timeout: 5000 });

    // Verify dialog has the Save button (from DemoDialog.java)
    const saveBtn = page.locator('vaadin-button:has-text("Save")');
    await expect(saveBtn).toBeVisible();

    await closeDialog(page);
  });

  test('should show entry details in dialog', async ({ page }) => {
    // Click on a known entry
    const entry = page.locator('.fc-event:has-text("Meeting")').first();
    if (await entry.isVisible()) {
      await entry.click();
      await page.waitForTimeout(1000);

      // Dialog opens - verify title field has text
      const dialog = page.locator('vaadin-dialog-overlay');
      await expect(dialog).toBeVisible({ timeout: 5000 });

      // The first text-field is the Title field
      const titleInput = page.locator('vaadin-text-field input').first();
      await expect(titleInput).toBeVisible();
      const titleValue = await titleInput.inputValue();
      expect(titleValue).toContain('Meeting');

      await closeDialog(page);
    }
  });

  test('should have All day event checkbox', async ({ page }) => {
    await page.locator('.fc-event').first().click();
    await page.waitForTimeout(1000);

    // From DemoDialog.java: Checkbox fieldAllDay = new Checkbox("All day event");
    const allDayCheckbox = page.locator('vaadin-checkbox:has-text("All day")');
    await expect(allDayCheckbox).toBeVisible({ timeout: 5000 });

    await closeDialog(page);
  });

  test('should have Recurring event checkbox', async ({ page }) => {
    await page.locator('.fc-event').first().click();
    await page.waitForTimeout(1000);

    // From DemoDialog.java: Checkbox fieldRecurring = new Checkbox("Recurring event");
    const recurringCheckbox = page.locator('vaadin-checkbox:has-text("Recurring")');
    await expect(recurringCheckbox).toBeVisible({ timeout: 5000 });

    await closeDialog(page);
  });

  test('should show recurrence fields when Recurring is checked', async ({ page }) => {
    await page.locator('.fc-event').first().click();

    // Wait for dialog to open
    const dialog = page.locator('vaadin-dialog-overlay');
    await expect(dialog).toBeVisible({ timeout: 5000 });

    // Check the recurring checkbox
    const recurringCheckbox = page.locator('vaadin-checkbox:has-text("Recurring")');
    await expect(recurringCheckbox).toBeVisible({ timeout: 3000 });

    // Check if it's already checked
    const checkboxInput = recurringCheckbox.locator('input[type="checkbox"]');
    const isAlreadyChecked = await checkboxInput.isChecked();

    if (!isAlreadyChecked) {
      // Click to check it
      await recurringCheckbox.click();
    }

    // From DemoDialog.java: fieldStart.setLabel("Start of recurrence");
    // The CustomDateTimePicker should now show "Start of recurrence"
    const startLabel = page.getByText('Start of recurrence');
    await expect(startLabel).toBeVisible({ timeout: 5000 });

    await closeDialog(page);
  });

  test('should open dialog for multi-day events', async ({ page }) => {
    // Click on "Short trip" multi-day event
    const shortTrip = page.locator('.fc-event:has-text("Short trip")').first();

    if (await shortTrip.isVisible()) {
      await shortTrip.click();
      await page.waitForTimeout(1000);

      const dialog = page.locator('vaadin-dialog-overlay');
      await expect(dialog).toBeVisible({ timeout: 5000 });

      // Verify it's the correct entry
      const titleInput = page.locator('vaadin-text-field input').first();
      const titleValue = await titleInput.inputValue();
      expect(titleValue).toContain('Short trip');

      await closeDialog(page);
    }
  });

  test('should handle double-click on entry', async ({ page }) => {
    // Note: Double-click in FullCalendar fires click events for each click,
    // which may open multiple dialogs (one per click). This is expected behavior.
    await page.locator('.fc-event').first().dblclick();

    // Wait for at least one dialog to be visible
    const dialog = page.locator('vaadin-dialog-overlay');
    await expect(dialog.first()).toBeVisible({ timeout: 5000 });

    // Close ALL open dialogs (there may be multiple from double-click)
    await closeAllDialogs(page);

    // Verify all dialogs are closed
    await expect(page.locator('vaadin-dialog-overlay')).toHaveCount(0, { timeout: 3000 });
  });

  test('should open create dialog when clicking empty day slot', async ({ page }) => {
    // Click on an empty area of the calendar
    const dayFrame = page.locator('.fc-daygrid-day-frame').first();
    await dayFrame.click();
    await page.waitForTimeout(1000);

    // Should open a dialog for creating new entry
    const dialog = page.locator('vaadin-dialog-overlay');
    await expect(dialog).toBeVisible({ timeout: 5000 });

    await closeDialog(page);
  });

  test('should show popover when clicking +more link', async ({ page }) => {
    // Find and click a "+more" link if it exists
    const moreLink = page.locator('.fc-more-link, .fc-daygrid-more-link').first();

    if (await moreLink.isVisible({ timeout: 2000 })) {
      await moreLink.click();
      await page.waitForTimeout(500);

      // Verify popover appears with entries
      const popover = page.locator('.fc-popover');
      await expect(popover).toBeVisible();

      // Popover should contain events
      const popoverEvents = await page.locator('.fc-popover .fc-event').count();
      expect(popoverEvents).toBeGreaterThan(0);

      // Close popover
      await page.keyboard.press('Escape');
    }
  });

  test('should support drag and drop of entries', async ({ page }) => {
    const entry = page.locator('.fc-event:has-text("Meeting")').first();

    if (await entry.isVisible()) {
      const box = await entry.boundingBox();
      if (box) {
        // Perform drag and drop
        await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
        await page.mouse.down();
        await page.mouse.move(box.x + 150, box.y, { steps: 10 });
        await page.mouse.up();

        await waitForCalendarUpdate(page);

        // No error should occur - calendar should still be visible
        const calendar = page.locator('.fc');
        await expect(calendar).toBeVisible();
      }
    }
  });

  test('should create entry from date range selection', async ({ page }) => {
    // Select a range of days by dragging
    const dayCells = await page.locator('.fc-daygrid-day-frame').all();

    if (dayCells.length >= 15) {
      const firstDay = await dayCells[10].boundingBox();
      const lastDay = await dayCells[14].boundingBox();

      if (firstDay && lastDay) {
        await page.mouse.move(firstDay.x + 10, firstDay.y + 10);
        await page.mouse.down();
        await page.mouse.move(lastDay.x + lastDay.width - 10, lastDay.y + 10, { steps: 10 });
        await page.mouse.up();

        await page.waitForTimeout(1000);

        // A dialog should open for creating a new entry with the date range
        const dialog = page.locator('vaadin-dialog-overlay');
        await expect(dialog).toBeVisible({ timeout: 5000 });

        // The "All day event" checkbox should be visible
        const allDayCheckbox = page.locator('vaadin-checkbox:has-text("All day")');
        await expect(allDayCheckbox).toBeVisible();

        await closeDialog(page);
      }
    }
  });
});
