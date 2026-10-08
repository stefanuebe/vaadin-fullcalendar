package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.AppTheme;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Test view for the FullCalendar Vaadin theme (UC-023).
 * <p>
 * Calendars show today with two entries, of which one fits and one is behind the "+1 more" link. The first uses the
 * default theme, the second and third the default theme with the option colorScheme set to dark and to light, the
 * fourth classic. The entries of the dark one can be dragged. The fifth has the class "custom-entry-color", for which
 * the application stylesheet sets the entry colors, over its rule for all calendars. The sixth shows a day in March
 * 2025 with one entry of a JSON feed entry source. Open the view with {@code ?theme=aura} and {@code ?scheme=dark} for
 * Aura and a dark application, see {@link AppTheme}.
 * <p>
 * The button "Remove color scheme" sets the option colorScheme of the light calendar to null and disables itself.
 * <p>
 * Route: /test/vaadin-theme
 */
@Route(value = "vaadin-theme", layout = TestLayout.class)
@MenuItem(label = "Vaadin Theme")
@CssImport("./theme-test/vaadin-theme-override.css")
public class VaadinThemeTestView extends VerticalLayout {

    public VaadinThemeTestView() {
        setSizeFull();

        add(new H2("FullCalendar Vaadin theme"));

        FullCalendar standard = createCalendar("cal-vaadin");

        FullCalendar dark = createCalendar("cal-vaadin-dark");
        dark.setOption(Option.COLOR_SCHEME, "dark");
        // its entries can be dragged, to check the entry FullCalendar shows outside the calendar while dragging
        dark.setOption(Option.EDITABLE, true);

        FullCalendar light = createCalendar("cal-vaadin-light");
        light.setOption(Option.COLOR_SCHEME, "light");

        Button removeColorScheme = new Button("Remove color scheme", event -> {
            light.setOption(Option.COLOR_SCHEME, null);
            event.getSource().setEnabled(false);
        });

        FullCalendar classic = createCalendar("cal-classic");
        classic.setTheme(FullCalendarTheme.CLASSIC);

        FullCalendar custom = createCalendar("cal-vaadin-custom");
        custom.addClassName("custom-entry-color");

        FullCalendar source = new FullCalendar();
        source.setId("cal-vaadin-source");
        source.setOption(Option.INITIAL_DATE, LocalDate.of(2025, 3, 10));
        source.addRemoteEntrySource(new JsonFeedEntrySource("/test-data/vaadin-theme-entries.json"));

        add(removeColorScheme, standard, dark, light, classic, custom, source);
        setFlexGrow(1, standard, dark, light, classic, custom, source);
    }

    /** Its entries are on today in the calendar's time zone, UTC, which also decides the client's today. */
    private FullCalendar createCalendar(String id) {
        FullCalendar calendar = new FullCalendar();
        calendar.setId(id);
        calendar.setOption(Option.DAY_MAX_ENTRIES, 1);

        Entry first = new Entry();
        first.setTitle("First entry");
        first.setStart(LocalDate.now(ZoneOffset.UTC).atStartOfDay());
        first.setAllDay(true);

        Entry second = new Entry();
        second.setTitle("Second entry");
        second.setStart(LocalDate.now(ZoneOffset.UTC).atStartOfDay());
        second.setAllDay(true);

        calendar.setEntryProvider(EntryProvider.inMemoryFrom(first, second));

        return calendar;
    }
}
