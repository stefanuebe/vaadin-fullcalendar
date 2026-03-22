// @ts-check
const { test: base } = require('@playwright/test');
const { expect, waitForVaadin } = require('./fixtures');

/**
 * Navigate to the detach/reattach test view.
 */
async function gotoDetachView(page) {
    await page.goto('/test/detach-reattach');
    await page.waitForSelector('.fc', { timeout: 10000 });
    await page.waitForSelector('.fc-dayGridMonth-view', { timeout: 5000 });
    await page.waitForSelector('.fc-event', { timeout: 5000 });
    await waitForVaadin(page);
}

/**
 * Click a Vaadin button by its ID.
 */
async function clickButton(page, id) {
    const btn = page.locator(`#${id}`);
    await expect(btn).toBeVisible({ timeout: 5000 });
    await btn.click();
    await waitForVaadin(page);
    await page.waitForTimeout(500);
}

// =============================================================================
// Detach / Reattach
// =============================================================================

base.describe('Detach / Reattach — State Preservation', () => {

    base.beforeEach(async ({ page }) => {
        await gotoDetachView(page);
    });

    base('calendar renders initially with entries', async ({ page }) => {
        await expect(page.locator('.fc-dayGridMonth-view')).toBeVisible();
        await expect(page.locator('.fc-event:has-text("Surviving Entry")')).toBeVisible();
    });

    base('entries survive detach and reattach', async ({ page }) => {
        // Verify entry is visible
        await expect(page.locator('.fc-event:has-text("Surviving Entry")')).toBeVisible();

        // Detach
        await clickButton(page, 'btn-detach');
        // Calendar should be gone from DOM
        await expect(page.locator('#calendar-container .fc')).toHaveCount(0, { timeout: 5000 });

        // Reattach
        await clickButton(page, 'btn-reattach');
        // Calendar should be back
        await page.waitForSelector('.fc', { timeout: 5000 });
        await page.waitForSelector('.fc-event', { timeout: 5000 });
        await waitForVaadin(page);

        // Entry should still be visible
        await expect(page.locator('.fc-event:has-text("Surviving Entry")')).toBeVisible({ timeout: 10000 });
    });

    base('current view is preserved after detach/reattach', async ({ page }) => {
        // Switch to timeGridWeek
        await clickButton(page, 'btn-switch-view');
        await page.waitForSelector('.fc-timeGridWeek-view', { timeout: 10000 });

        // Detach + Reattach
        await clickButton(page, 'btn-detach');
        await clickButton(page, 'btn-reattach');
        await page.waitForSelector('.fc', { timeout: 5000 });
        await waitForVaadin(page);

        // View should still be timeGridWeek
        await expect(page.locator('.fc-timeGridWeek-view')).toBeVisible({ timeout: 10000 });
    });

    base('navigated date is preserved after detach/reattach', async ({ page }) => {
        // Wait for initial DatesRenderedEvent
        await expect(page.locator('#interval-start')).toHaveText(/2025-03/, { timeout: 5000 });

        // Navigate to next month (April 2025)
        await clickButton(page, 'btn-navigate-next');
        await expect(page.locator('#interval-start')).toHaveText('2025-04-01', { timeout: 5000 });

        // Detach + Reattach
        await clickButton(page, 'btn-detach');
        await clickButton(page, 'btn-reattach');
        await page.waitForSelector('.fc', { timeout: 5000 });
        await waitForVaadin(page);

        // Should still show April (not reset to March)
        await expect(page.locator('#interval-start')).toHaveText(/2025-04/, { timeout: 10000 });
    });

    base('options are preserved after detach/reattach (week numbers)', async ({ page }) => {
        // Week numbers should be visible initially
        const weekNums = page.locator('.fc-daygrid-week-number');
        await expect(weekNums.first()).toBeVisible();

        // Detach + Reattach
        await clickButton(page, 'btn-detach');
        await clickButton(page, 'btn-reattach');
        await page.waitForSelector('.fc', { timeout: 5000 });
        await page.waitForSelector('.fc-event', { timeout: 5000 });
        await waitForVaadin(page);

        // Week numbers should still be visible
        await expect(page.locator('.fc-daygrid-week-number').first()).toBeVisible({ timeout: 10000 });
    });

    base('click listener works after detach/reattach', async ({ page }) => {
        // Click entry before detach
        await expect(page.locator('#click-count')).toHaveText('0');
        await page.locator('.fc-event:has-text("Surviving Entry")').first().click();
        await waitForVaadin(page);
        await expect(page.locator('#click-count')).toHaveText('1', { timeout: 5000 });

        // Detach + Reattach
        await clickButton(page, 'btn-detach');
        await clickButton(page, 'btn-reattach');
        await page.waitForSelector('.fc', { timeout: 5000 });
        await page.waitForSelector('.fc-event', { timeout: 5000 });
        await waitForVaadin(page);

        // Click entry again after reattach — listener should still work
        await page.locator('.fc-event:has-text("Surviving Entry")').first().click();
        await waitForVaadin(page);
        await expect(page.locator('#click-count')).toHaveText('2', { timeout: 5000 });
    });

    base('entry color is preserved after detach/reattach', async ({ page }) => {
        // Check green color before detach
        const entry = page.locator('.fc-event:has-text("Surviving Entry")').first();
        const colorBefore = await entry.evaluate(el => {
            const style = window.getComputedStyle(el);
            return style.backgroundColor !== 'rgba(0, 0, 0, 0)' ? style.backgroundColor :
                   window.getComputedStyle(el.querySelector('.fc-event-main') || el).backgroundColor;
        });

        // Detach + Reattach
        await clickButton(page, 'btn-detach');
        await clickButton(page, 'btn-reattach');
        await page.waitForSelector('.fc', { timeout: 5000 });
        await page.waitForSelector('.fc-event', { timeout: 5000 });
        await waitForVaadin(page);

        // Check color after reattach — should match
        const reattachedEntry = page.locator('.fc-event:has-text("Surviving Entry")').first();
        await expect(reattachedEntry).toBeVisible({ timeout: 10000 });
        const colorAfter = await reattachedEntry.evaluate(el => {
            const style = window.getComputedStyle(el);
            return style.backgroundColor !== 'rgba(0, 0, 0, 0)' ? style.backgroundColor :
                   window.getComputedStyle(el.querySelector('.fc-event-main') || el).backgroundColor;
        });
        expect(colorAfter).toBe(colorBefore);
    });
});

