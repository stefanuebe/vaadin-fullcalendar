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

@Route(value = "callback-item-will-unmount", layout = MainLayout.class)
@MenuItem(label = "Item WillUnmount")
public class ItemWillUnmountDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        FullCalendar<Entry> calendar = FullCalendarBuilder.create().build();

        calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);

        // itemDidMount: add a CSS fade-in animation so mount events are visually apparent
        calendar.setItemDidMountCallback(
                "function(info) {" +
                "  info.el.style.animation = 'fc-item-fadein 0.4s ease-in';" +
                "  if (!document.getElementById('fc-item-anim-style')) {" +
                "    var s = document.createElement('style');" +
                "    s.id = 'fc-item-anim-style';" +
                "    s.textContent = '@keyframes fc-item-fadein { from { opacity: 0; } to { opacity: 1; } }';" +
                "    document.head.appendChild(s);" +
                "  }" +
                "}"
        );

        // itemWillUnmount: log to the browser console when an entry leaves the DOM.
        // Switch views or navigate to a different period to trigger unmount events.
        calendar.setItemWillUnmountCallback(
                "function(info) {" +
                "  console.log('[ItemWillUnmount] Unmounting:', info.event.title," +
                "    '| id:', info.event.id," +
                "    '| at:', new Date().toISOString());" +
                "}"
        );

        // Add sample entries spread across this month
        LocalDate today = LocalDate.now();
        @SuppressWarnings("unchecked")
        InMemoryEntryProvider<Entry> provider =
                (InMemoryEntryProvider<Entry>) calendar.getCalendarItemProvider();

        Object[][] data = {
                {"Team Standup",        "#3788d8", 0},
                {"Sprint Planning",     "#e74c3c", 3},
                {"Architecture Review", "#9b59b6", 7},
                {"Demo Day",            "#f39c12", 10},
                {"Retrospective",       "#2ecc71", 14},
                {"Release Prep",        "#e74c3c", 17},
        };

        for (Object[] row : data) {
            Entry entry = new Entry();
            entry.setTitle((String) row[0]);
            entry.setColor((String) row[1]);
            int day = (int) row[2];
            entry.setStart(today.plusDays(day).atTime(10, 0));
            entry.setEnd(today.plusDays(day).atTime(11, 0));
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
        return "Demonstrates the itemWillUnmount callback, called before an entry element is removed from the DOM. "
                + "Open the browser console and switch views or navigate to a different period to see unmount events logged. "
                + "Entries also use itemDidMount to add a fade-in animation.";
    }
}
