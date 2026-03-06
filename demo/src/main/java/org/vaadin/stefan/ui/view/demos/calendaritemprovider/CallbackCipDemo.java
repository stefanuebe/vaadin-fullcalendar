package org.vaadin.stefan.ui.view.demos.calendaritemprovider;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.CalendarItemProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.CallbackCalendarItemProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarItemProviderToolbar;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Route(value = "cip-callback", layout = MainLayout.class)
@MenuItem(label = "Callback CIP")
public class CallbackCipDemo extends AbstractDemoView {

    // DEMO-START
    // NOTE: fields are NOT initialized here to avoid the AbstractDemoView constructor ordering issue.
    // All state is initialized inside createCalendar() where it is first used.
    private Map<String, Meeting> store;
    private CallbackCalendarItemProvider<Meeting> provider;

    @Override
    protected FullCalendar<?> createCalendar() {
        // Initialize backing store here — field initializers run after super(), too late
        store = new ConcurrentHashMap<>();

        LocalDate today = LocalDate.now();

        // Seed the store
        addToStore(new Meeting("1", "Architecture Review",
                today.atTime(9, 0), today.atTime(10, 30), "#3788d8"));
        addToStore(new Meeting("2", "Product Demo",
                today.atTime(14, 0), today.atTime(15, 0), "#e74c3c"));
        addToStore(new Meeting("3", "1:1 with Manager",
                today.plusDays(1).atTime(11, 0), today.plusDays(1).atTime(11, 30), "#2ecc71"));
        addToStore(new Meeting("4", "Design Workshop",
                today.plusDays(2).atTime(9, 0), today.plusDays(2).atTime(12, 0), "#9b59b6"));
        addToStore(new Meeting("5", "Release Planning",
                today.plusDays(3).atTime(15, 0), today.plusDays(3).atTime(16, 0), "#f39c12"));
        addToStore(new Meeting("6", "Customer Interview",
                today.minusDays(1).atTime(10, 0), today.minusDays(1).atTime(11, 0), "#e74c3c"));

        // Read-only mapper — Strategy B handles all mutations via the update handler
        var mapper = CalendarItemPropertyMapper.of(Meeting.class)
                .id(Meeting::getId)
                .title(Meeting::getSubject)
                .start(Meeting::getBegin)
                .end(Meeting::getFinish)
                .allDay(Meeting::isAllDay)
                .color(Meeting::getColor);

        // Callback provider with optional date-range filtering
        provider = (CallbackCalendarItemProvider<Meeting>) CalendarItemProvider.fromCallbacks(
                query -> store.values().stream().filter(m -> {
                    if (query.getStart() != null && m.getFinish() != null
                            && m.getFinish().isBefore(query.getStart())) {
                        return false;
                    }
                    if (query.getEnd() != null && m.getBegin() != null
                            && m.getBegin().isAfter(query.getEnd())) {
                        return false;
                    }
                    return true;
                }),
                id -> store.get(id)
        );

        // Build the calendar
        FullCalendar<Meeting> calendar = FullCalendarBuilder.<Meeting>create(Meeting.class)
                .withCalendarItemProvider(provider, mapper)
                .withCalendarItemLimit(3)
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setSizeFull();

        // Enable drag/drop and resize
        calendar.setEditable(true);

        // Strategy B: explicit update handler — we control how changes are persisted
        calendar.setCalendarItemUpdateHandler((meeting, changes) -> {
            Meeting updated = store.get(meeting.getId());
            if (updated != null) {
                changes.getChangedStart().ifPresent(updated::setBegin);
                changes.getChangedEnd().ifPresent(updated::setFinish);
                changes.getChangedAllDay().ifPresent(updated::setAllDay);
                store.put(updated.getId(), updated);
            }
            provider.refreshAll();
            Notification.show("Updated (Strategy B): " + meeting.getSubject());
        });

        // Click listener
        calendar.addCalendarItemClickedListener(event -> {
            Meeting meeting = event.getItem();
            String time = meeting.isAllDay()
                    ? "all day"
                    : meeting.getBegin().toLocalTime() + " – " + meeting.getFinish().toLocalTime();
            Notification.show(meeting.getSubject() + " (" + time + ")");
        });

        return calendar;
    }

    private void addToStore(Meeting meeting) {
        store.put(meeting.getId(), meeting);
    }

    @Override
    protected Component createToolbar() {
        return CalendarItemProviderToolbar.builder()
                .calendar(getCalendar())
                .dateChangeable(true)
                .viewChangeable(true)
                .settingsAvailable(true)
                .build();
    }
    // DEMO-END

    @Override
    protected String createDescription() {
        return "Uses CallbackCalendarItemProvider with explicit update handler (Strategy B). "
                + "Changes from drag/drop are applied manually, supporting immutable or store-managed objects.";
    }
}
