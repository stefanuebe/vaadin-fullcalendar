// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForVaadin } = require('./fixtures');

/**
 * Removing options of an attached calendar with setOption(option, null) (#294): the calendar behaves as if the
 * option had never been set, and changing an option neither refetches the entries nor re-mounts unaffected ones.
 * View: /test/option-reset, a German week view with an all-day and a timed entry from a callback entry provider. Its
 * initial JSON options set the entry color green, one entry row per day with "day" as the action of the "+N more"
 * link, an eventDataTransform callback, at top level and for the listDay view, that counts its evaluations, and long
 * weekday names in the day headers of the week view. Before attach the server sets 20:00 as the end of the time slots
 * of the week view.
 */

const ALL_DAY_TEXT = 'Ganztägig'; // allDayText of the German locale

async function gotoView(page) {
    await page.goto('/test/option-reset');
    await page.waitForSelector('#cal-option-reset .fc-event', { timeout: 10000 });
    await waitForVaadin(page);
    await expect(page.locator('#fetch-count')).toHaveText('1');
}

async function click(page, name) {
    await page.getByRole('button', { name, exact: true }).click();
    await waitForVaadin(page);
}

const calendarOf = (page) => page.locator('#cal-option-reset');

const timedEntry = (page) => page.locator('#cal-option-reset .fc-event', { hasText: 'Timed entry' });

/**
 * Counts the client's fetch calls in window.optionResetFetches, where they happen. FullCalendar refetches synchronously
 * when it applies options, the fetch-count span would only show a refetch after the server round trip.
 */
async function countFetches(page) {
    await calendarOf(page).evaluate((el) => {
        const fetch = el.$server.fetchEntriesFromServer;
        window.optionResetFetches = 0;
        el.$server.fetchEntriesFromServer = (...args) => {
            window.optionResetFetches++;
            return fetch.apply(el.$server, args);
        };
    });
}

const fetches = (page) => page.evaluate(() => window.optionResetFetches);

/** Background color of the timed entry's element and the entry color variable on it. */
const entryColor = (page) => timedEntry(page).evaluate((el) => ({
    background: getComputedStyle(el).backgroundColor,
    variable: el.style.getPropertyValue('--fc-event-color'),
}));

