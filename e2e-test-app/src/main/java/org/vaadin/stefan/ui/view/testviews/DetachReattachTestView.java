package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
 * Test view for verifying calendar state preservation across detach/reattach
 * and setVisible(false)/setVisible(true) cycles.
 * <p>
 * State that must survive:
 * <ul>
 *   <li>Entries remain visible</li>
 *   <li>Current view is preserved (e.g. timeGridWeek)</li>
 *   <li>Navigated date is preserved (not reset to initialDate)</li>
 *   <li>Options are restored (e.g. locale, editable)</li>
 *   <li>Event listeners still work after reattach</li>
 * </ul>
 * <p>
 * Route: /test/detach-reattach
 */
@Route(value = "detach-reattach", layout = TestLayout.class)
@MenuItem(label = "Detach/Reattach")
public class DetachReattachTestView extends VerticalLayout {

    private final FullCalendar calendar;
    private final Div calendarContainer;

    public DetachReattachTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Detach / Reattach"));
        add(new Paragraph("Tests that calendar state survives detach/reattach and visibility toggles."));

        // --- Badges ---
        Span clickCount = new Span("0");
        clickCount.setId("click-count");
        Span clickTitle = new Span("");
        clickTitle.setId("click-title");
        Span datesRenderedCount = new Span("0");
        datesRenderedCount.setId("dates-rendered-count");
        Span intervalStart = new Span("");
        intervalStart.setId("interval-start");

        Div badges = new Div(
                label("clicks: "), clickCount,
                label(" | title: "), clickTitle,
                label(" | datesRendered: "), datesRenderedCount,
                label(" | intervalStart: "), intervalStart
        );
        badges.getStyle().set("font-size", "12px");
        add(badges);

        // --- Action buttons ---
        Button detachBtn = new Button("Detach");
        detachBtn.setId("btn-detach");
        Button reattachBtn = new Button("Reattach");
        reattachBtn.setId("btn-reattach");
        Button hideBtn = new Button("Hide");
        hideBtn.setId("btn-hide");
        Button showBtn = new Button("Show");
        showBtn.setId("btn-show");
        Button switchViewBtn = new Button("Switch to TimeGrid Week");
        switchViewBtn.setId("btn-switch-view");
        Button navigateNextBtn = new Button("Navigate Next");
        navigateNextBtn.setId("btn-navigate-next");

        HorizontalLayout buttons = new HorizontalLayout(detachBtn, reattachBtn, hideBtn, showBtn, switchViewBtn, navigateNextBtn);
        add(buttons);

        // --- Calendar ---
        calendar = new FullCalendar();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setLocale(Locale.ENGLISH);
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.setOption("initialView", CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());
        calendar.setOption(FullCalendar.Option.EDITABLE, true);
        calendar.setOption(FullCalendar.Option.WEEK_NUMBERS, true);

        // --- Entries ---
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        Entry entry1 = new Entry();
        entry1.setTitle("Surviving Entry");
        entry1.setStart(LocalDate.of(2025, 3, 5).atStartOfDay());
        entry1.setAllDay(true);
        entry1.setColor("green");
        provider.addEntry(entry1);

        Entry entry2 = new Entry();
        entry2.setTitle("Timed Entry");
        entry2.setStart(LocalDateTime.of(2025, 3, 5, 9, 0));
        entry2.setEnd(LocalDateTime.of(2025, 3, 5, 10, 0));
        provider.addEntry(entry2);

        calendar.setEntryProvider(provider);

        // --- Listeners ---
        calendar.addEntryClickedListener(e -> {
            int count = Integer.parseInt(clickCount.getText()) + 1;
            clickCount.setText(String.valueOf(count));
            clickTitle.setText(e.getEntry().getTitle());
        });

        calendar.addDatesRenderedListener(e -> {
            int count = Integer.parseInt(datesRenderedCount.getText()) + 1;
            datesRenderedCount.setText(String.valueOf(count));
            intervalStart.setText(e.getIntervalStart().toString());
        });

        // --- Calendar container (for detach/reattach) ---
        calendarContainer = new Div();
        calendarContainer.setId("calendar-container");
        calendarContainer.setSizeFull();
        calendarContainer.add(calendar);
        add(calendarContainer);
        setFlexGrow(1, calendarContainer);

        // --- Button handlers ---
        detachBtn.addClickListener(e -> calendarContainer.remove(calendar));
        reattachBtn.addClickListener(e -> {
            if (calendar.getParent().isEmpty()) {
                calendarContainer.add(calendar);
            }
        });
        hideBtn.addClickListener(e -> calendar.setVisible(false));
        showBtn.addClickListener(e -> calendar.setVisible(true));
        switchViewBtn.addClickListener(e -> calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK));
        navigateNextBtn.addClickListener(e -> calendar.next());
    }

    private static Span label(String text) {
        return new Span(text);
    }
}
