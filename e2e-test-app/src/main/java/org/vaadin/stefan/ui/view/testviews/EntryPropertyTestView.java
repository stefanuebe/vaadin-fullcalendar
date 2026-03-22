package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.Div;
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
import java.util.Set;

/**
 * Test view for verifying visual effects of entry properties.
 * <p>
 * Verifies Pattern 1: "When I set a property on an entry, does it have the expected effect in the client?"
 * <ul>
 *   <li>color — CSS background-color</li>
 *   <li>backgroundColor / textColor — CSS background + text color</li>
 *   <li>borderColor — CSS border-color</li>
 *   <li>displayMode BACKGROUND — fc-bg-event class</li>
 *   <li>displayMode INVERSE_BACKGROUND — fc-bg-event class</li>
 *   <li>displayMode NONE — entry not visible</li>
 *   <li>classNames — custom CSS class on fc-event</li>
 *   <li>allDay true/false — placement in day-events vs timed area</li>
 *   <li>editable=false per-entry — no drag when calendar is editable</li>
 *   <li>durationEditable=false — no resize handle</li>
 * </ul>
 * Route: /test/entry-properties
 */
@Route(value = "entry-properties", layout = TestLayout.class)
@MenuItem(label = "Entry Properties")
public class EntryPropertyTestView extends VerticalLayout {

    public EntryPropertyTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Entry Properties"));
        add(new Paragraph("Tests visual effects of entry properties: color, displayMode, classNames, editable flags."));

        // --- Calendar ---
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setLocale(Locale.ENGLISH);
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.setOption("initialView", CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());
        calendar.setOption(FullCalendar.Option.EDITABLE, true);

        // --- Entry provider ---
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        // 1. Color entry — red
        Entry redEntry = new Entry();
        redEntry.setTitle("Red Entry");
        redEntry.setStart(LocalDate.of(2025, 3, 3).atStartOfDay());
        redEntry.setAllDay(true);
        redEntry.setColor("red");
        provider.addEntry(redEntry);

        // 2. Custom background + text color
        Entry customBg = new Entry();
        customBg.setTitle("Custom BG");
        customBg.setStart(LocalDate.of(2025, 3, 4).atStartOfDay());
        customBg.setAllDay(true);
        customBg.setBackgroundColor("#00ff00");
        customBg.setTextColor("#ffffff");
        provider.addEntry(customBg);

        // 3. Border color
        Entry borderEntry = new Entry();
        borderEntry.setTitle("Border Entry");
        borderEntry.setStart(LocalDate.of(2025, 3, 5).atStartOfDay());
        borderEntry.setAllDay(true);
        borderEntry.setBorderColor("blue");
        provider.addEntry(borderEntry);

        // 4. DisplayMode BACKGROUND
        Entry bgMode = new Entry();
        bgMode.setTitle("Background Mode");
        bgMode.setStart(LocalDate.of(2025, 3, 6).atStartOfDay());
        bgMode.setAllDay(true);
        bgMode.setDisplayMode(DisplayMode.BACKGROUND);
        bgMode.setColor("orange");
        provider.addEntry(bgMode);

        // 5. DisplayMode INVERSE_BACKGROUND
        Entry inverseBg = new Entry();
        inverseBg.setTitle("Inverse BG Mode");
        inverseBg.setStart(LocalDate.of(2025, 3, 7).atStartOfDay());
        inverseBg.setAllDay(true);
        inverseBg.setDisplayMode(DisplayMode.INVERSE_BACKGROUND);
        inverseBg.setColor("purple");
        provider.addEntry(inverseBg);

        // 6. DisplayMode NONE — hidden
        Entry hiddenEntry = new Entry();
        hiddenEntry.setTitle("Hidden Entry");
        hiddenEntry.setStart(LocalDate.of(2025, 3, 8).atStartOfDay());
        hiddenEntry.setAllDay(true);
        hiddenEntry.setDisplayMode(DisplayMode.NONE);
        provider.addEntry(hiddenEntry);

        // 7. Custom classNames
        Entry classEntry = new Entry();
        classEntry.setTitle("Custom Class");
        classEntry.setStart(LocalDate.of(2025, 3, 10).atStartOfDay());
        classEntry.setAllDay(true);
        classEntry.setClassNames(Set.of("my-custom-class"));
        provider.addEntry(classEntry);

        // 8. All-day entry
        Entry allDayEntry = new Entry();
        allDayEntry.setTitle("All-Day Entry");
        allDayEntry.setStart(LocalDate.of(2025, 3, 12).atStartOfDay());
        allDayEntry.setAllDay(true);
        provider.addEntry(allDayEntry);

        // 9. Timed entry (not all-day)
        Entry timedEntry = new Entry();
        timedEntry.setTitle("Timed Entry");
        timedEntry.setStart(LocalDateTime.of(2025, 3, 12, 9, 0));
        timedEntry.setEnd(LocalDateTime.of(2025, 3, 12, 10, 0));
        timedEntry.setAllDay(false);
        provider.addEntry(timedEntry);

        // 10. Non-editable entry (per-entry override)
        Entry notEditable = new Entry();
        notEditable.setTitle("Not Editable");
        notEditable.setStart(LocalDate.of(2025, 3, 14).atStartOfDay());
        notEditable.setAllDay(true);
        notEditable.setEditable(false);
        provider.addEntry(notEditable);

        // 11. Non-resizable entry (durationEditable=false)
        Entry noResize = new Entry();
        noResize.setTitle("No Resize");
        noResize.setStart(LocalDateTime.of(2025, 3, 14, 10, 0));
        noResize.setEnd(LocalDateTime.of(2025, 3, 14, 11, 0));
        noResize.setAllDay(false);
        noResize.setDurationEditable(false);
        provider.addEntry(noResize);

        // 12. Entry with extendedProps — verified via entryDidMount console.log
        Entry propsEntry = new Entry();
        propsEntry.setTitle("Has Props");
        propsEntry.setStart(LocalDate.of(2025, 3, 17).atStartOfDay());
        propsEntry.setAllDay(true);
        propsEntry.setCustomProperty("department", "Engineering");
        propsEntry.setCustomProperty("priority", "high");
        provider.addEntry(propsEntry);

        calendar.setEntryProvider(provider);

        // entryDidMount callback that logs extendedProps to a data attribute for E2E verification
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT,
                JsCallback.of("function(info) { " +
                "  var ep = info.event.extendedProps || {}; " +
                "  if (ep.customProperties && ep.customProperties.department) { " +
                "    info.el.setAttribute('data-department', ep.customProperties.department); " +
                "  } " +
                "}"));

        add(calendar);
        setFlexGrow(1, calendar);
    }
}
