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

import java.time.LocalDate;
import java.util.Locale;

/**
 * Test view for verifying listener firing and event data.
 * <p>
 * Verifies Patterns 3+4:
 * <ul>
 *   <li>Pattern 3: Server-side listeners are notified from client interactions</li>
 *   <li>Pattern 4: Events contain correct data (title, dates, etc.)</li>
 * </ul>
 * <p>
 * Listeners tested:
 * DatesRenderedEvent, MoreLinkClickedEvent, TimeslotClickedEvent,
 * EntryClickedEvent (with data), EntryMouseEnterEvent, EntryMouseLeaveEvent.
 * <p>
 * Route: /test/listener-data
 */
@Route(value = "listener-data", layout = TestLayout.class)
@MenuItem(label = "Listener Data")
public class ListenerDataTestView extends VerticalLayout {

    public ListenerDataTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Listener Data"));
        add(new Paragraph("Tests that server-side listeners fire and contain correct event data."));

        // --- Badges ---

        // DatesRenderedEvent
        Span datesRenderedCount = badge("dates-rendered-count", "0");
        Span datesIntervalStart = badge("dates-interval-start", "");
        Span datesIntervalEnd = badge("dates-interval-end", "");

        // MoreLinkClickedEvent
        Span moreLinkCount = badge("more-link-count", "0");
        Span moreLinkDate = badge("more-link-date", "");

        // TimeslotClickedEvent
        Span timeslotClickCount = badge("timeslot-click-count", "0");
        Span timeslotClickDate = badge("timeslot-click-date", "");
        Span timeslotClickAllDay = badge("timeslot-click-allday", "");

        // EntryClickedEvent (with data)
        Span entryClickCount = badge("entry-click-count", "0");
        Span entryClickTitle = badge("entry-click-title", "");
        Span entryClickStart = badge("entry-click-start", "");

        // EntryMouseEnterEvent
        Span mouseEnterCount = badge("mouse-enter-count", "0");
        Span mouseEnterTitle = badge("mouse-enter-title", "");

        // EntryMouseLeaveEvent
        Span mouseLeaveCount = badge("mouse-leave-count", "0");

        // TimeslotsSelectedEvent
        Span timeslotsSelectedCount = badge("timeslots-selected-count", "0");
        Span timeslotsSelectedStart = badge("timeslots-selected-start", "");
        Span timeslotsSelectedEnd = badge("timeslots-selected-end", "");
        Span timeslotsSelectedAllDay = badge("timeslots-selected-allday", "");

        // DayNumberClickedEvent
        Span dayNumberCount = badge("day-number-count", "0");
        Span dayNumberDate = badge("day-number-date", "");

        // WeekNumberClickedEvent
        Span weekNumberCount = badge("week-number-count", "0");
        Span weekNumberDate = badge("week-number-date", "");

        // BrowserTimezoneObtainedEvent
        Span browserTzCount = badge("browser-tz-count", "0");
        Span browserTzValue = badge("browser-tz-value", "");

        Div counters = new Div(
                label("datesRendered: "), datesRenderedCount,
                label(" | intervalStart: "), datesIntervalStart,
                label(" | intervalEnd: "), datesIntervalEnd,
                label(" | moreLink: "), moreLinkCount,
                label(" | moreLinkDate: "), moreLinkDate,
                label(" | timeslotClick: "), timeslotClickCount,
                label(" | tsDate: "), timeslotClickDate,
                label(" | tsAllDay: "), timeslotClickAllDay,
                label(" | entryClick: "), entryClickCount,
                label(" | clickTitle: "), entryClickTitle,
                label(" | clickStart: "), entryClickStart,
                label(" | mouseEnter: "), mouseEnterCount,
                label(" | enterTitle: "), mouseEnterTitle,
                label(" | mouseLeave: "), mouseLeaveCount,
                label(" | tsSelected: "), timeslotsSelectedCount,
                label(" | selStart: "), timeslotsSelectedStart,
                label(" | selEnd: "), timeslotsSelectedEnd,
                label(" | selAllDay: "), timeslotsSelectedAllDay,
                label(" | dayNum: "), dayNumberCount,
                label(" | dayNumDate: "), dayNumberDate,
                label(" | weekNum: "), weekNumberCount,
                label(" | weekNumDate: "), weekNumberDate,
                label(" | browserTz: "), browserTzCount,
                label(" | tzValue: "), browserTzValue
        );
        counters.getStyle().set("font-size", "12px").set("word-wrap", "break-word");
        add(counters);

        // --- Calendar ---
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setLocale(Locale.ENGLISH);
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.setOption("initialView", CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());
        calendar.setOption(FullCalendar.Option.DAY_MAX_EVENT_ROWS, 2); // triggers +more link
        calendar.setOption(FullCalendar.Option.NAV_LINKS, true);
        calendar.setOption(FullCalendar.Option.SELECTABLE, true);
        calendar.setOption(FullCalendar.Option.WEEK_NUMBERS, true);

