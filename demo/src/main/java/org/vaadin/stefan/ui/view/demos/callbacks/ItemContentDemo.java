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

@Route(value = "callback-item-content", layout = MainLayout.class)
@MenuItem(label = "Item Content")
public class ItemContentDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        FullCalendar<Entry> calendar = FullCalendarBuilder.create().build();

        // Time grid looks best for itemContent demos — events have visible height
        calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);

        // itemContent: render custom HTML with icon + title + optional description
        calendar.setItemContentCallback(
                "function(info) {" +
                "  var icon = info.event.getCustomProperty('icon', '\uD83D\uDCC5');" +
                "  var desc = info.event.getCustomProperty('description', '');" +
                "  var html = '<div style=\"padding:2px;overflow:hidden\">';" +
                "  html += '<span style=\"margin-right:4px\">' + icon + '</span>';" +
                "  html += '<b>' + info.event.title + '</b>';" +
                "  if (desc) { html += '<br><small style=\"opacity:0.85\">' + desc + '</small>'; }" +
                "  html += '</div>';" +
                "  return { html: html };" +
                "}"
        );

        // Add entries with "icon" and "description" custom properties
        LocalDate today = LocalDate.now();
        // Find the Monday of the current week so entries land in the visible week
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);

        @SuppressWarnings("unchecked")
        InMemoryEntryProvider<Entry> provider =
                (InMemoryEntryProvider<Entry>) calendar.getCalendarItemProvider();

        Object[][] data = {
                {"Team Standup",        "\uD83D\uDC65", "Daily sync — 15 minutes",         1, 9,  0,  9,  15, "#3788d8"},
                {"Sprint Planning",     "\uD83D\uDCCB", "Plan next sprint backlog items",   1, 10, 0,  11, 30, "#e74c3c"},
                {"Lunch Break",         "\uD83C\uDF54", null,                               2, 12, 0,  13, 0,  "#2ecc71"},
                {"Architecture Review", "\uD83D\uDEE0", "Design the new payment module",    3, 14, 0,  15, 30, "#9b59b6"},
                {"Retrospective",       "\uD83D\uDD0D", "What went well? What to improve?", 4, 16, 0,  17, 0,  "#f39c12"},
                {"Release Prep",        "\uD83D\uDE80", "Final checks before go-live",      5, 11, 0,  12, 0,  "#e74c3c"},
        };

        for (Object[] row : data) {
            Entry entry = new Entry();
            entry.setTitle((String) row[0]);
            entry.setCustomProperty("icon", (String) row[1]);
            if (row[2] != null) {
                entry.setCustomProperty("description", (String) row[2]);
            }
            int day = (int) row[3];
            entry.setStart(monday.plusDays(day - 1).atTime((int) row[4], (int) row[5]));
            entry.setEnd(monday.plusDays(day - 1).atTime((int) row[6], (int) row[7]));
            entry.setColor((String) row[8]);
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
        return "Demonstrates the itemContent callback for rendering custom HTML inside calendar entries. "
                + "Each entry shows an icon, bold title, and an optional description line.";
    }
}
