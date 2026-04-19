// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the auto-revert test view and wait for the calendar to render.
 */
async function gotoAutoRevertView(page) {
    await page.goto('/test/auto-revert');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await page.waitForSelector('.fc-timegrid-slot', { timeout: 5000 });
    await waitForVaadin(page);
}

/**
 * Get the bounding box of the "Drag Me" entry.
 */
async function getDragMeBox(page) {
    const entry = page.locator('.fc-event:has-text("Drag Me")').first();
    await expect(entry).toBeVisible({ timeout: 10000 });
    const box = await entry.boundingBox();
    if (!box) throw new Error('Could not get bounding box for Drag Me entry');
    return box;
}

/**
 * Drag the "Drag Me" entry to the right (next day column) by a fixed pixel offset.
 * Returns the starting box so callers can verify position changes.
 */
async function dragEntryToNextDay(page) {
    const box = await getDragMeBox(page);
    const startX = box.x + box.width / 2;
    const startY = box.y + box.height / 2;

    await page.mouse.move(startX, startY);
    await page.mouse.down();
    // Move ~120px to the right to reach the next day column in timeGridWeek
    await page.mouse.move(startX + 120, startY, { steps: 10 });
    await page.mouse.up();
    await waitForVaadin(page);

    return box;
}

// =============================================================================

test.describe('Auto Revert (#225)', () => {

    test.beforeEach(async ({ page }) => {
        await gotoAutoRevertView(page);
    });

    // -------------------------------------------------------------------------
    // autoRevert=true (default), applyChanges NOT called → entry reverts
    // -------------------------------------------------------------------------

    test('drop rejected: entry reverts to original position', async ({ page }) => {
        // apply toggle is "false" by default — drop will be rejected
        await expect(page.locator('#apply-changes')).toHaveText('false');

        // Record the original position
        const originalBox = await getDragMeBox(page);
        const originalX = originalBox.x;

        // Drag to next day
        await dragEntryToNextDay(page);

        // Wait for revert animation
        await page.waitForTimeout(500);
        await waitForVaadin(page);

        // Verify: status should say "rejected"
        await expect(page.locator('#drop-status')).toHaveText('rejected', { timeout: 5000 });

        // Verify: drop counter incremented
        await expect(page.locator('#drop-count')).toHaveText('1', { timeout: 5000 });

        // Verify: server-side start should be unchanged (still on Monday 2025-03-03 09:00)
        await expect(page.locator('#server-start')).toHaveText('2025-03-03T09:00', { timeout: 5000 });

        // Verify: entry should be back at (approximately) the original X position
        const revertedBox = await getDragMeBox(page);
        // Allow some pixel tolerance (5px) for animation/rendering differences
        expect(Math.abs(revertedBox.x - originalX)).toBeLessThan(5);
    });

    // -------------------------------------------------------------------------
    // autoRevert=true (default), applyChanges called → entry stays
    // -------------------------------------------------------------------------

    test('drop applied: entry stays at new position', async ({ page }) => {
        // Toggle apply to "true"
        await page.locator('#toggle-apply-btn').click();
        await expect(page.locator('#apply-changes')).toHaveText('true');

        // Record the original position
        const originalBox = await getDragMeBox(page);
        const originalX = originalBox.x;

        // Drag to next day
        await dragEntryToNextDay(page);

        // Wait for potential revert (should NOT happen)
        await page.waitForTimeout(500);
        await waitForVaadin(page);

        // Verify: status should say "applied"
        await expect(page.locator('#drop-status')).toHaveText('applied', { timeout: 5000 });

        // Verify: server-side start should have changed (no longer 2025-03-03T09:00)
        const serverStart = await page.locator('#server-start').textContent();
        expect(serverStart).not.toBe('2025-03-03T09:00');

        // Verify: entry should NOT be at the original X position (it moved)
        const newBox = await getDragMeBox(page);
        expect(Math.abs(newBox.x - originalX)).toBeGreaterThan(20);
    });

    // -------------------------------------------------------------------------
    // No flicker: when applied, entry should not visibly jump back then forward
    // -------------------------------------------------------------------------

    test('drop applied: no visible flicker (entry does not jump back)', async ({ page }) => {
        // Toggle apply to "true"
        await page.locator('#toggle-apply-btn').click();
        await expect(page.locator('#apply-changes')).toHaveText('true');

        const originalBox = await getDragMeBox(page);

        // Set up a position tracker — record all position changes during the drop
        await page.evaluate(() => {
            window._positionLog = [];
            const observer = new MutationObserver(() => {
                const entry = document.querySelector('.fc-event');
                if (entry) {
                    const rect = entry.getBoundingClientRect();
                    window._positionLog.push({ x: rect.x, y: rect.y, t: Date.now() });
                }
            });
            observer.observe(document.body, { childList: true, subtree: true, attributes: true });
            window._positionObserver = observer;
        });

        // Drag to next day
        await dragEntryToNextDay(page);
        await page.waitForTimeout(500);

        // Stop the observer and get the log
        const positionLog = await page.evaluate(() => {
            if (window._positionObserver) window._positionObserver.disconnect();
            return window._positionLog || [];
        });

        // If there was a flicker, we'd see the X position go: original → new → original → new
        // With no flicker, it should go: original → new (monotonic change in one direction)
        // We just check that the X position never goes back toward the original after moving away
        if (positionLog.length > 2) {
            const firstX = positionLog[0].x;
            let movedAway = false;
            let cameBack = false;
            for (const pos of positionLog) {
                if (Math.abs(pos.x - firstX) > 20) movedAway = true;
                if (movedAway && Math.abs(pos.x - firstX) < 5) cameBack = true;
            }
            // If the entry came back to the original position after moving away, that's a flicker
            expect(cameBack).toBe(false);
        }
    });
});
