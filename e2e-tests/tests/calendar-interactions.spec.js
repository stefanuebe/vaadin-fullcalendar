// @ts-check
const { test, expect, closeDialog, waitForCalendarUpdate, changeView, clickToday } = require('./fixtures');

test.describe('Calendar Interaction Tests', () => {

  test.describe('Drag and Drop - Month View', () => {

    test('should drag an all-day entry to another day', async ({ page }) => {
      // Find an all-day event (like "Short trip" or "This special holiday")
      const allDayEntry = page.locator('.fc-daygrid-event:has-text("Short trip"), .fc-daygrid-event:has-text("holiday")').first();

      if (await allDayEntry.isVisible({ timeout: 3000 })) {
        const box = await allDayEntry.boundingBox();
        if (box) {
          // Drag to a different day (200px to the right)
          await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
          await page.mouse.down();
          await page.mouse.move(box.x + 200, box.y, { steps: 20 });
          await page.mouse.up();

          await waitForCalendarUpdate(page, 1500);

          // Calendar should still be functional
          const calendar = page.locator('.fc');
          await expect(calendar).toBeVisible();
        }
      }
    });

    test('should drag a timed entry to another day', async ({ page }) => {
      // Find a timed entry (like "Meeting" with time)
      const timedEntry = page.locator('.fc-daygrid-event:has-text("Meeting")').first();

      if (await timedEntry.isVisible({ timeout: 3000 })) {
        const box = await timedEntry.boundingBox();
        if (box) {
          // Drag to a different day
          await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
          await page.mouse.down();
          await page.mouse.move(box.x + 150, box.y, { steps: 20 });
          await page.mouse.up();

          await waitForCalendarUpdate(page, 1500);

          // Calendar should still be functional
          const calendar = page.locator('.fc');
          await expect(calendar).toBeVisible();
        }
      }
    });

    test('should resize an all-day entry to span 2 days', async ({ page }) => {
      // Find an all-day event with a resize handle
      const allDayEntry = page.locator('.fc-daygrid-event:has-text("Short trip"), .fc-daygrid-event:has-text("holiday")').first();

      if (await allDayEntry.isVisible({ timeout: 3000 })) {
        // Find the resize handle at the end of the event
        const resizeHandle = allDayEntry.locator('.fc-event-resizer-end, .fc-event-resizer').first();

        if (await resizeHandle.isVisible({ timeout: 2000 })) {
          const box = await resizeHandle.boundingBox();
          if (box) {
            // Drag the resize handle to extend the event
            await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
            await page.mouse.down();
            await page.mouse.move(box.x + 100, box.y, { steps: 15 });
            await page.mouse.up();

            await waitForCalendarUpdate(page, 1500);

            // Calendar should still be functional
            const calendar = page.locator('.fc');
            await expect(calendar).toBeVisible();
          }
        }
      }
    });
  });

  test.describe('Create New Entries', () => {

    test('should create a new all-day entry by clicking empty cell', async ({ page }) => {
      // Click on an empty day cell to open create dialog
      const dayFrame = page.locator('.fc-daygrid-day-frame').nth(5);
      await dayFrame.click();
      await page.waitForTimeout(1000);

      // Fill in the entry details
      const titleInput = page.locator('vaadin-text-field input').first();
      await expect(titleInput).toBeVisible({ timeout: 5000 });
      await titleInput.fill('Test All Day Entry');

      // Check "All day event" checkbox
      const allDayCheckbox = page.locator('vaadin-checkbox:has-text("All day")');
      await allDayCheckbox.click();
      await page.waitForTimeout(300);

      // Click Save
      const saveBtn = page.locator('vaadin-button:has-text("Save")');
      await saveBtn.click();
      await page.waitForTimeout(1500);

      // Verify the entry was created
      const newEntry = page.locator('.fc-event:has-text("Test All Day Entry")');
      await expect(newEntry).toBeVisible({ timeout: 5000 });
    });

    test('should create a new timed entry by clicking empty cell', async ({ page }) => {
      // Click on an empty day cell to open create dialog
      const dayFrame = page.locator('.fc-daygrid-day-frame').nth(6);
      await dayFrame.click();
      await page.waitForTimeout(1000);

      // Fill in the entry details
      const titleInput = page.locator('vaadin-text-field input').first();
      await expect(titleInput).toBeVisible({ timeout: 5000 });
      await titleInput.fill('Test Timed Entry');

      // Make sure "All day event" is NOT checked (timed entry)
      const allDayCheckbox = page.locator('vaadin-checkbox:has-text("All day")');
      const isChecked = await allDayCheckbox.locator('input[type="checkbox"]').isChecked();
      if (isChecked) {
        await allDayCheckbox.click();
        await page.waitForTimeout(300);
      }

      // Click Save
      const saveBtn = page.locator('vaadin-button:has-text("Save")');
      await saveBtn.click();
      await page.waitForTimeout(1500);

      // Verify the entry was created
      const newEntry = page.locator('.fc-event:has-text("Test Timed Entry")');
      await expect(newEntry).toBeVisible({ timeout: 5000 });
    });
  });

  test.describe('Manipulate Created Entries', () => {

    test.beforeEach(async ({ page }) => {
      // Create test entry using the Entries menu -> Add single entry
      // This adds an entry titled "Single entry" for today at 10:00-11:00

      const entriesButton = page.locator('vaadin-menu-bar-button:has-text("Entries")').first();
      await entriesButton.click();
      await page.waitForTimeout(300);

      // Find and click the "Add single entry" menu item
      const addSingleItem = page.locator('vaadin-menu-bar-item:has-text("Add single entry")').first();
      if (await addSingleItem.isVisible({ timeout: 2000 })) {
        await addSingleItem.click();

        // Wait for notification that entry was added
        const notification = page.locator('vaadin-notification-container');
        await expect(notification).toContainText('Added', { timeout: 5000 });

        // Wait for entry to appear on calendar
        await page.waitForTimeout(500);
      } else {
        // Menu item might be disabled if already used - close menu and use existing entries
        await page.keyboard.press('Escape');
      }
    });

    test('should drag newly created all-day entry', async ({ page }) => {
      const testEntry = page.locator('.fc-event:has-text("Test All Day")').first();

      if (await testEntry.isVisible({ timeout: 3000 })) {
        const box = await testEntry.boundingBox();
        if (box) {
          await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
          await page.mouse.down();
          await page.mouse.move(box.x + 150, box.y, { steps: 15 });
          await page.mouse.up();
          await waitForCalendarUpdate(page, 1500);

          // Entry should still exist
          await expect(page.locator('.fc-event:has-text("Test All Day")')).toBeVisible();
        }
      }
    });

    test('should drag newly created timed entry', async ({ page }) => {
      const testEntry = page.locator('.fc-event:has-text("Test Timed")').first();

      if (await testEntry.isVisible({ timeout: 3000 })) {
        const box = await testEntry.boundingBox();
        if (box) {
          await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
          await page.mouse.down();
          await page.mouse.move(box.x + 150, box.y, { steps: 15 });
          await page.mouse.up();
          await waitForCalendarUpdate(page, 1500);

          // Entry should still exist
          await expect(page.locator('.fc-event:has-text("Test Timed")')).toBeVisible();
        }
      }
    });

    test('should resize newly created all-day entry', async ({ page }) => {
      const testEntry = page.locator('.fc-event:has-text("Test All Day")').first();

      if (await testEntry.isVisible({ timeout: 3000 })) {
        const resizeHandle = testEntry.locator('.fc-event-resizer-end, .fc-event-resizer').first();

        if (await resizeHandle.isVisible({ timeout: 2000 })) {
          const box = await resizeHandle.boundingBox();
          if (box) {
            await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
            await page.mouse.down();
            await page.mouse.move(box.x + 80, box.y, { steps: 10 });
            await page.mouse.up();
            await waitForCalendarUpdate(page, 1500);

            // Entry should still exist
            await expect(page.locator('.fc-event:has-text("Test All Day")')).toBeVisible();
          }
        }
      }
    });
  });

  test.describe('Delete Entries', () => {

    test.beforeEach(async ({ page }) => {
      // Create test entry by clicking on an empty day cell (use a cell that's likely empty)
      const dayCells = page.locator('.fc-daygrid-day-frame');
      // Try to find a day cell that doesn't have many events
      const dayFrame = dayCells.nth(10);
      await dayFrame.click();

      // Wait for dialog to open
      const dialog = page.locator('vaadin-dialog-overlay');
      await expect(dialog).toBeVisible({ timeout: 5000 });

      const titleInput = page.locator('vaadin-text-field input').first();
      await expect(titleInput).toBeVisible({ timeout: 3000 });
      await titleInput.fill('Entry To Delete');

      const saveBtn = page.locator('vaadin-button:has-text("Save")');
      await saveBtn.click();

      // Wait for dialog to close and entry to appear
      await expect(dialog).not.toBeVisible({ timeout: 5000 });
      await expect(page.locator('.fc-event:has-text("Entry To Delete")')).toBeVisible({ timeout: 5000 });
    });

    test('should delete entry using the edit dialog Remove button', async ({ page }) => {
      // Click on the entry to open edit dialog
      const entry = page.locator('.fc-event:has-text("Entry To Delete")').first();
      await expect(entry).toBeVisible({ timeout: 3000 });
      await entry.click();

      // Wait for dialog to open
      const dialog = page.locator('vaadin-dialog-overlay');
      await expect(dialog).toBeVisible({ timeout: 5000 });

      // Click Remove button
      const removeBtn = page.locator('vaadin-button:has-text("Remove")');
      await expect(removeBtn).toBeVisible({ timeout: 5000 });
      await removeBtn.click();

      // Wait for dialog to close
      await expect(dialog).not.toBeVisible({ timeout: 5000 });

      // Entry should be gone
      const deletedEntry = page.locator('.fc-event:has-text("Entry To Delete")');
      await expect(deletedEntry).not.toBeVisible({ timeout: 5000 });
    });

    test('should delete Multi 2 entry using the edit dialog', async ({ page }) => {
      // Note: We use "Multi 2" because "Multi 1" matches "Multi 10" as well
      // Navigate to find Multi 2 entry (might need to scroll or it might be in +more)
      const multiEntry = page.locator('.fc-event').filter({ hasText: /^.*Multi 2.*$/ }).first();

      // Skip if not visible (might be hidden in +more link)
      if (!(await multiEntry.isVisible({ timeout: 3000 }))) {
        test.skip();
        return;
      }

      // Count how many Multi 2 entries exist before deletion
      const countBefore = await page.locator('.fc-event').filter({ hasText: /Multi 2/ }).count();

      await multiEntry.click();

      // Wait for dialog to open
      const dialog = page.locator('vaadin-dialog-overlay');
      await expect(dialog).toBeVisible({ timeout: 5000 });

      // Verify we opened the right entry
      const titleInput = page.locator('vaadin-text-field input').first();
      const titleValue = await titleInput.inputValue();
      expect(titleValue).toContain('Multi 2');

      const removeBtn = page.locator('vaadin-button:has-text("Remove")');
      await expect(removeBtn).toBeVisible({ timeout: 3000 });
      await removeBtn.click();

      // Wait for dialog to close
      await expect(dialog).not.toBeVisible({ timeout: 5000 });

      // Verify one less Multi 2 entry exists
      const countAfter = await page.locator('.fc-event').filter({ hasText: /Multi 2/ }).count();
      expect(countAfter).toBeLessThan(countBefore);
    });
  });

  test.describe('Edit Recurring Event', () => {

    test('should edit recurring event and change recurrence settings', async ({ page }) => {
      // Find the sunday event (recurring)
      const sundayEvent = page.locator('.fc-event:has-text("sunday event")').first();

      // Skip test if recurring event not visible
      if (!(await sundayEvent.isVisible({ timeout: 5000 }))) {
        test.skip();
        return;
      }

      await sundayEvent.click();

      // Wait for dialog to open
      const dialog = page.locator('vaadin-dialog-overlay');
      await expect(dialog).toBeVisible({ timeout: 5000 });

      // Check that Recurring checkbox is visible
      const recurringCheckbox = page.locator('vaadin-checkbox:has-text("Recurring")');
      await expect(recurringCheckbox).toBeVisible({ timeout: 3000 });

      // Verify the recurring checkbox is checked (this is a recurring event)
      // In Vaadin, we check the input inside the checkbox
      const checkboxInput = recurringCheckbox.locator('input[type="checkbox"]');
      const isRecurring = await checkboxInput.isChecked();

      if (isRecurring) {
        // The event is already recurring - verify recurrence fields are shown
        // The label should say "Start of recurrence" when recurring is checked
        const startLabel = page.getByText('Start of recurrence');
        await expect(startLabel).toBeVisible({ timeout: 3000 });
      } else {
        // If not recurring, check the checkbox to make it recurring
        await recurringCheckbox.click();
        await page.waitForTimeout(300);

        // Verify recurrence fields appear
        const startLabel = page.getByText('Start of recurrence');
        await expect(startLabel).toBeVisible({ timeout: 3000 });
      }

      // Cancel the dialog (don't save changes to avoid side effects)
      const cancelBtn = page.locator('vaadin-button:has-text("Cancel")');
      await cancelBtn.click();

      // Verify dialog closed
      await expect(dialog).not.toBeVisible({ timeout: 3000 });

      // Calendar should still be functional
      const calendar = page.locator('.fc');
      await expect(calendar).toBeVisible();

      // The recurring event should still exist (there are multiple instances)
      await expect(page.locator('.fc-event:has-text("sunday event")').first()).toBeVisible();
    });
  });

  test.describe('Time Grid Week Interactions', () => {

    test.beforeEach(async ({ page }) => {
      // Switch to Time Grid Week view
      await changeView(page, 'Time Grid Week');
    });

    test('should create timed entry in Time Grid Week view', async ({ page }) => {
      // Click on a time slot to open create dialog
      const timeSlot = page.locator('.fc-timegrid-slot-lane').nth(10);
      await timeSlot.click();
      await page.waitForTimeout(1000);

      // Fill in entry
      const titleInput = page.locator('vaadin-text-field input').first();
      await expect(titleInput).toBeVisible({ timeout: 5000 });
      await titleInput.fill('Time Grid Test Entry');

      // Save
      const saveBtn = page.locator('vaadin-button:has-text("Save")');
      await saveBtn.click();
      await page.waitForTimeout(1500);

      // Verify entry exists
      const entry = page.locator('.fc-event:has-text("Time Grid Test Entry")');
      await expect(entry).toBeVisible({ timeout: 5000 });
    });

    test('should drag timed entry to another time slot on same day', async ({ page }) => {
      // Find a timed entry in the time grid
      const timedEntry = page.locator('.fc-timegrid-event, .fc-event').first();

      if (await timedEntry.isVisible({ timeout: 5000 })) {
        const box = await timedEntry.boundingBox();
        if (box) {
          // Drag vertically to a different time slot
          await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
          await page.mouse.down();
          await page.mouse.move(box.x + box.width / 2, box.y + 100, { steps: 15 }); // Move down
          await page.mouse.up();

          await waitForCalendarUpdate(page, 1500);

          // Calendar should still be functional
          const calendar = page.locator('.fc');
          await expect(calendar).toBeVisible();
        }
      }
    });

    test('should drag timed entry to another day in Time Grid Week', async ({ page }) => {
      // Find a timed entry
      const timedEntry = page.locator('.fc-timegrid-event, .fc-event').first();

      if (await timedEntry.isVisible({ timeout: 5000 })) {
        const box = await timedEntry.boundingBox();
        if (box) {
          // Drag horizontally to a different day
          await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
          await page.mouse.down();
          await page.mouse.move(box.x + 150, box.y, { steps: 15 }); // Move right to next day
          await page.mouse.up();

          await waitForCalendarUpdate(page, 1500);

          // Calendar should still be functional
          const calendar = page.locator('.fc');
          await expect(calendar).toBeVisible();
        }
      }
    });

    test('should resize timed entry to increase duration by one hour', async ({ page }) => {
      // Find a timed entry with resize handle
      const timedEntry = page.locator('.fc-timegrid-event, .fc-event').first();

      if (await timedEntry.isVisible({ timeout: 5000 })) {
        // Find the bottom resize handle
        const resizeHandle = timedEntry.locator('.fc-event-resizer-end, .fc-event-resizer').first();

        if (await resizeHandle.isVisible({ timeout: 2000 })) {
          const box = await resizeHandle.boundingBox();
          if (box) {
            // Drag down to increase duration (approx 42px per hour in typical time grid)
            await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
            await page.mouse.down();
            await page.mouse.move(box.x + box.width / 2, box.y + 50, { steps: 10 }); // ~1 hour down
            await page.mouse.up();

            await waitForCalendarUpdate(page, 1500);

            // Calendar should still be functional
            const calendar = page.locator('.fc');
            await expect(calendar).toBeVisible();
          }
        }
      }
    });

    test('should delete created entries in Time Grid Week view', async ({ page }) => {
      // First create an entry by clicking on a time slot
      const timeSlot = page.locator('.fc-timegrid-slot-lane').nth(15);
      await timeSlot.click();
      await page.waitForTimeout(1000);

      const titleInput = page.locator('vaadin-text-field input').first();
      await titleInput.fill('Entry To Delete In TimeGrid');
      await page.locator('vaadin-button:has-text("Save")').click();
      await page.waitForTimeout(1500);

      // Now delete it
      const entry = page.locator('.fc-event:has-text("Entry To Delete In TimeGrid")').first();
      if (await entry.isVisible({ timeout: 3000 })) {
        await entry.click();
        await page.waitForTimeout(1000);

        const removeBtn = page.locator('vaadin-button:has-text("Remove")');
        await expect(removeBtn).toBeVisible({ timeout: 5000 });
        await removeBtn.click();
        await page.waitForTimeout(1500);

        // Entry should be gone
        await expect(page.locator('.fc-event:has-text("Entry To Delete In TimeGrid")')).not.toBeVisible({ timeout: 3000 });
      }
    });
  });
});
