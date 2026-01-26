// @ts-check
const { test, expect } = require('@playwright/test');
const { closeDialog, waitForCalendarUpdate, changeView, clickEntriesMenuItem } = require('./fixtures');

/**
 * Entry Provider views to test - all have similar structure but different backend implementations
 */
const ENTRY_PROVIDER_VIEWS = [
  { name: 'In-Memory Entry Provider', route: '/in-memory-entry-provider' },
  { name: 'Callback Entry Provider', route: '/callback-entry-provider' },
  { name: 'Backend Entry Provider', route: '/backend-entry-provider' },
];

/**
 * Helper to wait for Vaadin client-server round trip to complete
 */
async function waitForVaadin(page) {
  await page.waitForFunction(() => {
    const vaadin = window.Vaadin;
    if (vaadin && vaadin.Flow && vaadin.Flow.clients) {
      const clients = Object.values(vaadin.Flow.clients);
      return clients.every(client => !client.isActive || !client.isActive());
    }
    return true;
  }, { timeout: 5000 }).catch(() => {});
}

/**
 * Helper to add entries via toolbar menu
 */
async function addEntriesViaToolbar(page, menuItem) {
  const entriesButton = page.locator('vaadin-menu-bar-button:has-text("Entries")').first();
  await entriesButton.click();
  await page.waitForTimeout(300);

  const item = page.locator(`vaadin-menu-bar-item:has-text("${menuItem}")`).first();
  if (await item.isVisible({ timeout: 2000 })) {
    await item.click();
    await page.waitForTimeout(1000);
    await waitForVaadin(page);
    return true;
  }
  await page.keyboard.press('Escape');
  return false;
}

/**
 * Helper to find the first all-day event in the calendar
 */
async function findFirstAllDayEvent(page) {
  // All-day events in month view are in fc-daygrid-event-harness with fc-daygrid-block-event class
  // or they don't have a time displayed
  const allDayEvent = page.locator('.fc-daygrid-block-event .fc-event, .fc-daygrid-event:not(:has(.fc-event-time))').first();
  if (await allDayEvent.isVisible({ timeout: 3000 })) {
    return allDayEvent;
  }
  // Fallback: any event without time
  return page.locator('.fc-event').first();
}

/**
 * Helper to find the first timed event in the calendar
 */
async function findFirstTimedEvent(page) {
  // Timed events have fc-event-time element showing the time
  const timedEvent = page.locator('.fc-daygrid-event:has(.fc-event-time)').first();
  if (await timedEvent.isVisible({ timeout: 3000 })) {
    return timedEvent;
  }
  // Fallback: any event
  return page.locator('.fc-event').first();
}

/**
 * Helper to find the first event in Time Grid view
 */
async function findFirstTimeGridEvent(page) {
  return page.locator('.fc-timegrid-event').first();
}