// =============================================================================
// setVisible(false) / setVisible(true)
// =============================================================================

base.describe('Visibility Toggle — setVisible(false) / setVisible(true)', () => {

    base.beforeEach(async ({ page }) => {
        await gotoDetachView(page);
    });

    base('calendar is hidden after setVisible(false)', async ({ page }) => {
        await expect(page.locator('.fc')).toBeVisible();

        await clickButton(page, 'btn-hide');

        // The FC element should be hidden (Vaadin sets display:none or similar)
        await expect(page.locator('vaadin-full-calendar')).toBeHidden({ timeout: 5000 });
    });

    base('calendar is visible again after setVisible(true)', async ({ page }) => {
        // Hide
        await clickButton(page, 'btn-hide');
        await expect(page.locator('vaadin-full-calendar')).toBeHidden({ timeout: 5000 });

        // Show
        await clickButton(page, 'btn-show');
        await expect(page.locator('.fc')).toBeVisible({ timeout: 10000 });
    });

    base('entries survive visibility toggle', async ({ page }) => {
        await expect(page.locator('.fc-event:has-text("Surviving Entry")')).toBeVisible();

        // Hide + Show
        await clickButton(page, 'btn-hide');
        await clickButton(page, 'btn-show');
        await expect(page.locator('.fc')).toBeVisible({ timeout: 10000 });
        await waitForVaadin(page);

        // Entry should still be there
        await expect(page.locator('.fc-event:has-text("Surviving Entry")')).toBeVisible({ timeout: 10000 });
    });

    base('click listener works after visibility toggle', async ({ page }) => {
        await expect(page.locator('#click-count')).toHaveText('0');

        // Hide + Show
        await clickButton(page, 'btn-hide');
        await clickButton(page, 'btn-show');
        await expect(page.locator('.fc')).toBeVisible({ timeout: 10000 });
        await waitForVaadin(page);

        // Click entry — listener should still fire
        await page.locator('.fc-event:has-text("Surviving Entry")').first().click();
        await waitForVaadin(page);
        await expect(page.locator('#click-count')).toHaveText('1', { timeout: 5000 });
    });

    base('navigated date survives visibility toggle', async ({ page }) => {
        // Navigate to April
        await clickButton(page, 'btn-navigate-next');
        await expect(page.locator('#interval-start')).toHaveText('2025-04-01', { timeout: 5000 });

        // Hide + Show
        await clickButton(page, 'btn-hide');
        await clickButton(page, 'btn-show');
        await expect(page.locator('.fc')).toBeVisible({ timeout: 10000 });
        await waitForVaadin(page);

        // Should still show April
        // Note: setVisible doesn't trigger detach/reattach, so FC calendar instance stays intact
        // But we verify the rendered state is still correct
        await expect(page.locator('#interval-start')).toHaveText(/2025-04/, { timeout: 5000 });
    });
});
