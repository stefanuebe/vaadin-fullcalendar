// @ts-check
const { test: base } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

async function gotoConstraintView(page) {
    await page.goto('/test/constraint-overlap');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await page.waitForSelector('.fc-timegrid-slot', { timeout: 5000 });
    await page.waitForSelector('.fc-event', { timeout: 5000 });
    await waitForVaadin(page);
}

base.describe('Per-Entry Editable Flags', () => {

    base.beforeEach(async ({ page }) => {
        await gotoConstraintView(page);
    });

    base('locked entry (editable=false) is not draggable — no fc-event-draggable class', async ({ page }) => {
        const locked = page.locator('.fc-event:has-text("Locked Entry")').first();
        await expect(locked).toBeVisible();
        // FC adds fc-event-draggable class only to draggable entries
        const classes = await locked.getAttribute('class');
        expect(classes).not.toContain('fc-event-draggable');
    });

    base('normal entry has fc-event-draggable class', async ({ page }) => {
        const normal = page.locator('.fc-event:has-text("Normal Entry")').first();
        await expect(normal).toBeVisible();
        const classes = await normal.getAttribute('class');
        expect(classes).toContain('fc-event-draggable');
    });

    base('startEditable=false entry has no fc-event-draggable class', async ({ page }) => {
        const noStart = page.locator('.fc-event:has-text("No Start Edit")').first();
        await expect(noStart).toBeVisible();
        const classes = await noStart.getAttribute('class');
        expect(classes).not.toContain('fc-event-draggable');
    });

    base('startEditable=false entry still has resize handle', async ({ page }) => {
        const noStart = page.locator('.fc-event:has-text("No Start Edit")').first();
        await noStart.hover();
        const resizer = noStart.locator('.fc-event-resizer');
        const count = await resizer.count();
        expect(count).toBeGreaterThanOrEqual(1);
    });
});

base.describe('Overlap Prevention', () => {

    base.beforeEach(async ({ page }) => {
        await gotoConstraintView(page);
    });

    base('entry with overlap=false exists and is visible', async ({ page }) => {
        const noOverlap = page.locator('.fc-event:has-text("No Overlap")').first();
        await expect(noOverlap).toBeVisible();
    });

    base('blocker entry exists on same day', async ({ page }) => {
        const blocker = page.locator('.fc-event:has-text("Blocker")').first();
        await expect(blocker).toBeVisible();
    });
});

base.describe('Constraint (businessHours)', () => {

    base.beforeEach(async ({ page }) => {
        await gotoConstraintView(page);
    });

    base('constrained entry is visible', async ({ page }) => {
        const entry = page.locator('.fc-event:has-text("BH Constrained")').first();
        await expect(entry).toBeVisible();
    });

    base('non-business hour slots have fc-non-business class', async ({ page }) => {
        // Business hours default Mon-Fri 9am-5pm, view is timeGridWeek
        const nonBiz = page.locator('.fc-non-business');
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
        const allDayHeader = page.locator('.fc-col-header-cell[data-date]').first();
        const dateStr = await allDayHeader.getAttribute('data-date');
        expect(dateStr).toBeTruthy();
        // The date should be >= 2025-03-01 (ISO-8601 dates compare correctly lexicographically)
        const date = new Date(dateStr);
        expect(date.getTime()).toBeGreaterThanOrEqual(new Date('2025-03-01').getTime());
    });
});