        // --- Entries ---
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        // Regular entry for click testing
        Entry clickable = new Entry();
        clickable.setTitle("Clickable Event");
        clickable.setStart(LocalDate.of(2025, 3, 5).atStartOfDay());
        clickable.setAllDay(true);
        provider.addEntry(clickable);

        // Hover entry for mouse enter/leave testing
        Entry hoverable = new Entry();
        hoverable.setTitle("Hover Me");
        hoverable.setStart(LocalDate.of(2025, 3, 7).atStartOfDay());
        hoverable.setAllDay(true);
        provider.addEntry(hoverable);

        // 5 entries on March 10 to trigger +more link (with dayMaxEventRows=2)
        for (int i = 1; i <= 5; i++) {
            Entry overflow = new Entry();
            overflow.setTitle("Overflow " + i);
            overflow.setStart(LocalDate.of(2025, 3, 10).atStartOfDay());
            overflow.setAllDay(true);
            provider.addEntry(overflow);
        }

        calendar.setEntryProvider(provider);

        // --- Listeners ---

        // DatesRenderedEvent
        calendar.addDatesRenderedListener(e -> {
            int count = Integer.parseInt(datesRenderedCount.getText()) + 1;
            datesRenderedCount.setText(String.valueOf(count));
            datesIntervalStart.setText(e.getIntervalStart().toString());
            datesIntervalEnd.setText(e.getIntervalEnd().toString());
        });

        // MoreLinkClickedEvent
        calendar.addMoreLinkClickedListener(e -> {
            int count = Integer.parseInt(moreLinkCount.getText()) + 1;
            moreLinkCount.setText(String.valueOf(count));
            moreLinkDate.setText(e.getClickedDate().toString());
        });

        // TimeslotClickedEvent
        calendar.addTimeslotClickedListener(e -> {
            int count = Integer.parseInt(timeslotClickCount.getText()) + 1;
            timeslotClickCount.setText(String.valueOf(count));
            timeslotClickDate.setText(e.getDateTime().toLocalDate().toString());
            timeslotClickAllDay.setText(String.valueOf(e.isAllDay()));
        });

        // EntryClickedEvent — with data
        calendar.addEntryClickedListener(e -> {
            int count = Integer.parseInt(entryClickCount.getText()) + 1;
            entryClickCount.setText(String.valueOf(count));
            entryClickTitle.setText(e.getEntry().getTitle());
            var start = e.getEntry().getStart();
            entryClickStart.setText(start != null ? start.toLocalDate().toString() : "null");
        });

        // EntryMouseEnterEvent
        calendar.addEntryMouseEnterListener(e -> {
            int count = Integer.parseInt(mouseEnterCount.getText()) + 1;
            mouseEnterCount.setText(String.valueOf(count));
            mouseEnterTitle.setText(e.getEntry().getTitle());
        });

        // EntryMouseLeaveEvent
        calendar.addEntryMouseLeaveListener(e -> {
            int count = Integer.parseInt(mouseLeaveCount.getText()) + 1;
            mouseLeaveCount.setText(String.valueOf(count));
        });

        // TimeslotsSelectedEvent
        calendar.addTimeslotsSelectedListener(e -> {
            int count = Integer.parseInt(timeslotsSelectedCount.getText()) + 1;
            timeslotsSelectedCount.setText(String.valueOf(count));
            timeslotsSelectedStart.setText(e.getStart().toLocalDate().toString());
            timeslotsSelectedEnd.setText(e.getEnd().toLocalDate().toString());
            timeslotsSelectedAllDay.setText(String.valueOf(e.isAllDay()));
        });

        // DayNumberClickedEvent
        calendar.addDayNumberClickedListener(e -> {
            int count = Integer.parseInt(dayNumberCount.getText()) + 1;
            dayNumberCount.setText(String.valueOf(count));
            dayNumberDate.setText(e.getDate().toString());
        });

        // WeekNumberClickedEvent
        calendar.addWeekNumberClickedListener(e -> {
            int count = Integer.parseInt(weekNumberCount.getText()) + 1;
            weekNumberCount.setText(String.valueOf(count));
            weekNumberDate.setText(e.getDate().toString());
        });

        // BrowserTimezoneObtainedEvent
        calendar.addBrowserTimezoneObtainedListener(e -> {
            int count = Integer.parseInt(browserTzCount.getText()) + 1;
            browserTzCount.setText(String.valueOf(count));
            browserTzValue.setText(e.getTimezone().getClientSideValue());
        });

        add(calendar);
        setFlexGrow(1, calendar);
    }

    private static Span badge(String id, String initialText) {
        Span span = new Span(initialText);
        span.setId(id);
        return span;
    }

    private static Span label(String text) {
        return new Span(text);
    }
}
