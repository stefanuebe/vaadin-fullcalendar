// @ts-check
const { test: base } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

async function gotoExternalDropView(page) {
    await page.goto('/test/external-drop');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 5000 });
    await waitForVaadin(page);
}

base.describe('External Drop — Draggable API', () => {

    base.beforeEach(async ({ page }) => {
        await gotoExternalDropView(page);
    });

    base('external draggable element is visible', async ({ page }) => {
        const el = page.locator('#external-draggable');
        await expect(el).toBeVisible();
        await expect(el).toHaveText('Drag me to calendar');
    });

    base('calendar is droppable', async ({ page }) => {
        await expect(page.locator('.fc-dayGridMonth-view')).toBeVisible();
    });

    base('dragging external element onto calendar fires DropEvent with component and entry', async ({ page }) => {
        await expect(page.locator('#drop-count')).toHaveText('0');

        const dragEl = page.locator('#external-draggable');
        const targetCell = page.locator('.fc-daygrid-day[data-date="2025-03-12"] .fc-daygrid-day-frame');

        const srcBox = await dragEl.boundingBox();
        const tgtBox = await targetCell.boundingBox();
        if (!srcBox || !tgtBox) throw new Error('Could not get bounding boxes');

        // Drag from external element to calendar cell
        await page.mouse.move(srcBox.x + srcBox.width / 2, srcBox.y + srcBox.height / 2);
        await page.mouse.down();
        await page.mouse.move(
            tgtBox.x + tgtBox.width / 2,
            tgtBox.y + tgtBox.height / 2,
            { steps: 15 }
        );
        await page.mouse.up();
        await waitForVaadin(page);

        // DropEvent should have fired
        await expect(page.locator('#drop-count')).not.toHaveText('0', { timeout: 5000 });
        await expect(page.locator('#drop-date')).toHaveText('2025-03-12', { timeout: 5000 });

        // Verify typed access via Draggable API
        await expect(page.locator('#drop-component')).toHaveText('external-draggable', { timeout: 5000 });
        await expect(page.locator('#drop-entry')).toHaveText('External Task', { timeout: 5000 });
    });
});
