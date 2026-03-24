package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Test view for verifying server-side roundtrip (Pattern 5).
 * <p>
 * Tests: "When I use a server-side event listener and update an entry, are the changes
 * correctly shown in the client afterwards?"
 * <ul>
 *   <li>Click "Click to Rename" → server changes title to "Renamed!" → client shows new title</li>
 *   <li>Click "Click to Recolor" → server changes color to "red" → client shows red entry</li>
 *   <li>Drop entry → server applies changes + refreshItem → entry appears at new date</li>
 * </ul>
 * Route: /test/roundtrip
 */
@Route(value = "roundtrip", layout = TestLayout.class)
@MenuItem(label = "Roundtrip")
public class RoundtripTestView extends VerticalLayout {

    public RoundtripTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Roundtrip"));
        add(new Paragraph("Click entries to trigger server-side modifications visible in the client."));

        // --- Calendar ---
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.LUMO);
        calendar.setLocale(Locale.ENGLISH);
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.setOption("initialView", CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());
        calendar.setOption(FullCalendar.Option.EDITABLE, true);

        // --- Entries ---
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        Entry renameEntry = new Entry();
        renameEntry.setTitle("Click to Rename");
        renameEntry.setStart(LocalDate.of(2025, 3, 5).atStartOfDay());
        renameEntry.setAllDay(true);
        provider.addEntry(renameEntry);

        Entry recolorEntry = new Entry();
        recolorEntry.setTitle("Click to Recolor");
        recolorEntry.setStart(LocalDate.of(2025, 3, 7).atStartOfDay());
        recolorEntry.setAllDay(true);
        recolorEntry.setColor("blue");
        provider.addEntry(recolorEntry);

        Entry dropEntry = new Entry();
        dropEntry.setTitle("Drop Target");
        dropEntry.setStart(LocalDate.of(2025, 3, 10).atStartOfDay());
        dropEntry.setAllDay(true);
        provider.addEntry(dropEntry);

        // Entry that will be removed on click
        Entry removeMe = new Entry();
        removeMe.setTitle("Click to Remove");
        removeMe.setStart(LocalDate.of(2025, 3, 14).atStartOfDay());
        removeMe.setAllDay(true);
        provider.addEntry(removeMe);

        calendar.setEntryProvider(provider);
        calendar.setOption(FullCalendar.Option.SELECTABLE, true);

        // --- Listeners ---

        // Click → rename, recolor, or remove on the server, then push update to client
        calendar.addEntryClickedListener(e -> {
            Entry entry = e.getEntry();
            if ("Click to Rename".equals(entry.getTitle())) {
                entry.setTitle("Renamed!");
                provider.refreshItem(entry);
            } else if ("Click to Recolor".equals(entry.getTitle())) {
                entry.setColor("red");
                provider.refreshItem(entry);
            } else if ("Click to Remove".equals(entry.getTitle())) {
                provider.removeEntry(entry);
                provider.refreshAll();
            }
        });

        // Timeslot click → server creates a new entry at that date
        calendar.addTimeslotClickedListener(e -> {
            Entry newEntry = new Entry();
            newEntry.setTitle("Server Created");
            newEntry.setStart(e.getDateTime());
            newEntry.setAllDay(e.isAllDay());
            provider.addEntry(newEntry);
            provider.refreshAll();
        });

        // Drop → apply new dates + push to client
        calendar.addEntryDroppedListener(e -> {
            e.applyChangesOnEntry();
            provider.refreshItem(e.getEntry());
        });

        add(calendar);
        setFlexGrow(1, calendar);
    }
}
