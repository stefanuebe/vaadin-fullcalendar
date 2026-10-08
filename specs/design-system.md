# Design System

> Styling, theming, and visual standards for the FullCalendar Flow addon. Reference this when modifying CSS or reviewing UI appearance.

---

## 1. Theme Architecture

The addon uses **light DOM** (no shadow DOM), so all FullCalendar CSS is directly accessible. Style layers:

| Layer | File | Purpose |
|-------|------|---------|
| **FullCalendar skeleton** | `fullcalendar/skeleton.css`, imported by `full-calendar.ts` | Layout every FullCalendar theme builds on |
| **FullCalendar theme** | Theme plugin plus `theme.css`, loaded on demand by the theme registry in `full-calendar.ts` (UC-027). Stock themes bring their default palette inside the cascade layer `fc-palette` (ADR 0002) | Look of the calendar (grid, entries, toolbar) |
| **Stable class names** | `legacy-class-names.ts`, `legacy-class-names-scheduler.ts` (FullCalendar plugins) | Re-add the documented v6 class names, see UC-025 and ADR 0001 |
| **Addon base styles** | `full-calendar-styles.css` | Sizing, layout fixes, integration with Vaadin |
| **Vaadin FullCalendar theme** | `vaadin-theme.ts` and `vaadin-theme.css`, `FullCalendarTheme.VAADIN`, the default. Loaded with the main module (UC-023) | Aligns FullCalendar look with Vaadin Lumo/Aura theme |
| **Scheduler styles** | `full-calendar-scheduler-styles.css` | Additional styles for scheduler views |

Select a FullCalendar theme per calendar:
```java
calendar.setTheme(FullCalendarTheme.MONARCH);
```

### Vaadin FullCalendar Theme Variables

The Vaadin FullCalendar theme is classic plus `vaadin-theme.css`. The plugin puts the class `fc-vaadin` on the calendar,
its popover and its entries. The rules set classic's color variables `--fc-classic-*` on `.fc-vaadin`, inside the
cascade layer `fc-palette` (ADR 0002), so unlayered application CSS overrides them:

```css
.fc-vaadin { --fc-classic-event: #2e7d32; }
```

The colors derive from three base colors of the application theme:

| Base color | Lumo | Aura |
|------------|------|------|
| Background | `--lumo-base-color` | `--aura-surface-color-solid` |
| Text | `--lumo-body-text-color` | `--aura-neutral` |
| Accent | `--lumo-primary-color` | `--aura-accent-color` |

Neutral backgrounds, borders and secondary text are `color-mix()` of the text color. The base `--vaadin-*` colors are the
fallback without Lumo and Aura. There are no `--vaadin-fc-*` variables.

**Color scheme**: Lumo and Aura switch colors through `color-scheme`. `Option.COLOR_SCHEME` sets `data-color-scheme` on
the calendar, its popover and a dragged entry, which switches `color-scheme` for them.

**Browser compatibility**: The theme uses `color-mix(in srgb, ...)` which requires Chrome 111+, Firefox 113+, Safari 16.2+.

**Entry hover effect**: The base styles apply `filter: brightness(90%) contrast(1.2)` on `.fc-event:not(.fc-bg-event):hover`. This is a direct CSS rule, not a custom property — there is no token to override it. To change or disable the hover effect, override the rule directly:
```css
vaadin-full-calendar .fc-event:not(.fc-bg-event):hover {
    filter: none; /* disable hover darkening */
}
```

### Known Limitation: Entry Text Contrast

When developers use custom entry background colors, FullCalendar defaults entry text to white. This can produce poor contrast on light backgrounds. **Developers using custom entry colors must verify text contrast meets WCAG 4.5:1 ratio themselves.**

---

## 2. CSS Customization

Since the component uses light DOM, any CSS can target FullCalendar elements from document scope. FullCalendar 7 renders build-generated class names only. Stable hooks for CSS and tests are the stable class names (UC-025), the `data-date` / `data-time` / `data-resource-id` attributes and ARIA roles. Selectors that depend on the DOM structure (`table td`, `> a`) are not stable.

**Addon-internal CSS** (bundled with the addon): Uses `@CssImport("./vaadin-full-calendar/...")` on the component class. Files live under `META-INF/frontend/`. This is the correct V25 mechanism for addon/component CSS bundled via Vite.