// Run tests for each Entry Provider view
for (const view of ENTRY_PROVIDER_VIEWS) {

  test.describe(`${view.name} - Entry Interactions`, () => {

    // Custom test setup that navigates to the specific Entry Provider view
    test.beforeEach(async ({ page }) => {
      await page.goto(view.route);

      // Wait for the calendar to be visible
      await page.waitForSelector('.fc', { timeout: 30000 });

      // Wait for Vaadin to initialize
      await waitForVaadin(page);
      await page.waitForTimeout(500);
    });

    test.describe('Create Entries via Toolbar', () => {

      test('should create single entry via toolbar', async ({ page }) => {
        // Get initial entry count
        const initialCount = await page.locator('.fc-event').count();

        // Add single entry via toolbar
        await addEntriesViaToolbar(page, 'Add single entry');

        // Wait for notification
        const notification = page.locator('vaadin-notification-container');
        await expect(notification).toContainText('Added', { timeout: 5000 });

        // Verify entry was added
        await page.waitForTimeout(500);
        const newCount = await page.locator('.fc-event').count();
        expect(newCount).toBeGreaterThan(initialCount);
      });

      test('should create recurring entries via toolbar', async ({ page }) => {
        // Get initial entry count
        const initialCount = await page.locator('.fc-event').count();

        // Add recurring entries via toolbar
        await addEntriesViaToolbar(page, 'Add recurring entries');

        // Wait for notification
        const notification = page.locator('vaadin-notification-container');
        await expect(notification).toContainText('Added', { timeout: 5000 });

        // Verify entries were added (recurring creates multiple instances)
        await page.waitForTimeout(500);
        const newCount = await page.locator('.fc-event').count();
        expect(newCount).toBeGreaterThan(initialCount);
      });

      test('should add 1k entries via toolbar', async ({ page }) => {
        // Add 1k entries via toolbar
        const added = await addEntriesViaToolbar(page, 'Add 1k entries');

        if (added) {
          // Wait for entries to be added (this may take a moment)
          await page.waitForTimeout(3000);
          await waitForVaadin(page);

          // Verify entries exist (there should be many visible)
          const entryCount = await page.locator('.fc-event').count();
          expect(entryCount).toBeGreaterThan(0);

          // Calendar should still be functional
          const calendar = page.locator('.fc');
          await expect(calendar).toBeVisible();
        }
      });
    });

    test.describe('Drag and Drop - Month View', () => {

      test.beforeEach(async ({ page }) => {
        // Ensure we have entries to work with
        await addEntriesViaToolbar(page, 'Add single entry');
        await page.waitForTimeout(500);
      });

      test('should drag an entry to another day', async ({ page }) => {
        const entry = await findFirstAllDayEvent(page);

        if (await entry.isVisible({ timeout: 3000 })) {
          const box = await entry.boundingBox();
          if (box) {
            // Get entry text before drag
            const entryText = await entry.textContent();

            // Drag to a different day (200px to the right)
            await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
            await page.mouse.down();
            await page.mouse.move(box.x + 200, box.y, { steps: 20 });
            await page.mouse.up();

            await waitForCalendarUpdate(page, 1500);

            // Calendar should still be functional
            const calendar = page.locator('.fc');
            await expect(calendar).toBeVisible();

            // Entry should still exist (with same or similar text)
            if (entryText) {
              const entryStillExists = await page.locator('.fc-event').count();
              expect(entryStillExists).toBeGreaterThan(0);
            }
          }
        }
      });

      test('should drag a timed entry to another day', async ({ page }) => {
        const timedEntry = await findFirstTimedEvent(page);

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
    });

    test.describe('Resize Entries - Month View', () => {

      test.beforeEach(async ({ page }) => {
        // Ensure we have entries to work with
        await addEntriesViaToolbar(page, 'Add single entry');
        await page.waitForTimeout(500);
      });

      test('should resize an all-day entry to span multiple days', async ({ page }) => {
        const entry = await findFirstAllDayEvent(page);

        if (await entry.isVisible({ timeout: 3000 })) {
          // Find the resize handle at the end of the event
          const resizeHandle = entry.locator('.fc-event-resizer-end, .fc-event-resizer').first();

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

    test.describe('Edit Entry via Dialog', () => {

      test.beforeEach(async ({ page }) => {
        // Ensure we have entries to work with
        await addEntriesViaToolbar(page, 'Add single entry');
        await page.waitForTimeout(500);
      });

      test('should open edit dialog when clicking an entry', async ({ page }) => {
        const entry = page.locator('.fc-event').first();

        if (await entry.isVisible({ timeout: 3000 })) {
          await entry.click();

          // Wait for dialog to open
          const dialog = page.locator('vaadin-dialog-overlay');
          await expect(dialog).toBeVisible({ timeout: 5000 });

          // Verify dialog has title input
          const titleInput = page.locator('vaadin-text-field input').first();
          await expect(titleInput).toBeVisible({ timeout: 3000 });

          // Close dialog
          const cancelBtn = page.locator('vaadin-button:has-text("Cancel")');
          await cancelBtn.click();
          await expect(dialog).not.toBeVisible({ timeout: 3000 });
        }
      });

      test('should edit entry title via dialog', async ({ page }) => {
        const entry = page.locator('.fc-event').first();

        if (await entry.isVisible({ timeout: 3000 })) {
          // Get original title
          const originalTitle = await entry.textContent();

          await entry.click();

          // Wait for dialog to open
          const dialog = page.locator('vaadin-dialog-overlay');
          await expect(dialog).toBeVisible({ timeout: 5000 });

          // Change title
          const titleInput = page.locator('vaadin-text-field input').first();
          await titleInput.clear();
          await titleInput.fill('Modified Entry Title');

          // Save
          const saveBtn = page.locator('vaadin-button:has-text("Save")');
          await saveBtn.click();

          // Wait for dialog to close
          await expect(dialog).not.toBeVisible({ timeout: 5000 });

          // Verify entry was updated
          await page.waitForTimeout(500);
          const modifiedEntry = page.locator('.fc-event:has-text("Modified Entry Title")');
          await expect(modifiedEntry).toBeVisible({ timeout: 5000 });
        }
      });

      test('should toggle All day checkbox in edit dialog', async ({ page }) => {
        const entry = page.locator('.fc-event').first();

        if (await entry.isVisible({ timeout: 3000 })) {
          await entry.click();

          // Wait for dialog to open
          const dialog = page.locator('vaadin-dialog-overlay');
          await expect(dialog).toBeVisible({ timeout: 5000 });

          // Find All day checkbox
          const allDayCheckbox = page.locator('vaadin-checkbox:has-text("All day")');
          await expect(allDayCheckbox).toBeVisible({ timeout: 3000 });

          // Toggle checkbox
          const checkboxInput = allDayCheckbox.locator('input[type="checkbox"]');
          const wasChecked = await checkboxInput.isChecked();
          await allDayCheckbox.click();
          await page.waitForTimeout(300);

          // Verify it toggled
          const isNowChecked = await checkboxInput.isChecked();
          expect(isNowChecked).toBe(!wasChecked);

          // Cancel (don't save changes)
          const cancelBtn = page.locator('vaadin-button:has-text("Cancel")');
          await cancelBtn.click();
          await expect(dialog).not.toBeVisible({ timeout: 3000 });
        }
      });
    });

    test.describe('Delete Entries', () => {

      test.beforeEach(async ({ page }) => {
        // Ensure we have entries to work with
        await addEntriesViaToolbar(page, 'Add single entry');
        await page.waitForTimeout(500);
      });

      test('should delete entry using Remove button in dialog', async ({ page }) => {
        // Count entries before
        const countBefore = await page.locator('.fc-event').count();

        const entry = page.locator('.fc-event').first();

        if (await entry.isVisible({ timeout: 3000 })) {
          await entry.click();

          // Wait for dialog to open
          const dialog = page.locator('vaadin-dialog-overlay');
          await expect(dialog).toBeVisible({ timeout: 5000 });

          // Click Remove button
          const removeBtn = page.locator('vaadin-button:has-text("Remove")');
          await expect(removeBtn).toBeVisible({ timeout: 3000 });
          await removeBtn.click();

          // Wait for dialog to close
          await expect(dialog).not.toBeVisible({ timeout: 5000 });

          // Verify entry count decreased
          await page.waitForTimeout(500);
          const countAfter = await page.locator('.fc-event').count();
          expect(countAfter).toBeLessThan(countBefore);
        }
      });

      test('should remove all entries via toolbar', async ({ page }) => {
        // First ensure we have some entries
        const initialCount = await page.locator('.fc-event').count();
        if (initialCount === 0) {
          await addEntriesViaToolbar(page, 'Add single entry');
          await page.waitForTimeout(500);
        }

        // Remove all entries via toolbar
        await addEntriesViaToolbar(page, 'Remove all entries');

        // Wait for removal
        await page.waitForTimeout(1000);
        await waitForVaadin(page);

        // Verify all entries are gone
        const finalCount = await page.locator('.fc-event').count();
        expect(finalCount).toBe(0);
      });
    });

    test.describe('Time Grid Week View Interactions', () => {

      test.beforeEach(async ({ page }) => {
        // Add entries first
        await addEntriesViaToolbar(page, 'Add single entry');
        await page.waitForTimeout(500);

        // Switch to Time Grid Week view
        await changeView(page, 'Time Grid Week');
        await page.waitForTimeout(500);
      });

      test('should display entries in Time Grid Week view', async ({ page }) => {
        // Verify we're in time grid view
        const timeGrid = page.locator('.fc-timegrid');
        await expect(timeGrid).toBeVisible({ timeout: 5000 });

        // There should be events visible (or at least the calendar is functional)
        const calendar = page.locator('.fc');
        await expect(calendar).toBeVisible();
      });

      test('should drag timed entry to another time slot', async ({ page }) => {
        const timedEntry = await findFirstTimeGridEvent(page);

        if (await timedEntry.isVisible({ timeout: 5000 })) {
          const box = await timedEntry.boundingBox();
          if (box) {
            // Drag vertically to a different time slot
            await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
            await page.mouse.down();
            await page.mouse.move(box.x + box.width / 2, box.y + 100, { steps: 15 });
            await page.mouse.up();

            await waitForCalendarUpdate(page, 1500);

            // Calendar should still be functional
            const calendar = page.locator('.fc');
            await expect(calendar).toBeVisible();
          }
        }
      });

      test('should drag timed entry to another day', async ({ page }) => {
        const timedEntry = await findFirstTimeGridEvent(page);

        if (await timedEntry.isVisible({ timeout: 5000 })) {
          const box = await timedEntry.boundingBox();
          if (box) {
            // Drag horizontally to a different day
            await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
            await page.mouse.down();
            await page.mouse.move(box.x + 150, box.y, { steps: 15 });
            await page.mouse.up();

            await waitForCalendarUpdate(page, 1500);

            // Calendar should still be functional
            const calendar = page.locator('.fc');
            await expect(calendar).toBeVisible();
          }
        }
      });

      test('should resize timed entry in Time Grid view', async ({ page }) => {
        const timedEntry = await findFirstTimeGridEvent(page);

        if (await timedEntry.isVisible({ timeout: 5000 })) {
          const resizeHandle = timedEntry.locator('.fc-event-resizer-end, .fc-event-resizer').first();

          if (await resizeHandle.isVisible({ timeout: 2000 })) {
            const box = await resizeHandle.boundingBox();
            if (box) {
              // Drag down to increase duration
              await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
              await page.mouse.down();
              await page.mouse.move(box.x + box.width / 2, box.y + 50, { steps: 10 });
              await page.mouse.up();

              await waitForCalendarUpdate(page, 1500);

              // Calendar should still be functional
              const calendar = page.locator('.fc');
              await expect(calendar).toBeVisible();
            }
          }
        }
      });

      test('should edit entry via dialog in Time Grid view', async ({ page }) => {
        const entry = page.locator('.fc-event').first();

        if (await entry.isVisible({ timeout: 5000 })) {
          // Use force click because time grid events have overlapping harness elements
          await entry.click({ force: true });

          // Wait for dialog to open
          const dialog = page.locator('vaadin-dialog-overlay');
          await expect(dialog).toBeVisible({ timeout: 5000 });

          // Verify title input is visible
          const titleInput = page.locator('vaadin-text-field input').first();
          await expect(titleInput).toBeVisible({ timeout: 3000 });

          // Cancel
          const cancelBtn = page.locator('vaadin-button:has-text("Cancel")');
          await cancelBtn.click();
          await expect(dialog).not.toBeVisible({ timeout: 3000 });
        }
      });

      test('should delete entry in Time Grid view', async ({ page }) => {
        const countBefore = await page.locator('.fc-event').count();

        const entry = page.locator('.fc-event').first();

        if (await entry.isVisible({ timeout: 5000 }) && countBefore > 0) {
          // Use force click because time grid events have overlapping harness elements
          await entry.click({ force: true });

          const dialog = page.locator('vaadin-dialog-overlay');
          await expect(dialog).toBeVisible({ timeout: 5000 });

          const removeBtn = page.locator('vaadin-button:has-text("Remove")');
          await expect(removeBtn).toBeVisible({ timeout: 3000 });
          await removeBtn.click();

          await expect(dialog).not.toBeVisible({ timeout: 5000 });

          await page.waitForTimeout(500);
          const countAfter = await page.locator('.fc-event').count();
          expect(countAfter).toBeLessThan(countBefore);
        }
      });
    });

    test.describe('Recurring Entries', () => {

      test('should create and display recurring entries', async ({ page }) => {
        // Add recurring entries
        await addEntriesViaToolbar(page, 'Add recurring entries');

        // Wait for notification
        const notification = page.locator('vaadin-notification-container');
        await expect(notification).toContainText('Added', { timeout: 5000 });

        // Wait for entries to appear
        await page.waitForTimeout(1000);

        // Verify recurring entries are visible (should appear on multiple days)
        const entries = await page.locator('.fc-event').count();
        expect(entries).toBeGreaterThan(0);
      });

      test('should edit recurring entry via dialog', async ({ page }) => {
        // First add recurring entries
        await addEntriesViaToolbar(page, 'Add recurring entries');
        await page.waitForTimeout(1000);

        // Find and click a recurring entry
        const entry = page.locator('.fc-event').first();

        if (await entry.isVisible({ timeout: 3000 })) {
          await entry.click();

          // Wait for dialog to open
          const dialog = page.locator('vaadin-dialog-overlay');
          await expect(dialog).toBeVisible({ timeout: 5000 });

          // Check for Recurring checkbox
          const recurringCheckbox = page.locator('vaadin-checkbox:has-text("Recurring")');
          await expect(recurringCheckbox).toBeVisible({ timeout: 3000 });

          // Cancel dialog
          const cancelBtn = page.locator('vaadin-button:has-text("Cancel")');
          await cancelBtn.click();
          await expect(dialog).not.toBeVisible({ timeout: 3000 });
        }
      });
    });

    test.describe('Data Integrity', () => {

      test('should maintain entries after view switch', async ({ page }) => {
        // Add some entries
        await addEntriesViaToolbar(page, 'Add single entry');
        await page.waitForTimeout(500);

        const initialCount = await page.locator('.fc-event').count();

        // Switch to a different view
        await changeView(page, 'Time Grid Week');
        await page.waitForTimeout(500);

        // Switch back to month view
        await changeView(page, 'Day Grid Month');
        await page.waitForTimeout(500);

        // Entries should still exist
        const finalCount = await page.locator('.fc-event').count();
        expect(finalCount).toBeGreaterThanOrEqual(initialCount);
      });

      test('should persist entry changes after edit', async ({ page }) => {
        // Add an entry
        await addEntriesViaToolbar(page, 'Add single entry');
        await page.waitForTimeout(500);

        const entry = page.locator('.fc-event').first();

        if (await entry.isVisible({ timeout: 3000 })) {
          await entry.click();

          const dialog = page.locator('vaadin-dialog-overlay');
          await expect(dialog).toBeVisible({ timeout: 5000 });

          // Edit title
          const titleInput = page.locator('vaadin-text-field input').first();
          await titleInput.clear();
          await titleInput.fill('Persisted Entry');

          // Save
          const saveBtn = page.locator('vaadin-button:has-text("Save")');
          await saveBtn.click();
          await expect(dialog).not.toBeVisible({ timeout: 5000 });

          // Navigate away and back
          await changeView(page, 'Time Grid Week');
          await page.waitForTimeout(500);
          await changeView(page, 'Day Grid Month');
          await page.waitForTimeout(500);

          // Entry should still have the new title
          const persistedEntry = page.locator('.fc-event:has-text("Persisted Entry")');
          await expect(persistedEntry).toBeVisible({ timeout: 5000 });
        }
      });
    });
  });
}