test.describe('Removing options of an attached calendar', () => {

    test.beforeEach(async ({ page }) => {
        await gotoView(page);
    });

    test('removing the entry color restores the color of the initial JSON options', async ({ page }) => {
        const initial = await entryColor(page);
        expect(initial.variable).toBe('green');

        await click(page, 'Set entry color');
        await expect.poll(async () => (await entryColor(page)).variable).toBe('red');

        await click(page, 'Remove entry color');
        await expect.poll(() => entryColor(page)).toEqual(initial);
    });

    test('removing an option the initial JSON options do not set restores FullCalendar\'s default', async ({ page }) => {
        const textColor = () => timedEntry(page).evaluate((el) =>
            el.style.getPropertyValue('--fc-event-contrast-color'));
        const initial = await textColor();
        expect(initial).not.toBe('yellow');

        await click(page, 'Set entry text color');
        await expect.poll(textColor).toBe('yellow');

        await click(page, 'Remove entry text color');
        await expect.poll(textColor).toBe(initial);
    });

    test('options set or removed while the calendar is detached apply when it is attached again', async ({ page }) => {
        const initial = await entryColor(page);

        await click(page, 'Set entry color');
        await expect.poll(async () => (await entryColor(page)).variable).toBe('red');

        await click(page, 'Set entry color while detached');
        await expect.poll(async () => (await entryColor(page)).variable).toBe('orange');

        await click(page, 'Remove entry color while detached');
        await expect.poll(() => entryColor(page)).toEqual(initial);
    });

    test('a time zone set while the calendar is detached refetches the entries when it is attached again',
        async ({ page }) => {
            await countFetches(page);
            await click(page, 'Set time zone while detached');
            await expect(timedEntry(page).locator('.fc-event-time')).toContainText('05:00');
            await expect.poll(() => fetches(page)).toBeGreaterThan(0);
        });

    test('removing the height restores the add-on\'s full height', async ({ page }) => {
        // FullCalendar writes the height on the element, the layout of the view stretches it anyway
        const height = () => calendarOf(page).evaluate((el) => el.style.height);
        await expect.poll(height).toBe('100%');

        await click(page, 'Set height');
        await expect.poll(height).toBe('300px');

        await click(page, 'Remove height');
        await expect.poll(height).toBe('100%');
    });

    test('an option removed between a detach and an attach in separate requests applies', async ({ page }) => {
        const initial = await entryColor(page);

        await click(page, 'Set entry color');
        await expect.poll(async () => (await entryColor(page)).variable).toBe('red');

        await click(page, 'Detach');
        await expect(calendarOf(page)).toHaveCount(0);
        await click(page, 'Remove entry color');
        await click(page, 'Attach');
        await expect(timedEntry(page)).toBeVisible();
        await expect.poll(() => entryColor(page)).toEqual(initial);
    });

    test('an eventDidMount callback removed while the calendar is detached no longer runs', async ({ page }) => {
        const mounted = () => timedEntry(page).evaluate((el) => el.dataset.mounted ?? '');

        // the callback applies to entries mounted afterwards, so the views are switched
        await click(page, 'Set entry did mount');
        await click(page, 'Show month');
        await click(page, 'Show week');
        await expect.poll(mounted).toBe('yes');

        await click(page, 'Remove entry did mount while detached');
        await click(page, 'Show month');
        await click(page, 'Show week');
        await expect(timedEntry(page)).toBeVisible();
        await expect.poll(mounted).toBe('');
    });

    test('removing editable restores the add-on\'s own value', async ({ page }) => {
        // the add-on sets editable to true, FullCalendar's default is false
        const draggable = calendarOf(page).locator('.fc-event.fc-event-draggable');
        await expect(draggable.first()).toBeVisible();

        await click(page, 'Set not editable');
        await expect(draggable).toHaveCount(0);

        await click(page, 'Remove editable');
        await expect(draggable.first()).toBeVisible();
    });

    test('removing the all-day text restores the text of the locale', async ({ page }) => {
        const calendar = calendarOf(page);
        await expect(calendar.getByText(ALL_DAY_TEXT, { exact: true })).toBeVisible();

        await click(page, 'Set all-day text');
        await expect(calendar.getByText('Custom', { exact: true })).toBeVisible();
        await expect(calendar.getByText(ALL_DAY_TEXT, { exact: true })).toHaveCount(0);

        await click(page, 'Remove all-day text');
        await expect(calendar.getByText(ALL_DAY_TEXT, { exact: true })).toBeVisible();
        await expect(calendar.getByText('Custom', { exact: true })).toHaveCount(0);
        await expect(calendar.getByText('null', { exact: true })).toHaveCount(0);
    });

    test('removing an option without a default restores the behaviour of the unset option', async ({ page }) => {
        // displayEventTime has no default, FullCalendar decides per view whether to show the time
        const time = timedEntry(page).locator('.fc-event-time');
        await expect(time).toBeVisible();

        await click(page, 'Hide entry time');
        await expect(time).toHaveCount(0);

        await click(page, 'Remove entry time display');
        await expect(time).toBeVisible();
    });

    test('removing a view-specific option restores the default of the view', async ({ page }) => {
        const calendar = calendarOf(page);

        await click(page, 'Set week all-day text');
        await expect(calendar.getByText('Week', { exact: true })).toBeVisible();

        await click(page, 'Remove week all-day text');
        await expect(calendar.getByText(ALL_DAY_TEXT, { exact: true })).toBeVisible();
        await expect(calendar.getByText('Week', { exact: true })).toHaveCount(0);
    });

    test('removing two options one after the other restores both', async ({ page }) => {
        const initialColor = await entryColor(page);
        const time = timedEntry(page).locator('.fc-event-time');

        await click(page, 'Set entry color');
        await click(page, 'Hide entry time');
        await expect(time).toHaveCount(0);

        await click(page, 'Remove entry color');
        await expect.poll(() => entryColor(page)).toEqual(initialColor);
        await click(page, 'Remove entry time display');
        await expect(time).toBeVisible();
    });

    test('an eventAdd callback set by the application still runs after an option was removed', async ({ page }) => {
        await click(page, 'Set entry add callback');
        await click(page, 'Hide entry time');
        await click(page, 'Remove entry time display');
        await expect(timedEntry(page).locator('.fc-event-time')).toBeVisible();

        await calendarOf(page).evaluate((el) =>
            el.calendar.addEvent({ title: 'Client entry', start: '2025-03-06T10:00:00' }));
        expect(await page.evaluate(() => window.optionResetEventAdds)).toBe(1);
        // getOption returns the application's callback, not the function that applies the removal
        expect(await calendarOf(page).evaluate((el) => String(el.getOption('eventAdd'))))
            .toContain('optionResetEventAdds');
    });

    test('removing the time zone restores the time zone of the add-on', async ({ page }) => {
        // the timed entry starts at 10:00 UTC, the add-on shows UTC unless a time zone is set
        const time = timedEntry(page).locator('.fc-event-time');
        await expect(time).toContainText('10:00');

        await click(page, 'Set time zone');
        await expect(time).toContainText('05:00');

        await click(page, 'Remove time zone');
        await expect(time).toContainText('10:00');
    });

    test('view-specific options of the initial JSON options and of the server apply together', async ({ page }) => {
        const calendar = calendarOf(page);
        const lastSlot = () => calendar.locator('.fc-timegrid-slot-label[data-time]').last().getAttribute('data-time');
        const MONDAY = 'Montag';

        // at creation: the JSON day header format and the slot end the server set before attach
        await expect(calendar.getByText(MONDAY, { exact: true })).toBeVisible();
        expect(await lastSlot()).toMatch(/^19:/);

        // at runtime: a view-specific option of the server for the same view keeps the JSON one
        await click(page, 'Set week all-day text');
        await expect(calendar.getByText('Week', { exact: true })).toBeVisible();
        await expect(calendar.getByText(MONDAY, { exact: true })).toBeVisible();

        await click(page, 'Remove week all-day text');
        await expect(calendar.getByText(ALL_DAY_TEXT, { exact: true })).toBeVisible();
        await expect(calendar.getByText(MONDAY, { exact: true })).toBeVisible();
        expect(await lastSlot()).toMatch(/^19:/);
    });

    test('removing one of two view-specific options keeps the other', async ({ page }) => {
        const calendar = calendarOf(page);

        await click(page, 'Set week entry color');
        await click(page, 'Set week all-day text');
        await expect.poll(async () => (await entryColor(page)).variable).toBe('blue');
        await expect(calendar.getByText('Week', { exact: true })).toBeVisible();

        await click(page, 'Remove week all-day text');
        await expect(calendar.getByText(ALL_DAY_TEXT, { exact: true })).toBeVisible();
        expect((await entryColor(page)).variable).toBe('blue');
    });

    test('an explicit value equal to the locale text stays when the locale changes', async ({ page }) => {
        const calendar = calendarOf(page);

        await click(page, 'Set locale all-day text');
        await click(page, 'Set English locale');
        // the long weekday names of the week view, so the English locale is applied
        await expect(calendar.getByText('Monday', { exact: true })).toBeVisible();
        await expect(calendar.getByText('all-day', { exact: true })).toHaveCount(0);
        await expect(calendar.getByText(ALL_DAY_TEXT, { exact: true })).toBeVisible();
    });

    test('the code of the callbacks in the initial JSON options runs once', async ({ page }) => {
        const evaluations = () => page.evaluate(() => window.optionResetJsonEvaluations);
        // one at top level, one in the views
        expect(await evaluations()).toBe(2);

        await click(page, 'Set week all-day text');
        await click(page, 'Remove week all-day text');
        await expect(calendarOf(page).getByText(ALL_DAY_TEXT, { exact: true })).toBeVisible();
        expect(await evaluations()).toBe(2);
    });

    test('removing the more link action restores the action of the initial JSON options', async ({ page }) => {
        await click(page, 'Set more link popover');
        await click(page, 'Remove more link action');
        await click(page, 'Show month');

        await page.locator('.fc-daygrid-more-link').first().click();
        await expect(page.locator('.fc-dayGridDay-view, .fc-timeGridDay-view')).toHaveCount(1);
        await expect(page.locator('.fc-popover')).toHaveCount(0);
    });

    test('removing the view did mount callback restores the add-on\'s own one', async ({ page }) => {
        // the add-on reports a rendered view to the server through its own viewDidMount handler
        const count = page.locator('#view-rendered-count');
        await expect(count).toHaveText('1'); // the week view shown at start

        await click(page, 'Set view did mount');
        await click(page, 'Remove view did mount');
        await click(page, 'Show month');

        await expect(count).toHaveText('2');
    });

    test('removing an option after the calendar was attached again restores it', async ({ page }) => {
        const initial = await entryColor(page);

        await click(page, 'Set entry color');
        await click(page, 'Set time zone');
        await countFetches(page);
        await click(page, 'Attach again');
        await expect.poll(async () => (await entryColor(page)).variable).toBe('red');
        // restoring the options refetches only for a changed time zone, the restored one is unchanged
        expect(await fetches(page)).toBe(0);

        await click(page, 'Remove entry color');
        await expect.poll(() => entryColor(page)).toEqual(initial);
    });

    test('setting and removing an unrelated option neither refetches nor re-mounts the entries', async ({ page }) => {
        // keep the element of the rendered entry, to compare it after the option changes
        await timedEntry(page).evaluate((el) => { window.optionResetEntry = el; });
        await countFetches(page);

        await click(page, 'Set all-day text');
        await expect(calendarOf(page).getByText('Custom', { exact: true })).toBeVisible();
        await click(page, 'Remove all-day text');
        await expect(calendarOf(page).getByText(ALL_DAY_TEXT, { exact: true })).toBeVisible();

        expect(await fetches(page)).toBe(0);
        expect(await timedEntry(page).evaluate((el) => el === window.optionResetEntry && el.isConnected)).toBe(true);
    });
});

test.describe('Removing an option of a calendar the browser creates again', () => {

    test('a removed option stays removed after a refresh', async ({ page }) => {
        const color = () => page.locator('#cal-option-reset-preserve .fc-event').first()
            .evaluate((el) => el.style.getPropertyValue('--fc-event-color'));

        await page.goto('/test/option-reset-preserve');
        await page.waitForSelector('#cal-option-reset-preserve .fc-event', { timeout: 10000 });
        await waitForVaadin(page);
        expect(await color()).toBe('red');

        await page.getByRole('button', { name: 'Remove entry color', exact: true }).click();
        await waitForVaadin(page);
        await expect.poll(color).not.toBe('red');
        const removed = await color();
        expect(removed).not.toBe('null');

        // the view keeps its components, the browser creates a new calendar element
        await page.reload();
        await page.waitForSelector('#cal-option-reset-preserve .fc-event', { timeout: 10000 });
        await waitForVaadin(page);
        expect(await color()).toBe(removed);
    });
});
