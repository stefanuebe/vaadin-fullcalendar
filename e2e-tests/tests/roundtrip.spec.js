// @ts-check
const { test: base } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the roundtrip test view.
 */
async function gotoRoundtripView(page) {
    await page.goto('/test/roundtrip');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 5000 });
    await page.waitForSelector('.fc-event', { timeout: 5000 });
    await waitForVaadin(page);
}

base.describe('Roundtrip — Server modifies entry on click', () => {

    base.beforeEach(async ({ page }) => {
        await gotoRoundtripView(page);
    });

    base('clicking "Click to Rename" changes entry text to "Renamed!"', async ({ page }) => {
        // Verify entry exists with original title
        const entry = page.locator('.fc-event:has-text("Click to Rename")').first();
        await expect(entry).toBeVisible();

        // Click the entry
        await entry.click();
        await waitForVaadin(page);

        // After click, server changes title to "Renamed!" — verify it appears in DOM
        await expect(page.locator('.fc-event:has-text("Renamed!")')).toBeVisible({ timeout: 5000 });
    });

    base('clicking "Click to Recolor" changes entry to red', async ({ page }) => {
        const entry = page.locator('.fc-event:has-text("Click to Recolor")').first();
        await expect(entry).toBeVisible();

        // Verify initial color is blue — blue channel dominant
        const initialBg = await entry.evaluate(el => {
            const style = window.getComputedStyle(el);
            return style.backgroundColor !== 'rgba(0, 0, 0, 0)' ? style.backgroundColor :
                   window.getComputedStyle(el.querySelector('.fc-event-main') || el).backgroundColor;
        });
        const blueMatch = initialBg.match(/rgb\w?\((\d+),\s*(\d+),\s*(\d+)/);
        expect(blueMatch).not.toBeNull();
        expect(parseInt(blueMatch[3])).toBeGreaterThan(100); // blue channel present

        // Click the entry
        await entry.click();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        // After click, server changes color to red — verify the entry is now red-ish
        const recoloredEntry = page.locator('.fc-event:has-text("Click to Recolor")').first();
        await expect(recoloredEntry).toBeVisible({ timeout: 5000 });
        const newBg = await recoloredEntry.evaluate(el => {
            const style = window.getComputedStyle(el);
            return style.backgroundColor !== 'rgba(0, 0, 0, 0)' ? style.backgroundColor :
                   window.getComputedStyle(el.querySelector('.fc-event-main') || el).backgroundColor;
        });
        const redMatch = newBg.match(/rgb\w?\((\d+),\s*(\d+),\s*(\d+)/);
        expect(redMatch).not.toBeNull();
        // Red must be dominant after recolor
        expect(parseInt(redMatch[1])).toBeGreaterThan(200);
        expect(parseInt(redMatch[2])).toBeLessThan(100);
        expect(parseInt(redMatch[3])).toBeLessThan(100);
    });
});

base.describe('Roundtrip — Drop moves entry to new date', () => {

    base.beforeEach(async ({ page }) => {
        await gotoRoundtripView(page);
    });

    base('dragging "Drop Target" to another date moves it visually', async ({ page }) => {
        // Verify entry is on March 10
        const march10Cell = page.locator('.fc-daygrid-day[data-date="2025-03-10"]');
        await expect(march10Cell.locator('.fc-event:has-text("Drop Target")')).toBeVisible();

        // Get the entry and a target cell
        const entry = page.locator('.fc-event:has-text("Drop Target")').first();
        const targetCell = page.locator('.fc-daygrid-day[data-date="2025-03-12"] .fc-daygrid-day-frame');

        const entryBox = await entry.boundingBox();
        const targetBox = await targetCell.boundingBox();
        if (!entryBox || !targetBox) throw new Error('Could not get bounding boxes');

        // Drag from entry center to target cell center
        await page.mouse.move(entryBox.x + entryBox.width / 2, entryBox.y + entryBox.height / 2);
        await page.mouse.down();
        await page.mouse.move(
            targetBox.x + targetBox.width / 2,
            targetBox.y + targetBox.height / 2,
            { steps: 10 }
        );
        await page.mouse.up();
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        // Entry should now be in March 12 cell
        const march12Cell = page.locator('.fc-daygrid-day[data-date="2025-03-12"]');
        await expect(march12Cell.locator('.fc-event:has-text("Drop Target")')).toBeVisible({ timeout: 5000 });
    });
});

base.describe('Roundtrip — Server removes entry on click', () => {

    base.beforeEach(async ({ page }) => {
        await gotoRoundtripView(page);
    });

    base('clicking "Click to Remove" removes entry from calendar', async ({ page }) => {
        const entry = page.locator('.fc-event:has-text("Click to Remove")').first();
        await expect(entry).toBeVisible();

        await entry.click();
        await waitForVaadin(page);

        // Entry should be gone
        await expect(page.locator('.fc-event:has-text("Click to Remove")')).toHaveCount(0, { timeout: 5000 });
    });
});

base.describe('Roundtrip — Server creates entry on timeslot click', () => {

    base.beforeEach(async ({ page }) => {
        await gotoRoundtripView(page);
    });

    base('clicking empty day creates "Server Created" entry', async ({ page }) => {
        // March 20 should be empty
        await expect(page.locator('.fc-event:has-text("Server Created")')).toHaveCount(0);

        // Click the empty day cell
        const emptyCell = page.locator('.fc-daygrid-day[data-date="2025-03-20"] .fc-daygrid-day-frame');
        await emptyCell.click();
        await waitForVaadin(page);

        // New entry should appear
        await expect(page.locator('.fc-event:has-text("Server Created")')).toBeVisible({ timeout: 5000 });
    });
});
