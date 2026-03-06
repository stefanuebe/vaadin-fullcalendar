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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Route(value = "cip-backend", layout = MainLayout.class)
@MenuItem(label = "Backend CIP")
public class BackendCipDemo extends AbstractDemoView {

    // DEMO-START
    private CallbackCalendarItemProvider<Meeting> provider;

    // -----------------------------------------------------------------------
    // Simulated service layer
    // -----------------------------------------------------------------------

    /**
     * Simulates a backend service with CRUD operations backed by an in-memory store.
     * In a real application this would be a Spring {@code @Service} injected via constructor.
     */
    static class MeetingService {

        private final Map<String, Meeting> store = new ConcurrentHashMap<>();

        public Collection<Meeting> findAll() {
            return store.values();
        }

        public Meeting findById(String id) {
            return store.get(id);
        }

        public void save(Meeting meeting) {
            store.put(meeting.getId(), meeting);
        }

        public void deleteById(String id) {
            store.remove(id);
        }
    }

    // -----------------------------------------------------------------------
    // View
    // -----------------------------------------------------------------------

    @Override
    protected FullCalendar<?> createCalendar() {
        MeetingService service = new MeetingService();

        // Seed the service with sample meetings
        LocalDate today = LocalDate.now();
        service.save(new Meeting("1", "Board Meeting",
                today.atTime(9, 0), today.atTime(10, 30), "#3788d8"));
        service.save(new Meeting("2", "Investor Call",
                today.atTime(14, 0), today.atTime(15, 0), "#e74c3c"));
        service.save(new Meeting("3", "Team Lunch",
                today.plusDays(1).atTime(12, 0), today.plusDays(1).atTime(13, 0), "#2ecc71"));
        service.save(new Meeting("4", "Strategy Session",
                today.plusDays(2).atTime(10, 0), today.plusDays(2).atTime(12, 0), "#9b59b6"));
        service.save(new Meeting("5", "Quarterly Review",
                today.plusDays(3).atTime(14, 0), today.plusDays(3).atTime(16, 0), "#f39c12"));
        service.save(new Meeting("6", "Security Audit",
                today.minusDays(1).atTime(9, 0), today.minusDays(1).atTime(11, 0), "#3788d8"));

        // Read-only mapper — mutations are delegated to the service
        var mapper = CalendarItemPropertyMapper.of(Meeting.class)
                .id(Meeting::getId)
                .title(Meeting::getSubject)
                .start(Meeting::getBegin)
                .end(Meeting::getFinish)
                .allDay(Meeting::isAllDay)
                .color(Meeting::getColor);

        // Callback provider delegates to the service for data access
        provider = (CallbackCalendarItemProvider<Meeting>) CalendarItemProvider.fromCallbacks(
                query -> service.findAll().stream().filter(m -> {
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
                id -> service.findById(id)
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

        // Update handler persists changes through the service layer
        calendar.setCalendarItemUpdateHandler((meeting, changes) -> {
            Meeting persisted = service.findById(meeting.getId());
            if (persisted != null) {
                changes.getChangedStart().ifPresent(persisted::setBegin);
                changes.getChangedEnd().ifPresent(persisted::setFinish);
                changes.getChangedAllDay().ifPresent(persisted::setAllDay);
                service.save(persisted);
            }
            provider.refreshAll();
            Notification.show("Persisted via service: " + meeting.getSubject());
        });

        // Click listener showing meeting details
        calendar.addCalendarItemClickedListener(event -> {
            Meeting meeting = event.getItem();
            String time = meeting.isAllDay()
                    ? "all day"
                    : meeting.getBegin().toLocalTime() + " – " + meeting.getFinish().toLocalTime();
            Notification.show(meeting.getSubject() + " (" + time + ")");
        });

        return calendar;
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
        return "Simulates a backend service pattern. The CallbackCalendarItemProvider delegates to a MeetingService, "
                + "and updates are persisted through the service layer.";
    }
}
