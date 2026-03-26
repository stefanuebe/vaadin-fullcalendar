// @ts-check
const { test } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

async function gotoSignalDropResizeView(page) {
    await page.goto('/test/signal-drop-resize');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await page.waitForSelector('.fc-timegrid-slot', { timeout: 5000 });
    await waitForVaadin(page);
}

async function dragEntryToNextDay(page) {
    const entry = page.locator('.fc-event:has-text("Drag Me")').first();
    await expect(entry).toBeVisible({ timeout: 10000 });
    const box = await entry.boundingBox();
    if (!box) throw new Error('Could not get bounding box');

    const startX = box.x + box.width / 2;
    const startY = box.y + box.height / 2;

    await page.mouse.move(startX, startY);
    await page.mouse.down();
    await page.mouse.move(startX + 120, startY, { steps: 10 });
    await page.mouse.up();
    await waitForVaadin(page);
}

test.describe('Signal Drop/Resize', () => {

    test.beforeEach(async ({ page }) => {
        await gotoSignalDropResizeView(page);
    });

    test('drop with signal binding: rejected → entry reverts', async ({ page }) => {
        // apply toggle is false by default
        await expect(page.locator('#apply-changes')).toHaveText('false');

        const originalBox = await page.locator('.fc-event:has-text("Drag Me")').first().boundingBox();
        const originalX = originalBox.x;

        await dragEntryToNextDay(page);
        await page.waitForTimeout(500);

        await expect(page.locator('#drop-status')).toHaveText('rejected', { timeout: 5000 });
        // Server-side start unchanged
        await expect(page.locator('#server-start')).toHaveText('2025-03-03T09:00', { timeout: 5000 });

        // Entry should revert to original position
        const revertedBox = await page.locator('.fc-event:has-text("Drag Me")').first().boundingBox();
        expect(Math.abs(revertedBox.x - originalX)).toBeLessThan(5);
    });

    test('drop with signal binding: applied via signal.modify() → entry stays', async ({ page }) => {
        // Toggle apply to true
        await page.locator('#toggle-apply-btn').click();
        await expect(page.locator('#apply-changes')).toHaveText('true');

        const originalBox = await page.locator('.fc-event:has-text("Drag Me")').first().boundingBox();
        const originalX = originalBox.x;

        await dragEntryToNextDay(page);
        await page.waitForTimeout(500);

        await expect(page.locator('#drop-status')).toHaveText('applied', { timeout: 5000 });
        // Server-side start should have changed
        const serverStart = await page.locator('#server-start').textContent();
        expect(serverStart).not.toBe('2025-03-03T09:00');

        // Entry should stay at new position
        const newBox = await page.locator('.fc-event:has-text("Drag Me")').first().boundingBox();
        expect(Math.abs(newBox.x - originalX)).toBeGreaterThan(20);
    });
});
