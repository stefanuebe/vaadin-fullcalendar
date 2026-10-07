package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
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
 *   <li>color / contrastColor — CSS background + text color</li>
 *   <li>displayMode BACKGROUND — fc-bg-event class</li>
 *   <li>displayMode INVERSE_BACKGROUND — fc-bg-event class</li>
 *   <li>displayMode NONE — entry not visible</li>
 *   <li>classNames — custom CSS class on fc-event</li>
 *   <li>allDay true/false — placement in day-events vs timed area</li>
 *   <li>editable=false per-entry — no drag when calendar is editable</li>
 *   <li>durationEditable=false — no resize handle</li>
 *   <li>extended props — readable in render hooks under extendedProps, removable</li>
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
        FullCalendar calendar = new FullCalendar();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setOption(Option.LOCALE, Locale.ENGLISH);
        calendar.setOption(Option.INITIAL_DATE, LocalDate.of(2025, 3, 1));
        calendar.setOption(Option.INITIAL_VIEW, CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());
        calendar.setOption(Option.EDITABLE, true);

        // --- Entry provider ---
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        // 1. Color entry — red
        Entry redEntry = new Entry();
        redEntry.setTitle("Red Entry");
        redEntry.setStart(LocalDate.of(2025, 3, 3).atStartOfDay());
        redEntry.setAllDay(true);
        redEntry.setColor("red");
        provider.addEntry(redEntry);

        // 2. Custom color + contrast color
        Entry customColor = new Entry();
        customColor.setTitle("Custom Color");
        customColor.setStart(LocalDate.of(2025, 3, 4).atStartOfDay());
        customColor.setAllDay(true);
        customColor.setColor("#00ff00");
        customColor.setContrastColor("#ffffff");
        provider.addEntry(customColor);

        // 3. DisplayMode BACKGROUND
        Entry bgMode = new Entry();
        bgMode.setTitle("Background Mode");
        bgMode.setStart(LocalDate.of(2025, 3, 6).atStartOfDay());
        bgMode.setAllDay(true);
        bgMode.setDisplayMode(DisplayMode.BACKGROUND);
        bgMode.setColor("orange");
        provider.addEntry(bgMode);

        // 4. DisplayMode INVERSE_BACKGROUND
        Entry inverseBg = new Entry();
        inverseBg.setTitle("Inverse BG Mode");
        inverseBg.setStart(LocalDate.of(2025, 3, 7).atStartOfDay());
        inverseBg.setAllDay(true);
        inverseBg.setDisplayMode(DisplayMode.INVERSE_BACKGROUND);
        inverseBg.setColor("purple");
        provider.addEntry(inverseBg);

        // 5. DisplayMode NONE — hidden
        Entry hiddenEntry = new Entry();
        hiddenEntry.setTitle("Hidden Entry");
        hiddenEntry.setStart(LocalDate.of(2025, 3, 8).atStartOfDay());
        hiddenEntry.setAllDay(true);
        hiddenEntry.setDisplayMode(DisplayMode.NONE);
        provider.addEntry(hiddenEntry);

        // 6. Custom classNames
        Entry classEntry = new Entry();
        classEntry.setTitle("Custom Class");
        classEntry.setStart(LocalDate.of(2025, 3, 10).atStartOfDay());
        classEntry.setAllDay(true);
        classEntry.setClassNames(Set.of("my-custom-class"));
        provider.addEntry(classEntry);

        // 7. All-day entry
        Entry allDayEntry = new Entry();
        allDayEntry.setTitle("All-Day Entry");
        allDayEntry.setStart(LocalDate.of(2025, 3, 12).atStartOfDay());
        allDayEntry.setAllDay(true);
        provider.addEntry(allDayEntry);

        // 8. Timed entry (not all-day)
        Entry timedEntry = new Entry();
        timedEntry.setTitle("Timed Entry");
        timedEntry.setStart(LocalDateTime.of(2025, 3, 12, 9, 0));
        timedEntry.setEnd(LocalDateTime.of(2025, 3, 12, 10, 0));
        timedEntry.setAllDay(false);
        provider.addEntry(timedEntry);

        // 9. Non-editable entry (per-entry override)
        Entry notEditable = new Entry();
        notEditable.setTitle("Not Editable");
        notEditable.setStart(LocalDate.of(2025, 3, 14).atStartOfDay());
        notEditable.setAllDay(true);
        notEditable.setEditable(false);
        provider.addEntry(notEditable);

        // 10. Non-resizable entry (durationEditable=false)
        Entry noResize = new Entry();
        noResize.setTitle("No Resize");
        noResize.setStart(LocalDateTime.of(2025, 3, 14, 10, 0));
        noResize.setEnd(LocalDateTime.of(2025, 3, 14, 11, 0));
        noResize.setAllDay(false);
        noResize.setDurationEditable(false);
        provider.addEntry(noResize);

        // 11. Entry with extended props, read by the render hooks below
        Entry propsEntry = new Entry();
        propsEntry.setTitle("Has Props");
        propsEntry.setStart(LocalDate.of(2025, 3, 17).atStartOfDay());
        propsEntry.setAllDay(true);
        propsEntry.setExtendedProp("department", "Engineering");
        propsEntry.setExtendedProp("priority", "high");
        provider.addEntry(propsEntry);

        // 12. Entry with most fields set besides its extended props. None of these fields may end up in
        // extendedProps, where it would overwrite an extended prop of the same name.
        Entry allFields = new Entry();
        allFields.setTitle("All Fields");
        allFields.setGroupId("group");
        allFields.setStart(LocalDateTime.of(2025, 3, 19, 9, 0));
        allFields.setEnd(LocalDateTime.of(2025, 3, 19, 10, 0));
        allFields.setAllDay(false);
        allFields.setColor("teal");
        allFields.setContrastColor("white");
        allFields.setClassNames(Set.of("all-fields"));
        allFields.setEditable(true);
        allFields.setStartEditable(true);
        allFields.setDurationEditable(true);
        allFields.setOverlap(false);
        allFields.setConstraint("businessHours");
        allFields.setInteractive(true);
        allFields.setUrl("#all-fields");
        allFields.setDisplayMode(DisplayMode.BLOCK);
        allFields.setExtendedProp("department", "Engineering");
        provider.addEntry(allFields);

        calendar.setEntryProvider(provider);

        // entryDidMount writes extended props to data attributes for E2E verification
        calendar.setOption(Option.ENTRY_DID_MOUNT,
                JsCallback.of("function(info) { " +
                "  var ep = info.event.extendedProps; " +
                "  if (ep.department) { " +
                "    info.el.setAttribute('data-department', ep.department); " +
                "  } " +
                "  info.el.setAttribute('data-extended-keys', Object.keys(ep).sort().join(',')); " +
                "}"));

        // a FullCalendar 7 split class hook, run again on every render
        calendar.setOption("eventTitleClass",
                JsCallback.of("info => 'dept-' + (info.event.extendedProps.department || 'none')"));

        Button removeDepartment = new Button("Remove department", e -> {
            propsEntry.removeExtendedProp("department");
            provider.refreshItem(propsEntry);
        });
        removeDepartment.setId("remove-department");
        add(removeDepartment);

        add(calendar);
        setFlexGrow(1, calendar);
    }
}
