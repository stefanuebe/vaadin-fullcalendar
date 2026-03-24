# Design System

> Styling, theming, and visual standards for the FullCalendar Flow addon. Reference this when modifying CSS or reviewing UI appearance.

---

## 1. Theme Architecture

The addon uses **light DOM** (no shadow DOM), so all FullCalendar CSS is directly accessible. Four style layers:

| Layer | File | Purpose |
|-------|------|---------|
| **FullCalendar native** | Bundled via `@fullcalendar/*` npm packages | Default FC appearance (grid, entries, toolbar) |
| **Addon base styles** | `full-calendar-styles.css` | Sizing, layout fixes, integration with Vaadin |
| **Lumo theme variant** | `full-calendar-theme-lumo.css` | Aligns FC look with the Vaadin Lumo theme |
| **Scheduler styles** | `full-calendar-scheduler-styles.css` | Additional styles for scheduler views |

Apply the Vaadin theme variant:
```java
calendar.addThemeVariants(FullCalendarVariant.VAADIN);
```

### Custom Properties (`--vaadin-fc-*`)

The Vaadin theme variant defines these CSS custom properties on `html`. Override them to retheme:

| Token | Resolves to | Purpose |
|-------|-------------|---------|
| `--vaadin-fc-bg-color` | `--vaadin-background-color` | Calendar background |
| `--vaadin-fc-highlight-color` | `--lumo-primary-color` | Primary accent (entries, selection, today badge) |
| `--vaadin-fc-highlight-color-100..800` | `color-mix()` alpha variants of above | Hover, selection highlight, background entries |
| `--vaadin-fc-highlight-text-color` | `--lumo-primary-text-color` | Today badge text |
| `--vaadin-fc-contrast-color` | `--lumo-contrast` | Grid lines, now indicator |
| `--vaadin-fc-contrast-color-50..800` | `color-mix()` alpha variants of above | Borders, non-business shading |
| `--vaadin-fc-neutral-color` | `--vaadin-text-color-disabled` at 15% | Non-business hours shading |
| `--vaadin-fc-hover-color` | `--vaadin-fc-highlight-color-100` | Entry/link hover background |
| `--vaadin-fc-font-size-s` | `--lumo-font-size-s` | Small text (headers, labels) |
| `--vaadin-fc-line-height-xs` | `--lumo-line-height-xs` | Compact line height |

**Fallback chain**: Every token uses `var(--lumo-*, fallback)`, so custom themes that do not define Lumo variables get CSS fallback values.

**Browser compatibility**: The Vaadin theme variant uses `color-mix(in srgb, ...)` which requires Chrome 111+, Firefox 113+, Safari 16.2+.

**Entry hover effect**: The base styles apply `filter: brightness(90%) contrast(1.2)` on `.fc-event:not(.fc-bg-event):hover`. This is a direct CSS rule, not a custom property — there is no token to override it. To change or disable the hover effect, override the rule directly:
```css
vaadin-full-calendar .fc-event:not(.fc-bg-event):hover {
    filter: none; /* disable hover darkening */
}
```

### Known Limitation: Entry Text Contrast

When developers use custom entry background colors, FullCalendar defaults entry text to white. This can produce poor contrast on light backgrounds. The Vaadin theme includes a commented-out OKLCH-based auto-contrast rule (`--fc-event-text-color`) that is not yet active. **Developers using custom entry colors must verify text contrast meets WCAG 4.5:1 ratio themselves.**

---

## 2. CSS Customization

Since the component uses light DOM, any CSS can target FC elements from document scope.

**Addon-internal CSS** (bundled with the addon): Uses `@CssImport("./vaadin-full-calendar/...")` on the component class. Files live under `META-INF/resources/frontend/`.

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
| **Global** | `Option.ENTRY_COLOR`, `ENTRY_BACKGROUND_COLOR`, etc. | `calendar.setOption(Option.ENTRY_COLOR, "#3788d8")` |
| **Per-resource** | `Resource.setEntryBackgroundColor()`, etc. | `resource.setEntryBackgroundColor("#ff6b6b")` |
| **Per-entry** | `Entry.setColor()`, `setBackgroundColor()`, `setTextColor()`, `setBorderColor()` | `entry.setColor("red")` |
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
| `SchedulerOption.RESOURCE_AREA_WIDTH` | Resource panel width in scheduler | Auto |
| `SchedulerOption.SLOT_MIN_WIDTH` | Minimum slot width in timeline | Auto |

The component includes a `ResizeObserver` for responsive sizing (cleaned up in `disconnectedCallback()`).

---

## 5. Toolbar

Configured via `Option.HEADER_TOOLBAR` and `Option.FOOTER_TOOLBAR`:

```java
calendar.setOption(Option.HEADER_TOOLBAR,
    Map.of("left", "prev,next,today",
           "center", "title",
           "right", "dayGridMonth,timeGridWeek,timeGridDay"));
```

Button labels: `Option.NATIVE_TOOLBAR_BUTTON_TEXT` (e.g., `Map.of("today", "Heute")`).

---

## 6. Responsive Behavior

- The calendar adapts to its container width automatically via a `ResizeObserver`
- The host element must have a sized ancestor — without explicit height on a parent, the calendar may collapse to 0px
- Use `Option.DAY_MIN_WIDTH` to enable horizontal scrolling on narrow containers (sensible default: ~100px)

### Recommended Views by Width

| Width | Recommended Views | Notes |
|-------|-------------------|-------|
| < 480px (mobile) | `listWeek`, `listDay` | Full-width entry list; avoid timegrid (slots too narrow) |
| 480–768px (tablet) | `dayGridMonth`, `listWeek` | Month grid works; timegrid borderline |
| > 768px (desktop) | All views | Timegrid, timeline, and resource views work well |

### Toolbar Overflow

When many buttons are configured on narrow screens, FullCalendar wraps toolbar sections to new lines. The addon does not add custom overflow handling. Developers on mobile should reduce toolbar buttons or use a `Vaadin MenuBar` for view switching instead of the native FC toolbar.

### Animation & Reduced Motion

The addon uses `filter: brightness(90%) contrast(1.2)` for entry hover and `DRAG_REVERT_DURATION` for drag animations. These do **not** automatically respect `prefers-reduced-motion`. Developers targeting WCAG should add:
```css
@media (prefers-reduced-motion: reduce) {
    vaadin-full-calendar .fc-event { transition: none !important; }
}
```
