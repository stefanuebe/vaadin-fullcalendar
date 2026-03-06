package org.vaadin.stefan.ui.view.demos.callbacks;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarItemProviderToolbar;

import java.time.LocalDate;

@Route(value = "callback-item-did-mount", layout = MainLayout.class)
@MenuItem(label = "Item DidMount")
public class ItemDidMountDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        FullCalendar<Entry> calendar = FullCalendarBuilder.create().build();

        calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);

        // itemDidMount: called after the entry element is added to the DOM.
        // Here we set a native browser tooltip from the "tooltip" custom property
        // and apply a pointer cursor to signal that the entry is interactive.
        calendar.setItemDidMountCallback(
                "function(info) {" +
                "  var tooltip = info.event.getCustomProperty('tooltip', '');" +
                "  if (tooltip) {" +
                "    info.el.title = tooltip;" +
                "  }" +
                "  info.el.style.cursor = 'pointer';" +
                "}"
        );

        // Add entries with descriptive tooltip text
        LocalDate today = LocalDate.now();
        @SuppressWarnings("unchecked")
        InMemoryEntryProvider<Entry> provider =
                (InMemoryEntryProvider<Entry>) calendar.getCalendarItemProvider();

        Object[][] data = {
                {"Team Standup",       "#3788d8", 0, "Daily 15-min sync. Agenda: blockers + updates."},
                {"Sprint Planning",    "#e74c3c", 2, "Full team planning — bring your backlog items ready."},
                {"Architecture Review","#9b59b6", 4, "Review the proposed microservice split with tech leads."},
                {"Retrospective",      "#f39c12", 6, "End-of-sprint retro. What went well? What to improve?"},
                {"Release Day",        "#2ecc71", 8, "Production deployment window: 22:00 - 23:00 UTC."},
        };

        for (Object[] row : data) {
            Entry entry = new Entry();
            entry.setTitle((String) row[0]);
            entry.setColor((String) row[1]);
            int day = (int) row[2];
            entry.setStart(today.plusDays(day).atTime(10, 0));
            entry.setEnd(today.plusDays(day).atTime(11, 0));
            entry.setCustomProperty("tooltip", (String) row[3]);
            provider.addEntry(entry);
        }

        return calendar;
    }

    @Override
    protected Component createToolbar() {
        return CalendarItemProviderToolbar.builder()
                .calendar(getCalendar())
                .dateChangeable(true)
                .viewChangeable(true)
                .build();
    }
    // DEMO-END

    @Override
    protected String createDescription() {
        return "Demonstrates the itemDidMount callback, called after an entry is rendered in the DOM. "
                + "Hover over any entry to see the native browser tooltip populated from a custom property.";
    }
}
