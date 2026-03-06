package org.vaadin.stefan.ui.view.demos.callbacks;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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

@Route(value = "callback-item-classnames", layout = MainLayout.class)
@MenuItem(label = "Item ClassNames")
public class ItemClassNamesDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        FullCalendar<Entry> calendar = FullCalendarBuilder.create().build();

        calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);

        // itemClassNames: return CSS class based on "category" custom property
        calendar.setItemClassNamesCallback(
                "function(arg) {" +
                "  var cat = arg.event.getCustomProperty('category', 'default');" +
                "  return ['fc-category-' + cat];" +
                "}"
        );

        // Inject category CSS rules after attach so the page is ready
        calendar.addAttachListener(ev ->
                UI.getCurrent().getPage().executeJs(
                        "var style = document.createElement('style');" +
                        "style.textContent = " +
                        "  '.fc-category-meeting  { font-weight: bold; border-radius: 6px !important; }' +" +
                        "  '.fc-category-deadline { text-decoration: underline; border-style: dashed !important; }' +" +
                        "  '.fc-category-personal { opacity: 0.75; font-style: italic; }' +" +
                        "  '.fc-category-default  { opacity: 0.6; }';" +
                        "document.head.appendChild(style);"
                )
        );

        // Add entries with different categories
        LocalDate today = LocalDate.now();
        @SuppressWarnings("unchecked")
        InMemoryEntryProvider<Entry> provider =
                (InMemoryEntryProvider<Entry>) calendar.getCalendarItemProvider();

        Object[][] data = {
                {"Team Standup",        "meeting",  "#3788d8", 0, 9,  9,  30},
                {"Release Deadline",    "deadline", "#e74c3c", 1, 17, 17, 0},
                {"Doctor Appointment",  "personal", "#9b59b6", 2, 14, 15, 0},
                {"Sprint Review",       "meeting",  "#3788d8", 3, 11, 12, 0},
                {"Submit Report",       "deadline", "#e74c3c", 4, 10, 10, 30},
                {"Gym Session",         "personal", "#9b59b6", 5, 7,  8,  0},
        };

        for (Object[] row : data) {
            Entry entry = new Entry();
            entry.setTitle((String) row[0]);
            entry.setCustomProperty("category", (String) row[1]);
            entry.setColor((String) row[2]);
            int day = (int) row[3];
            entry.setStart(today.plusDays(day).atTime((int) row[4], (int) row[5]));
            entry.setEnd(today.plusDays(day).atTime((int) row[6], (int) row[7]));
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
        return "Demonstrates the itemClassNames callback. CSS classes are dynamically assigned based on "
                + "entry properties, enabling category-based styling (bold for meetings, underline for deadlines, "
                + "italic for personal events).";
    }
}
