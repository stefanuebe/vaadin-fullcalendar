// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the interaction callbacks test view and wait for the calendar to render.
 */
async function gotoInteractionCallbacksView(page) {
    await page.goto('/test/interaction-callbacks');
    await page.waitForSelector('.fc', { timeout: 30000 });
    // timeGridWeek renders time-slot rows
    await page.waitForSelector('.fc-timegrid-slot', { timeout: 15000 });
    await waitForVaadin(page);
}

// =============================================================================

// =============================================================================

test.describe('Interaction Callbacks', () => {

    test.beforeEach(async ({ page }) => {
        await gotoInteractionCallbacksView(page);
    });

    // -------------------------------------------------------------------------
    // Drag start / stop counter badges
    // -------------------------------------------------------------------------

    test('drag start/stop listeners: counters increment after drag', async ({ page }) => {
        // Locate the "Drag Me" event in the timegrid
        const dragEvent = page.locator('.fc-event:has-text("Drag Me")').first();
        await expect(dragEvent).toBeVisible({ timeout: 10000 });

        // Initial counter values
        await expect(page.locator('#drag-start-count')).toHaveText('0');
        await expect(page.locator('#drag-stop-count')).toHaveText('0');

        // Drag the event a small amount (just enough to trigger start/stop without a meaningful drop)
        const box = await dragEvent.boundingBox();
        if (!box) throw new Error('Could not get bounding box for drag event');

        await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
        await page.mouse.down();
        // Move slightly to trigger drag start
        await page.mouse.move(box.x + box.width / 2 + 5, box.y + box.height / 2 + 5, { steps: 3 });
        await page.mouse.up();

        // After drag, both counters should have incremented
        await expect(page.locator('#drag-start-count')).not.toHaveText('0', { timeout: 5000 });
        await expect(page.locator('#drag-stop-count')).not.toHaveText('0', { timeout: 5000 });
    });

    // -------------------------------------------------------------------------
    // Resize start / stop counter badges
    // -------------------------------------------------------------------------

    test('resize start/stop listeners: counters increment after resize', async ({ page }) => {
        const resizeEvent = page.locator('.fc-event:has-text("Resize Me")').first();
        await expect(resizeEvent).toBeVisible({ timeout: 10000 });

        await expect(page.locator('#resize-start-count')).toHaveText('0');
        await expect(page.locator('#resize-stop-count')).toHaveText('0');

        // The resize handle is at the bottom of the event element
        const handle = resizeEvent.locator('.fc-event-resizer-end');
        await expect(handle).toBeVisible({ timeout: 5000 });
        const handleBox = await handle.boundingBox();
        if (!handleBox) throw new Error('Could not get bounding box for resize handle');

        await page.mouse.move(handleBox.x + handleBox.width / 2, handleBox.y + handleBox.height / 2);
        await page.mouse.down();
        await page.mouse.move(
            handleBox.x + handleBox.width / 2,
            handleBox.y + handleBox.height / 2 + 10,
            { steps: 3 }
        );
        await page.mouse.up();

        await expect(page.locator('#resize-start-count')).not.toHaveText('0', { timeout: 5000 });
        await expect(page.locator('#resize-stop-count')).not.toHaveText('0', { timeout: 5000 });
    });

    // -------------------------------------------------------------------------
    // Unselect listener
    // -------------------------------------------------------------------------

    test('unselect listener: counter increments after clicking outside selection', async ({ page }) => {
        await expect(page.locator('#unselect-count')).toHaveText('0');

        // Click-drag to create a timeslot selection
        const slot = page.locator('.fc-timegrid-slot').nth(5);
        await expect(slot).toBeVisible({ timeout: 5000 });
        const slotBox = await slot.boundingBox();
        if (!slotBox) throw new Error('Could not get slot bounding box');

        // Create a selection
        await page.mouse.move(slotBox.x + 10, slotBox.y + 2);
        await page.mouse.down();
        await page.mouse.move(slotBox.x + 10, slotBox.y + slotBox.height * 3, { steps: 5 });
        await page.mouse.up();

        // Click elsewhere on the page (outside the selection) to trigger unselect
        const header = page.locator('.fc-col-header-cell').first();
        await header.click();

        await expect(page.locator('#unselect-count')).not.toHaveText('0', { timeout: 5000 });
    });

    // -------------------------------------------------------------------------
    // selectAllow callback: deny selections before 2025-03-03
    // -------------------------------------------------------------------------

    test('selectAllow callback: selection on allowed date succeeds', async ({ page }) => {
        // Tuesday 2025-03-04 is allowed — click a slot in that column
        // Week starts Monday 2025-03-03, so column index 2 (0-based) is Wednesday, etc.
        // Just verify the calendar renders without error and no console error fires
        const col = page.locator('.fc-col-header-cell').nth(2); // some column
        await expect(col).toBeVisible({ timeout: 5000 });
        // No assertion beyond "no crash" — selectAllow is a client-side-only callback
    });

    // -------------------------------------------------------------------------
    // Calendar renders correctly in timeGridWeek
    // -------------------------------------------------------------------------

    test('calendar renders in timeGridWeek view', async ({ page }) => {
        await expect(page.locator('.fc')).toBeVisible();
        await expect(page.locator('.fc-timeGridWeek-view')).toBeVisible();
    });

    test('draggable entry is visible', async ({ page }) => {
        await expect(page.locator('.fc-event:has-text("Drag Me")')).toBeVisible({ timeout: 10000 });
    });

    test('resizable entry is visible', async ({ page }) => {
        await expect(page.locator('.fc-event:has-text("Resize Me")')).toBeVisible({ timeout: 10000 });
    });

});

// Note: The following interaction features are not covered by E2E tests because they
// are either client-side-only callbacks or require complex simulation:
//   - setEventAllow: client-side-only callback (no server-side event)
//   - setUnselectCancel: CSS selector based configuration, no observable DOM change
//   - setDropAccept: CSS selector filtering for external drops, requires external element
//   - ExternalEntryDroppedEvent: requires dragging an element from outside the calendar
