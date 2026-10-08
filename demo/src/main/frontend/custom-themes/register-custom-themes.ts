import {FullCalendar} from 'Frontend/generated/jar-resources/vaadin-full-calendar/full-calendar';

// Registers the two custom FullCalendar themes of the custom theme demo. The view loads this file with @JsModule, so
// the themes are registered before its calendar selects one. The browser loads a theme module only when a calendar
// selects the theme.
FullCalendar.registerTheme('demo-breezy', () => import('./demo-breezy'));
FullCalendar.registerTheme('demo-plain', () => import('./demo-plain'));