**Application-level customization** (by addon users): Use `@StyleSheet` on `AppShellConfigurator` or place CSS in the app's stylesheet. Example:

```css
/* Example: custom entry colors */
vaadin-full-calendar .fc-event {
    border-radius: 4px;
}

/* Example: hide weekend columns */
vaadin-full-calendar .fc-day-sat,
vaadin-full-calendar .fc-day-sun {
    display: none;
}
```

Custom element tags:
- `vaadin-full-calendar` — base calendar
- `vaadin-full-calendar-scheduler` — scheduler extension

---

## 3. Entry Styling

Entries can be styled at multiple levels (highest priority wins):

| Level | Mechanism | Example |
|-------|-----------|---------|
| **Global** | `Option.ENTRY_COLOR`, `ENTRY_CONTRAST_COLOR` | `calendar.setOption(Option.ENTRY_COLOR, "#3788d8")` |
| **Per-resource** | `Resource.setColor()`, `setEntryContrastColor()` | `resource.setColor("#ff6b6b")` |
| **Per-entry** | `Entry.setColor()`, `setContrastColor()` | `entry.setColor("red")` |
| **CSS classes** | `Entry.setClassNames(Set)` or `Resource.setEntryClassNames(Set)` | `entry.setClassNames(Set.of("urgent"))` |
| **Display mode** | `Entry.setDisplayMode(DisplayMode)` | `BACKGROUND`, `INVERSE_BACKGROUND`, `BLOCK`, `LIST_ITEM`, `NONE` |

---

## 4. Sizing & Layout

| Option | Description | Default |
|--------|-------------|---------|
| `Option.HEIGHT` | Total calendar height (px, %, or `"auto"`) | Fills parent |
| `Option.CONTENT_HEIGHT` | Event area height | `"auto"` |
| `Option.ASPECT_RATIO` | Width-to-height ratio | `1.35` |
| `Option.EXPAND_ROWS` | Stretch rows to fill vertically | `false` |
| `SchedulerOption.RESOURCE_COLUMNS_WIDTH` | Resource column width in scheduler | Auto |
| `SchedulerOption.SLOT_MIN_WIDTH` | Minimum slot width in timeline | Auto |

FullCalendar resizes itself when the component's size changes. There is no `updateSize()` to call.

---

## 5. Toolbar

Configured via `Option.HEADER_TOOLBAR` and `Option.FOOTER_TOOLBAR`:

```java
calendar.setOption(Option.HEADER_TOOLBAR,
    Map.of("left", "prev,next,today",
           "center", "title",
           "right", "dayGridMonth,timeGridWeek,timeGridDay"));
```

Button labels: the `buttons` option, one map per button (e.g., `calendar.setOption("buttons", Map.of("today", Map.of("text", "Heute")))`).

---

## 6. Responsive Behavior

- The calendar adapts to its container size automatically (FullCalendar observes its own size)
- The host element must have a sized ancestor — without explicit height on a parent, the calendar may collapse to 0px
- Use `Option.DAY_MIN_WIDTH` to enable horizontal scrolling on narrow containers (sensible default: ~100px)

### Recommended Views by Width

| Width | Recommended Views | Notes |
|-------|-------------------|-------|
| < 480px (mobile) | `listWeek`, `listDay` | Full-width entry list; avoid timegrid (slots too narrow) |
| 480–768px (tablet) | `dayGridMonth`, `listWeek` | Month grid works; timegrid borderline |
| > 768px (desktop) | All views | Timegrid, timeline, and resource views work well |

### Toolbar Overflow

When many buttons are configured on narrow screens, FullCalendar wraps toolbar sections to new lines. The addon does not add custom overflow handling. Developers on mobile should reduce toolbar buttons or use a `Vaadin MenuBar` for view switching instead of the built-in FullCalendar toolbar.

### Animation & Reduced Motion

The addon uses `filter: brightness(90%) contrast(1.2)` for entry hover and `DRAG_REVERT_DURATION` for drag animations. These do **not** automatically respect `prefers-reduced-motion`. Developers targeting WCAG should add:
```css
@media (prefers-reduced-motion: reduce) {
    vaadin-full-calendar .fc-event { transition: none !important; }
}
```
