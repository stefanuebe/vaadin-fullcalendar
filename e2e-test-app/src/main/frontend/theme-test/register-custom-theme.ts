import {FullCalendar} from 'Frontend/generated/jar-resources/vaadin-full-calendar/full-calendar';

// Registered the way the wiki documents it: a lazy import of a module whose default export is the theme plugin.
FullCalendar.registerTheme('test-custom', () => import('./custom-theme-plugin'));

// Loads only once the test calls window.releaseGatedTheme(), so the test can look at the calendar while its initial
// theme is still loading.
let releaseGate: () => void;
const gate = new Promise<void>(resolve => releaseGate = resolve);
(window as any).releaseGatedTheme = () => releaseGate();
FullCalendar.registerTheme('test-gated', () => gate.then(() => import('./custom-theme-plugin')));

// Fails once the test calls window.failFailingTheme(). window.failingThemeLoads counts the loader runs.
let failGate: () => void;
const failing = new Promise<void>(resolve => failGate = resolve);
(window as any).failFailingTheme = () => failGate();
(window as any).failingThemeLoads = 0;
FullCalendar.registerTheme('test-failing', () => {
    (window as any).failingThemeLoads++;
    return failing.then(() => Promise.reject(new Error('test load failure')));
});

// Replaced while it loads: window.replaceSwappedTheme() registers the custom theme under the same name and then lets
// the old loader finish with a plugin of its own. The calendar must end up with the new loader's plugin.
let releaseOld: () => void;
const oldLoad = new Promise<void>(resolve => releaseOld = resolve);
FullCalendar.registerTheme('test-swapped', () => oldLoad.then(() => ({
    name: 'test-old-theme',
    optionDefaults: {eventClass: 'test-old-theme-entry'},
})));
(window as any).replaceSwappedTheme = () => {
    FullCalendar.registerTheme('test-swapped', () => import('./custom-theme-plugin'));
    releaseOld();
};

// Like test-swapped, but the old loader fails after it was replaced. Its failure must not count.
let failOld: () => void;
const oldFailingLoad = new Promise<void>(resolve => failOld = resolve);
FullCalendar.registerTheme('test-swapped-failing',
    () => oldFailingLoad.then(() => Promise.reject(new Error('test load failure of a replaced loader'))));
(window as any).replaceSwappedFailingTheme = () => {
    FullCalendar.registerTheme('test-swapped-failing', () => import('./custom-theme-plugin'));
    failOld();
};
