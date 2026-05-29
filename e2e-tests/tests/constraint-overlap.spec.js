// @ts-check
const { test: base } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

async function gotoConstraintView(page) {
    await page.goto('/test/constraint-overlap');
    await page.waitForSelector('.vfc-view', { timeout: 15000 });
    await page.waitForFunction(
        () => document.querySelectorAll('.vfc-event').length > 0,
        { timeout: 20000 }
    );
    await waitForVaadin(page);
}

base.describe('Per-Entry Editable Flags', () => {

    base.beforeEach(async ({ page }) => {
        await gotoConstraintView(page);
    });

    base('locked entry (editable=false) is not draggable — no fc-DD class', async ({ page }) => {
        const locked = page.locator('.vfc-event:has-text("Locked Entry")').first();
        await expect(locked).toBeVisible();
        // v7: FC uses fc-DD (internalEventDraggable) class for draggable entries
        const classes = await locked.getAttribute('class');
        expect(classes).not.toContain('fc-DD');
    });

    base('normal entry has fc-DD draggable class', async ({ page }) => {
        const normal = page.locator('.vfc-event:has-text("Normal Entry")').first();
        await expect(normal).toBeVisible();
        const classes = await normal.getAttribute('class');
        expect(classes).toContain('fc-DD');
    });

    base('startEditable=false entry has no fc-DD draggable class', async ({ page }) => {
        const noStart = page.locator('.vfc-event:has-text("No Start Edit")').first();
        await expect(noStart).toBeVisible();
        const classes = await noStart.getAttribute('class');
        expect(classes).not.toContain('fc-DD');
    });

    base('startEditable=false entry still has resize handle', async ({ page }) => {
        const noStart = page.locator('.vfc-event:has-text("No Start Edit")').first();
        await noStart.hover();
        // v7: resize handles use fc-aL (internalEventResizer) class
        const resizer = noStart.locator('.fc-aL');
        const count = await resizer.count();
        expect(count).toBeGreaterThanOrEqual(1);
    });
});

base.describe('Overlap Prevention', () => {

    base.beforeEach(async ({ page }) => {
        await gotoConstraintView(page);
    });

    base('entry with overlap=false exists and is visible', async ({ page }) => {
        const noOverlap = page.locator('.vfc-event:has-text("No Overlap")').first();
        await expect(noOverlap).toBeVisible();
    });

    base('blocker entry exists on same day', async ({ page }) => {
        const blocker = page.locator('.vfc-event:has-text("Blocker")').first();
        await expect(blocker).toBeVisible();
    });
});

base.describe('Constraint (businessHours)', () => {

    base.beforeEach(async ({ page }) => {
        await gotoConstraintView(page);
    });

    base('constrained entry is visible', async ({ page }) => {
        const entry = page.locator('.vfc-event:has-text("BH Constrained")').first();
        await expect(entry).toBeVisible();
    });

    base('non-business hour slots have fc-non-business class', async ({ page }) => {
        // Business hours default Mon-Fri 9am-5pm, view is timeGridWeek
        const nonBiz = page.locator('.vfc-non-business');
        const count = await nonBiz.count();
        expect(count).toBeGreaterThanOrEqual(1);
    });
});

base.describe('ValidRange', () => {

    base.beforeEach(async ({ page }) => {
        await gotoConstraintView(page);
    });

    base('dates outside valid range are disabled', async ({ page }) => {
        // The calendar is in timeGridWeek starting 2025-03-03
        // validRange is 2025-03-01 to 2025-04-30
        // Try to navigate far back via JS — FC should prevent going before March 2025
        await page.evaluate(() => {
            const fcEl = document.querySelector('vaadin-full-calendar');
            if (fcEl && fcEl.calendar) {
                // Try to navigate to February 2025 — outside validRange
                fcEl.calendar.gotoDate('2025-02-01');
            }
        });
        await waitForVaadin(page);
        await page.waitForTimeout(500);

        // FC should NOT show February — it should stay within valid range
        // Check that the view still shows dates in March or later
        const allDayHeader = page.locator('.vfc-day-header[data-date]').first();
        const dateStr = await allDayHeader.getAttribute('data-date');
        expect(dateStr).toBeTruthy();
        // The date should be >= 2025-03-01 (ISO-8601 dates compare correctly lexicographically)
        const date = new Date(dateStr);
        expect(date.getTime()).toBeGreaterThanOrEqual(new Date('2025-03-01').getTime());
    });
});
