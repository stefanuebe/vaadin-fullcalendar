package org.vaadin.stefan.ui.view.demos.customtheme;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.NativeToolbarParts;
import org.vaadin.stefan.fullcalendar.Option;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Demo view for custom FullCalendar themes. The themes are in the frontend folder custom-themes: demo-breezy builds on
 * the stock breezy theme, demo-plain is written from scratch. register-custom-themes.ts registers both with
 * {@code FullCalendar.registerTheme}, and the view selects one with {@code calendar.setTheme(name)}.
 * <p>
 * FullCalendar has no option to select a palette, a palette is plain CSS. This view sets the selected palette as the
 * attribute {@code data-demo-palette} on the body, and the theme's CSS declares the palette's colors under it, e.g.
 * {@code [data-demo-palette=ocean] .demo-breezy}. The body is used instead of the calendar, because FullCalendar adds
 * the "+more" popover and the entry that follows the pointer while dragging to the body, outside the calendar (unless
 * the calendar is inside a shadow root).
 */
@Route(value = "custom-theme", layout = MainLayout.class)
@PageTitle("Custom Theme")
@MenuItem(label = "Custom Theme")
@JsModule("./custom-themes/register-custom-themes.ts")
public class CustomThemeDemo extends VerticalLayout {

    private static final String PALETTE_ATTRIBUTE = "data-demo-palette";

    // the FullCalendar themes of this view with their palettes, the first palette is selected with the theme
    private static final Map<String, List<String>> PALETTES = new LinkedHashMap<>();

    static {
        PALETTES.put("demo-breezy", List.of("ocean", "sunset"));
        PALETTES.put("demo-plain", List.of("forest", "berry"));
    }

    private final Select<String> palette = new Select<>();

    public CustomThemeDemo() {
        FullCalendar calendar = new FullCalendar();
        ((InMemoryEntryProvider<Entry>) calendar.getEntryProvider())
                .addEntries(EntryService.createSimpleInstance().getEntries());
        calendar.setOption(Option.DAY_MAX_ENTRIES, 3);
        calendar.setOption(Option.EDITABLE, true);
        // without these listeners the calendar moves a dropped or resized entry back, see
        // FullCalendar#setAutoRevertUnappliedEntryChanges
        calendar.addEntryDroppedListener(event -> event.applyChangesOnEntry());
        calendar.addEntryResizedListener(event -> event.applyChangesOnEntry());
        calendar.setOption(Option.HEADER_TOOLBAR, Map.of(
                NativeToolbarParts.START, NativeToolbarParts.PREV + "," + NativeToolbarParts.NEXT + " "
                        + NativeToolbarParts.TODAY,
                NativeToolbarParts.CENTER, NativeToolbarParts.TITLE,
                NativeToolbarParts.END, CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue() + ","
                        + CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue() + ","
                        + CalendarViewImpl.LIST_WEEK.getClientSideValue()));

        palette.setLabel("Palette");
        palette.addValueChangeListener(event -> {
            // skip the null that setItems sets when the theme changes
            if (event.getValue() != null) {
                getUI().ifPresent(this::setPaletteAttribute);
            }
        });

        Select<String> theme = new Select<>();
        theme.setLabel("FullCalendar theme");
        theme.setItems(PALETTES.keySet());
        theme.addValueChangeListener(event -> {
            calendar.setTheme(event.getValue());
            List<String> palettes = PALETTES.get(event.getValue());
            palette.setItems(palettes);
            palette.setValue(palettes.getFirst());
        });
        theme.setValue(PALETTES.keySet().iterator().next());

        Checkbox dark = new Checkbox("Dark color scheme");
        dark.addValueChangeListener(event ->
                calendar.setOption(Option.COLOR_SCHEME, event.getValue() ? "dark" : "light"));

        HorizontalLayout controls = new HorizontalLayout(theme, palette, dark);
        controls.setAlignItems(Alignment.BASELINE);

        add(new Paragraph("Two custom FullCalendar themes, registered in JavaScript with FullCalendar.registerTheme "
                + "(see register-custom-themes.ts). demo-breezy builds on the stock breezy theme, sets its color "
                + "variables and adds a few class names, demo-plain is written from scratch for the month, week and "
                + "list view. Each theme has two palettes, and each palette has a light and a dark color scheme."),
                controls, calendar);
        calendar.setWidthFull();
        setFlexGrow(1, calendar);
        setSizeFull();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setPaletteAttribute(attachEvent.getUI());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // other views share the body, the attribute must not stay there
        detachEvent.getUI().getElement().removeAttribute(PALETTE_ATTRIBUTE);
        super.onDetach(detachEvent);
    }

    // the element of the UI is the body
    private void setPaletteAttribute(UI ui) {
        ui.getElement().setAttribute(PALETTE_ATTRIBUTE, palette.getValue());
    }
}
