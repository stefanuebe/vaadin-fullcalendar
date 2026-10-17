package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.option.ToolbarValue;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * Test view for accessibility and touch improvements.
 * <p>
 * Verifies:
 * <ul>
 *   <li>Calendar renders in dayGridMonth view</li>
 *   <li>Events have tabindex="0" when eventInteractive is enabled</li>
 *   <li>Nav link day numbers are rendered when navLinks is enabled</li>
 *   <li>Toolbar buttons carry aria-label values from the today, prev and next hints</li>
 *   <li>"+N more" overflow link appears and carries the moreLinkHint aria-label</li>
 *   <li>Clicking an interactive event increments the click counter</li>
 * </ul>
 * <p>
 * Route: /test/accessibility
 */
@Route(value = "accessibility", layout = TestLayout.class)
@MenuItem(label = "Accessibility")
public class AccessibilityTouchTestView extends VerticalLayout {

    public AccessibilityTouchTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Accessibility and Touch"));
        add(new Paragraph(
                "Tests eventInteractive (tabindex), button hints (aria-label on toolbar buttons), " +
                "navLinkHint (aria-label on day numbers), and moreLinkHint (aria-label on +N more links)."));

        // --- Status span for Playwright ---
        Span clickCount = new Span("0");
        clickCount.setId("click-count");

        Div counters = new Div(label("clickCount: "), clickCount);
        add(counters);

        // --- Calendar ---
        FullCalendar calendar = new FullCalendar();
        calendar.getElement().setAttribute("data-testid", "calendar");

        calendar.options().setInitialDate(LocalDate.of(2025, 3, 1));
        calendar.options().setInitialView(CalendarViewImpl.DAY_GRID_MONTH);

        // Enable native toolbar so the button hints (TODAY_HINT, PREV_HINT, NEXT_HINT) have buttons to label.
        // The addon disables headerToolbar by default; this view needs it for its button hint tests.
        calendar.options().setHeaderToolbar(ToolbarValue.ofLeftRight("prev,next today", "title", ""));

        // Accessibility options
        calendar.options().setEntryInteractive(true);
        calendar.options().setNavLinks(true);
        // Limit event rows to 2 so the 5 events on 2025-03-05 reliably produce a "+N more" link
        calendar.options().setDayMaxEntryRows(2);
        calendar.options().setNavLinkHint("Open $0"); // not FullCalendar's default "Go to $0", so the test sees our value
        calendar.options().setMoreLinkHint("$0 more events. Click to expand");
        calendar.options().setTodayHint("Jump to today");
        calendar.options().setPrevHint("Go to previous $0");
        calendar.options().setNextHint("Go to next $0");

        // --- Entry provider ---
        // Five all-day events on 2025-03-05. In month view with default row limits
        // at least some of these will overflow and trigger the "+N more" link.
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        // All entries are keyboard-focusable via the calendar-level setEventInteractive(true);
        // no per-entry interactive override is needed here.
        Entry entryA = new Entry();
        entryA.setTitle("Interactive Event A");
        entryA.setStart(LocalDate.of(2025, 3, 5));
        entryA.setAllDay(true);
        provider.addEntry(entryA);

        Entry entryB = new Entry();
        entryB.setTitle("Interactive Event B");
        entryB.setStart(LocalDate.of(2025, 3, 5));
        entryB.setAllDay(true);
        provider.addEntry(entryB);

        Entry entryC = new Entry();
        entryC.setTitle("Interactive Event C");
        entryC.setStart(LocalDate.of(2025, 3, 5));
        entryC.setAllDay(true);
        provider.addEntry(entryC);

        Entry entryD = new Entry();
        entryD.setTitle("Interactive Event D");
        entryD.setStart(LocalDate.of(2025, 3, 5));
        entryD.setAllDay(true);
        provider.addEntry(entryD);

        Entry entryE = new Entry();
        entryE.setTitle("Interactive Event E");
        entryE.setStart(LocalDate.of(2025, 3, 5));
        entryE.setAllDay(true);
        provider.addEntry(entryE);

        calendar.setEntryProvider(provider);

        // --- Click listener updates counter ---
        calendar.addEntryClickedListener(e -> {
            int count = Integer.parseInt(clickCount.getText()) + 1;
            clickCount.setText(String.valueOf(count));
        });

        add(calendar);
    }

    private static Span label(String text) {
        return new Span(text);
    }
}
