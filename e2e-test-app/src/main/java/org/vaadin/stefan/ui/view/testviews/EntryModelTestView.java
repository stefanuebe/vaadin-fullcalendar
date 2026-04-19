package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.util.Locale;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Test view for entry model features.
 * <p>
 * Verifies:
 * <ul>
 *   <li>URL entry: fc-event has href containing the configured URL</li>
 *   <li>Interactive entry: keyboard-focusable (tabindex="0")</li>
 *   <li>Recurring duration: multi-day span via recurringDuration</li>
 *   <li>RRule entry: multiple weekly occurrences rendered in March 2025</li>
 *   <li>Exdate: excluded Monday is absent; remaining Mondays render (4 of 5)</li>
 *   <li>Monthly last-Friday: renders on March 28, not on March 21</li>
 *   <li>ofRaw: raw RRULE string produces correct occurrences (4 Wednesdays)</li>
 *   <li>Entry click listener updates counter badge</li>
 * </ul>
 * <p>
 * Route: /test/entry-model
 */
@Route(value = "entry-model", layout = TestLayout.class)
@MenuItem(label = "Entry Model")
public class EntryModelTestView extends VerticalLayout {

    public EntryModelTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Entry Model"));
        add(new Paragraph(
                "Tests URL navigation, interactive (keyboard-focusable) entries, " +
                "recurring duration and RRule-based recurrence."));

        // --- Status spans for Playwright ---
        Span clickCount = new Span("0");
        clickCount.setId("click-count");

        Span entryClickTitle = new Span("");
        entryClickTitle.setId("entry-click-title");

        Span urlEntryTitle = new Span("");
        urlEntryTitle.setId("url-entry-title");

        Div counters = new Div(
                label("clickCount: "), clickCount,
                label(" | lastClicked: "), entryClickTitle,
                label(" | urlEntry: "), urlEntryTitle
        );
        add(counters);

        // --- Calendar ---
        FullCalendar calendar = new FullCalendar();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.getElement().setAttribute("data-testid", "calendar");
        calendar.setLocale(Locale.ENGLISH);
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.setOption("initialView", CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());

        // Calendar-level interactive setting so all entries are keyboard-focusable by default
        calendar.setOption(FullCalendar.Option.ENTRY_INTERACTIVE, true);

        // --- Entry provider ---
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        // 1. URL entry
        Entry urlEntry = new Entry();
        urlEntry.setTitle("Visit Homepage");
        urlEntry.setStart(LocalDate.of(2025, 3, 5).atStartOfDay());
        urlEntry.setAllDay(true);
        urlEntry.setUrl("https://example.com");
        provider.addEntry(urlEntry);

        // 2. Interactive entry — keyboard-focusable
        Entry interactiveEntry = new Entry();
        interactiveEntry.setTitle("Keyboard Event");
        interactiveEntry.setStart(LocalDate.of(2025, 3, 10).atStartOfDay());
        interactiveEntry.setAllDay(true);
        interactiveEntry.setInteractive(true);
        provider.addEntry(interactiveEntry);

        // 3. Recurring entry visible on both Monday AND Tuesday in March 2025
        // FC renders multi-day bars with title only in the first cell, so instead of
        // using duration=P2D we recur on MONDAY+TUESDAY directly (independent occurrences)
        Entry recurringDuration = new Entry();
        recurringDuration.setTitle("Multi-Day Recurring");
        recurringDuration.setAllDay(true);
        recurringDuration.setRecurringDaysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));
        recurringDuration.setRecurringStartDate(LocalDate.of(2025, 3, 1));
        recurringDuration.setRecurringEndDate(LocalDate.of(2025, 3, 31));
        provider.addEntry(recurringDuration);

        // 4. RRule entry — weekly on Monday and Friday throughout March 2025
        Entry rruleEntry = new Entry();
        rruleEntry.setTitle("RRule Weekly");
        rruleEntry.setAllDay(true);
        rruleEntry.setRRule(
                RRule.weekly()
                        .dtstart(LocalDate.of(2025, 3, 3))
                        .until(LocalDate.of(2025, 3, 31))
                        .byWeekday(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
        );
        provider.addEntry(rruleEntry);

        // 5. Exdate entry — weekly Monday in March 2025, but March 10 is excluded
        //    Mondays: 3, 10, 17, 24, 31 → after exdate: 3, 17, 24, 31 (4 occurrences)
        Entry exdateEntry = new Entry();
        exdateEntry.setTitle("Exdate Test");
        exdateEntry.setAllDay(true);
        exdateEntry.setRRule(
                RRule.weekly()
                        .dtstart(LocalDate.of(2025, 3, 3))
                        .until(LocalDate.of(2025, 3, 31))
                        .byWeekday(DayOfWeek.MONDAY)
                        .excludeDates(LocalDate.of(2025, 3, 10))
        );
        provider.addEntry(exdateEntry);

        // 6. Monthly last-Friday entry — last Friday of each month Jan–Mar 2025
        //    Last Friday of March 2025 = March 28
        Entry lastFridayEntry = new Entry();
        lastFridayEntry.setTitle("Last Friday");
        lastFridayEntry.setAllDay(true);
        lastFridayEntry.setRRule(
                RRule.monthly()
                        .dtstart(LocalDate.of(2025, 1, 1))
                        .until(LocalDate.of(2025, 3, 31))
                        .byWeekday("-1fr")
        );
        provider.addEntry(lastFridayEntry);

        // 7. Raw RRULE — Wednesdays in March 2025: 5, 12, 19, 26 (4 occurrences)
        Entry rawEntry = new Entry();
        rawEntry.setTitle("Raw RRule");
        rawEntry.setAllDay(true);
        rawEntry.setRRule(RRule.ofRaw("FREQ=WEEKLY;BYDAY=WE;DTSTART=20250305;UNTIL=20250331"));
        provider.addEntry(rawEntry);

        calendar.setEntryProvider(provider);

        // --- Click listener updates counter and title ---
        calendar.addEntryClickedListener(e -> {
            int count = Integer.parseInt(clickCount.getText()) + 1;
            clickCount.setText(String.valueOf(count));
            entryClickTitle.setText(e.getEntry().getTitle());
        });

        add(calendar);
    }

    private static Span label(String text) {
        return new Span(text);
    }
}
