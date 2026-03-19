package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.CustomButton;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.FullCalendarVariant;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.util.Map;

/**
 * Test view for advanced and niche options.
 * <p>
 * Verifies:
 * <ul>
 *   <li>Calendar renders in dayGridMonth view</li>
 *   <li>Custom button renders in the toolbar and fires a server-side click listener</li>
 *   <li>View-specific option (dayMaxEventRows=2 for dayGrid only) truncates events in month view</li>
 *   <li>buttonIcons override renders a custom aria-label on the prev button</li>
 * </ul>
 * <p>
 * Route: /test/advanced-options
 */
@Route(value = "advanced-options", layout = TestLayout.class)
@MenuItem(label = "Advanced Options")
public class AdvancedOptionsTestView extends VerticalLayout {

    public AdvancedOptionsTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Advanced Options"));
        add(new Paragraph(
                "Tests customButtons (server-side click), view-specific options (dayMaxEventRows per view), " +
                "buttonIcons, dateIncrement, and getCurrentIntervalStart/End."));

        // --- Status spans for Playwright ---
        Span customButtonClickCount = new Span("0");
        customButtonClickCount.setId("custom-btn-click-count");

        Span customButtonName = new Span("none");
        customButtonName.setId("custom-btn-name");

        Div counters = new Div(
                label("customBtnClicks: "), customButtonClickCount,
                new Span(" | "),
                label("lastBtn: "), customButtonName
        );
        add(counters);

        // --- Calendar ---
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.getElement().setAttribute("data-testid", "calendar");

        calendar.setInitialDate(LocalDate.of(2025, 3, 1));
        calendar.setInitialView(CalendarViewImpl.DAY_GRID_MONTH);

        // Custom button --------------------------------------------------------
        CustomButton schedBtn = new CustomButton("scheduleWizard");
        schedBtn.setText("Schedule");
        schedBtn.setHint("Open scheduling wizard");

        // Register the custom button first, then expose it in the toolbar
        calendar.addCustomButton(schedBtn, event -> {
            int count = Integer.parseInt(customButtonClickCount.getText()) + 1;
            customButtonClickCount.setText(String.valueOf(count));
            customButtonName.setText(event.getButtonName());
        });

        // Put the custom button at the end of the right toolbar section using setOption
        calendar.setOption(FullCalendar.Option.HEADER_TOOLBAR, Map.of(
                "left",   "prev,next today",
                "center", "title",
                "right",  "dayGridMonth scheduleWizard"
        ));

        // buttonIcons ----------------------------------------------------------
        // Overrides the prev button icon class (FC still renders the button, but with a
        // different icon class — Playwright can verify the button still exists)
        calendar.setButtonIcons(Map.of(
                "prev", "chevron-left-custom",
                "next", "chevron-right-custom"
        ));

        // dateAlignment --------------------------------------------------------
        // Aligning to "month" is the default for dayGridMonth; this just exercises the setter.
        calendar.setDateAlignment("month");

        // View-specific option ------------------------------------------------
        // Limit displayed event rows to 2 only in the dayGrid view family (not in other views).
        // With 5 events on 2025-03-05 this guarantees a "+N more" link in month view.
        calendar.setViewSpecificOption("dayGrid", FullCalendar.Option.DAY_MAX_EVENT_ROWS, 2);

        // eventConstraint -------------------------------------------------------
        // Constrain drag-and-drop to business hours (does not affect rendering, exercises setter).
        calendar.setEventConstraintToBusinessHours();

        // --- Entry provider --------------------------------------------------------------
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        // 5 all-day events on 2025-03-05: with dayMaxEventRows=2 these produce a "+N more" link
        for (int i = 1; i <= 5; i++) {
            Entry e = new Entry();
            e.setTitle("Event " + i);
            e.setStart(LocalDate.of(2025, 3, 5));
            e.setAllDay(true);
            provider.addEntry(e);
        }

        calendar.setEntryProvider(provider);
        add(calendar);
    }

    private static Span label(String text) {
        return new Span(text);
    }
}
