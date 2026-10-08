import {PluginInput} from 'fullcalendar';

// A minimal FullCalendar theme: one class on every entry, so the test can see that the theme was applied.
const customThemePlugin: PluginInput = {
    name: 'test-custom-theme',
    optionDefaults: {
        eventClass: 'test-custom-theme-entry',
    },
};

export default customThemePlugin;
