package org.vaadin.stefan.ui.view.demos.calendaritemprovider;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.CalendarItemProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demonstrates CIP with an immutable record and the CalendarItemUpdateHandler.
 * Shows that immutable objects work with CIP by creating new instances on update.
 */
@Route(value = "cip-update-handler-demo", layout = MainLayout.class)
@MenuItem(label = "CIP Update Handler Demo")
public class CalendarItemProviderUpdateHandlerDemo extends VerticalLayout {

    // Immutable appointment record
    public record Appointment(String id, String title, LocalDateTime start, LocalDateTime end, String color) {}

    private final Map<String, Appointment> store = new ConcurrentHashMap<>();

    public CalendarItemProviderUpdateHandlerDemo() {
        setSizeFull();

        // Seed data
        LocalDate today = LocalDate.now();
        addToStore(new Appointment("1", "Board Meeting", today.atTime(9, 0), today.atTime(10, 30), "#3788d8"));
        addToStore(new Appointment("2", "Investor Call", today.atTime(14, 0), today.atTime(15, 0), "#e74c3c"));
        addToStore(new Appointment("3", "Team Lunch", today.plusDays(1).atTime(12, 0), today.plusDays(1).atTime(13, 0), "#2ecc71"));
        addToStore(new Appointment("4", "Strategy Session", today.plusDays(2).atTime(10, 0), today.plusDays(2).atTime(12, 0), "#9b59b6"));

        // Read-only mapper — no setters (records are immutable)
        var mapper = CalendarItemPropertyMapper.of(Appointment.class)
                .id(Appointment::id)
                .title(Appointment::title)
                .start(Appointment::start)
                .end(Appointment::end)
                .color(Appointment::color);

        // Callback provider
        var provider = CalendarItemProvider.fromCallbacks(
                query -> store.values().stream(),
                id -> store.get(id)
        );

        // Build calendar
        FullCalendar<Appointment> calendar = FullCalendarBuilder.<Appointment>create(Appointment.class)
                .withCalendarItemProvider(provider, mapper)
                .withCalendarItemLimit(3)
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setSizeFull();

        // Strategy B: update handler creates new immutable instances
        calendar.setCalendarItemUpdateHandler((appointment, changes) -> {
            var updated = new Appointment(
                    appointment.id(),
                    appointment.title(),
                    changes.getChangedStart().orElse(appointment.start()),
                    changes.getChangedEnd().orElse(appointment.end()),
                    appointment.color()
            );
            store.put(updated.id(), updated);
            provider.refreshAll();
            Notification.show("Updated: " + updated.title()
                    + " (new instance created — records are immutable)");
        });

        // Click listener
        calendar.addCalendarItemClickedListener(event -> {
            Appointment appt = event.getItem();
            Notification.show("Clicked: " + appt.title()
                    + " [" + appt.getClass().getSimpleName() + " — immutable record]");
        });

        add(calendar);
        setFlexGrow(1, calendar);
    }

    private void addToStore(Appointment appointment) {
        store.put(appointment.id(), appointment);
    }
}
