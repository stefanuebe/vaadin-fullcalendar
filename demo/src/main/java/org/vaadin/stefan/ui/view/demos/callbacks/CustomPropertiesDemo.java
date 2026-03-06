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

@Route(value = "callback-custom-properties", layout = MainLayout.class)
@MenuItem(label = "Custom Properties")
public class CustomPropertiesDemo extends AbstractDemoView {

    // DEMO-START
    /**
     * Tracks the currently selected entry. Declared here (not as a field initializer)
     * because createCalendar() runs before subclass field initializers.
     */
    private Entry selected;

    @Override
    protected FullCalendar<?> createCalendar() {
        FullCalendar<Entry> calendar = FullCalendarBuilder.create().build();

        calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);

        // itemDidMount: apply border color based on "priority" and outline if selected
        calendar.setItemDidMountCallback(
                "function(info) {" +
                "  var priority = info.event.getCustomProperty('priority', 'low');" +
                "  var borderColor = priority === 'high' ? '#e74c3c'" +
                "                  : priority === 'medium' ? '#f39c12' : '#2ecc71';" +
                "  info.el.style.border = '3px solid ' + borderColor;" +
                "  info.el.style.boxSizing = 'border-box';" +
                "  if (info.event.getCustomProperty('selected', false)) {" +
                "    info.el.style.outline = '3px solid #3498db';" +
                "    info.el.style.outlineOffset = '1px';" +
                "  }" +
                "}"
        );

        // Click: toggle "selected" property, re-render via remove+add
        calendar.addCalendarItemClickedListener(event -> {
            @SuppressWarnings("unchecked")
            InMemoryEntryProvider<Entry> provider =
                    (InMemoryEntryProvider<Entry>) calendar.getCalendarItemProvider();

            Entry clicked = event.getItem();

            // Clear previous selection
            if (selected != null && !selected.getId().equals(clicked.getId())) {
                selected.setCustomProperty("selected", false);
                provider.removeEntry(selected);
                provider.addEntry(selected);
            }

            // Toggle selection on clicked entry
            boolean wasSelected = Boolean.TRUE.equals(clicked.getCustomProperty("selected"));
            clicked.setCustomProperty("selected", !wasSelected);
            selected = !wasSelected ? clicked : null;

            provider.removeEntry(clicked);
            provider.addEntry(clicked);
        });

        // Add sample entries with "priority" and "description" custom properties
        LocalDate today = LocalDate.now();
        @SuppressWarnings("unchecked")
        InMemoryEntryProvider<Entry> provider =
                (InMemoryEntryProvider<Entry>) calendar.getCalendarItemProvider();

        String[][] data = {
                {"Urgent Bug Fix",      "high",   "#e74c3c", "Critical production issue"},
                {"Sprint Planning",     "medium", "#f39c12", "Quarterly planning session"},
                {"Team Retrospective",  "medium", "#f39c12", "Reflect on last sprint outcomes"},
                {"Documentation",       "low",    "#2ecc71", "Update API documentation"},
                {"Code Review",         "low",    "#2ecc71", "Review open pull requests"},
        };

        for (int i = 0; i < data.length; i++) {
            Entry entry = new Entry();
            entry.setTitle(data[i][0]);
            entry.setCustomProperty("priority", data[i][1]);
            entry.setCustomProperty("description", data[i][3]);
            entry.setCustomProperty("selected", false);
            entry.setColor(data[i][2]);
            entry.setStart(today.plusDays(i * 2).atTime(10, 0));
            entry.setEnd(today.plusDays(i * 2).atTime(11, 0));
            provider.addEntry(entry);
        }

        // Pre-select the first entry
        provider.getEntries().stream().findFirst().ifPresent(first -> {
            first.setCustomProperty("selected", true);
            selected = first;
        });

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
        return "Demonstrates custom properties on calendar entries. Properties are defined server-side and "
                + "accessed in JavaScript callbacks for dynamic styling. Click an entry to toggle its selection highlight.";
    }
}
