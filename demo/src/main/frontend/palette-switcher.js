// Switches the palette of a stock FullCalendar theme for the demo. The add-on loads the default palette inside the
// cascade layer "fc-palette", so a palette added here without a layer wins over it. The keys match the palettes of
// FullCalendarPalette.
const palettes = {
    'monarch/blue': () => import('fullcalendar/themes/monarch/palettes/blue.css?inline'),
    'monarch/green': () => import('fullcalendar/themes/monarch/palettes/green.css?inline'),
    'monarch/purple': () => import('fullcalendar/themes/monarch/palettes/purple.css?inline'),
    'monarch/red': () => import('fullcalendar/themes/monarch/palettes/red.css?inline'),
    'monarch/yellow': () => import('fullcalendar/themes/monarch/palettes/yellow.css?inline'),
    'breezy/amber': () => import('fullcalendar/themes/breezy/palettes/amber.css?inline'),
    'breezy/emerald': () => import('fullcalendar/themes/breezy/palettes/emerald.css?inline'),
    'breezy/indigo': () => import('fullcalendar/themes/breezy/palettes/indigo.css?inline'),
    'breezy/rose': () => import('fullcalendar/themes/breezy/palettes/rose.css?inline'),
    'forma/blue': () => import('fullcalendar/themes/forma/palettes/blue.css?inline'),
    'forma/green': () => import('fullcalendar/themes/forma/palettes/green.css?inline'),
    'forma/purple': () => import('fullcalendar/themes/forma/palettes/purple.css?inline'),
    'forma/red': () => import('fullcalendar/themes/forma/palettes/red.css?inline'),
    'pulse/blue': () => import('fullcalendar/themes/pulse/palettes/blue.css?inline'),
    'pulse/green': () => import('fullcalendar/themes/pulse/palettes/green.css?inline'),
    'pulse/purple': () => import('fullcalendar/themes/pulse/palettes/purple.css?inline'),
    'pulse/red': () => import('fullcalendar/themes/pulse/palettes/red.css?inline'),
};

/** Sets the palette, e.g. "breezy/emerald". Without a key, the theme's default palette applies again. */
window.demoSetPalette = async (key) => {
    document.getElementById('demo-palette')?.remove();
    if (!key) {
        return;
    }
    const css = await palettes[key]();
    document.getElementById('demo-palette')?.remove();
    const style = document.createElement('style');
    style.id = 'demo-palette';
    // Vaadin's build turns "?inline" CSS into a Lit CSSResult, String() gives its CSS text
    style.textContent = String(css.default);
    document.head.appendChild(style);
};
