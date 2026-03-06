package org.vaadin.stefan.ui.view.demos.basic;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarItemProviderToolbar;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Route(value = "native-event-listeners", layout = MainLayout.class)
@MenuItem(label = "Native Event Listeners")
public class NativeEventListenersDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        LocalDate today = LocalDate.now();

        FullCalendar<Entry> calendar = FullCalendarBuilder.<Entry>create()
                .withInitialEntries(List.of(
                        createEntry("Team Standup",
                                today.atTime(9, 0), today.atTime(9, 30), "#3788d8"),
                        createEntry("Sprint Planning",
                                today.plusDays(1).atTime(10, 0), today.plusDays(1).atTime(11, 30), "#e74c3c"),
                        createEntry("Lunch Meeting",
                                today.plusDays(2).atTime(12, 0), today.plusDays(2).atTime(13, 0), "#2ecc71"),
                        createEntry("Code Review",
                                today.plusDays(3).atTime(14, 0), today.plusDays(3).atTime(15, 0), "#9b59b6"),
                        createEntry("Friday Wrap-up",
                                today.plusDays(4).atTime(16, 0), today.plusDays(4).atTime(17, 0), "#f39c12")
                ))
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);

        // mouseover: fade the entry element to 50% opacity
        calendar.addItemNativeEventListener("mouseover",
                "e => info.el.style.opacity = '0.5'");

        // mouseout: restore full opacity
        calendar.addItemNativeEventListener("mouseout",
                "e => info.el.style.opacity = ''");

        // contextmenu: prevent browser menu and notify the server with the entry id
        calendar.addItemNativeEventListener("contextmenu",
                "e => { e.preventDefault(); this.el.parentElement.$server.onContextMenu(info.event.id); }");

        return calendar;
    }

    private Entry createEntry(String title,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end,
            String color) {
        Entry entry = new Entry(UUID.randomUUID().toString());
        entry.setTitle(title);
        entry.setStart(start);
        entry.setEnd(end);
        entry.setColor(color);
        return entry;
    }

    @ClientCallable
    public void onContextMenu(String id) {
        Notification.show("Right-clicked entry id: " + id, 3000, Notification.Position.BOTTOM_START);
    }
    // DEMO-END

    @Override
    protected Component createToolbar() {
        return CalendarItemProviderToolbar.builder()
                .calendar(getCalendar())
                .dateChangeable(true)
                .viewChangeable(true)
                .settingsAvailable(true)
                .build();
    }

    @Override
    protected String createDescription() {
        return "Native event listeners let you react to browser-level events on calendar items"
                + " — like mouse hover, right-click, or touch events. Hover over an entry to see it"
                + " fade, or right-click for a context menu notification. These are powered by"
                + " FullCalendar's eventDidMount callback.";
    }
}
