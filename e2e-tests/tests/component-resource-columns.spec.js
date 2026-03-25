// @ts-check
const { test: base, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

async function gotoTestView(page) {
    await page.goto('/test/component-resource-columns');
    await page.waitForSelector('.fc', { timeout: 15000 });
    await page.waitForSelector('.fc-timeline', { timeout: 15000 });
    await waitForVaadin(page);
}

base.describe('Component Resource Columns', () => {
    base.beforeEach(async ({ page }) => {
        await gotoTestView(page);
    });

    base.describe('Rendering', () => {
        base('DatePicker renders in resource cell', async ({ page }) => {
            const picker = page.locator('[data-testid="datepicker-res-a"]');
            await expect(picker).toBeVisible();
        });

        base('TextField renders in resource cell', async ({ page }) => {
            const field = page.locator('[data-testid="textfield-res-a"]');
            await expect(field).toBeVisible();
        });

        base('Multiple resources have independent components', async ({ page }) => {
            await expect(page.locator('[data-testid="datepicker-res-a"]')).toBeVisible();
            await expect(page.locator('[data-testid="datepicker-res-b"]')).toBeVisible();
        });

        base('Child resource has components', async ({ page }) => {
            await expect(page.locator('[data-testid="datepicker-res-child"]')).toBeVisible();
        });
    });

    base.describe('Interactivity', () => {
        base('TextField accepts input', async ({ page }) => {
            const field = page.locator('[data-testid="textfield-res-a"]').locator('input');
            await field.fill('Hello');
            await waitForVaadin(page);

            // Read state via button
            await page.click('#btn-read-state');
            await waitForVaadin(page);
            const state = await page.locator('#component-state').textContent();
            expect(state).toContain('notes[res-a]=Hello');
        });

        base('DatePicker accepts date', async ({ page }) => {
            // Set value programmatically — Vaadin 24 DatePicker input handling differs from V25
            await page.evaluate(() => {
                const picker = document.querySelector('[data-testid="datepicker-res-a"]');
                picker.value = '2025-03-03';
                picker.dispatchEvent(new Event('change', { bubbles: true }));
            });
            await waitForVaadin(page);

            await page.click('#btn-read-state');
            await waitForVaadin(page);
            const state = await page.locator('#component-state').textContent();
            expect(state).toContain('date[res-a]=2025-03-03');
        });
    });

    base.describe('View change preserves state', () => {
        base('component value survives view change', async ({ page }) => {
            // Set a value
            const field = page.locator('[data-testid="textfield-res-a"]').locator('input');
            await field.fill('Persist me');
            await waitForVaadin(page);

            // Change view via JS API (no toolbar buttons configured)
            await page.evaluate(() => {
                document.querySelector('vaadin-full-calendar-scheduler').calendar.changeView('resourceTimelineDay');
            });
            await waitForVaadin(page);
            await page.waitForTimeout(500);

            await page.evaluate(() => {
                document.querySelector('vaadin-full-calendar-scheduler').calendar.changeView('resourceTimelineWeek');
            });
            await waitForVaadin(page);
            await page.waitForTimeout(500);

            // Read state
            await page.click('#btn-read-state');
            await waitForVaadin(page);
            const state = await page.locator('#component-state').textContent();
            expect(state).toContain('notes[res-a]=Persist me');
        });
    });

    base.describe('Resource add/remove', () => {
        base('adding resource creates component', async ({ page }) => {
            await page.click('#btn-add-resource');
            await waitForVaadin(page);
            await page.waitForTimeout(500);

            await expect(page.locator('[data-testid="datepicker-res-new"]')).toBeVisible();
            await expect(page.locator('[data-testid="textfield-res-new"]')).toBeVisible();
        });

        base('removing resource removes component', async ({ page }) => {
            await expect(page.locator('[data-testid="datepicker-res-b"]')).toBeVisible();

            await page.click('#btn-remove-resource');
            await waitForVaadin(page);
            await page.waitForTimeout(500);

            await expect(page.locator('[data-testid="datepicker-res-b"]')).not.toBeVisible();
        });
    });

    base.describe('Detach / Reattach', () => {
        base('component state preserved after detach/reattach', async ({ page }) => {
            // Set value
            const field = page.locator('[data-testid="textfield-res-a"]').locator('input');
            await field.fill('Remember me');
            await waitForVaadin(page);

            // Detach
            await page.click('#btn-detach');
            await waitForVaadin(page);
            await page.waitForTimeout(500);

            // Verify calendar is gone
            await expect(page.locator('[data-testid="calendar"]')).not.toBeVisible();

            // Reattach
            await page.click('#btn-reattach');
            await waitForVaadin(page);
            await page.waitForTimeout(1000);

            // Verify calendar is back
            await expect(page.locator('[data-testid="calendar"]')).toBeVisible();

            // Read state — value should be preserved
            await page.click('#btn-read-state');
            await waitForVaadin(page);
            const state = await page.locator('#component-state').textContent();
            expect(state).toContain('notes[res-a]=Remember me');
        });
    });

    base.describe('Keyboard events', () => {
        base('Enter in TextField does not trigger FC navigation', async ({ page }) => {
            const field = page.locator('[data-testid="textfield-res-a"]').locator('input');
            await field.click();
            await field.fill('test');
            await field.press('Enter');
            await waitForVaadin(page);

            // Calendar should still be in the same view (timeline visible)
            await expect(page.locator('.fc-timeline')).toBeVisible();

            // Value should be preserved
            await page.click('#btn-read-state');
            await waitForVaadin(page);
            const state = await page.locator('#component-state').textContent();
            expect(state).toContain('notes[res-a]=test');
        });
    });
});
