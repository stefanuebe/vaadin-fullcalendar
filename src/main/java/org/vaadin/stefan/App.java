package org.vaadin.stefan;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Route("")
public class App extends VerticalLayout {

    private static int count = 1;

    public App() {
        FullCalendar calendar = new FullCalendar();
        calendar.addDayClickListener(event -> {
            Optional<LocalDateTime> optionalDateTime = event.getClickedDateTime();
            Optional<LocalDate> optionalDate = event.getClickedDate();

            String title = "Event " + count++;

            if (optionalDateTime.isPresent()) { // check if user clicked a time slot
                LocalDateTime time = optionalDateTime.get();
                calendar.addEntry(new Entry(title, time, time.plusHours(FullCalendar.DEFAULT_TIMED_EVENT_DURATION)));

            } else if (optionalDate.isPresent()) { // check if user clicked a day slot
                LocalDate date = optionalDate.get();
                calendar.addEntry(new Entry(title, date));

            }
        });

        calendar.addEntryClickListener(event -> {
            Entry entry = event.getEntry();
            String oldTitle = entry.getTitle();
            entry.setTitle("Event " + count++);

            Notification.show(oldTitle + " renamed by click to " + entry.getTitle());
            calendar.updateEntry(entry);
        });
        calendar.addEntryResizeListener(event -> {
            Entry entry = event.getEntry();
            Notification.show(entry.getTitle() + " resized to " + entry.getStart() + " - " + entry.getEnd() + " by " + event.getDelta());
        });
        calendar.addEntryDropListener(event -> {
            Entry entry = event.getEntry();
            boolean allDay = entry.isAllDay();
            LocalDateTime start = entry.getStart();
            LocalDateTime end = entry.getEnd();

            Notification.show(entry.getTitle() + " moved to " + (allDay ? start.toLocalDate() : start) + " - " + (allDay ? end.toLocalDate() : end)+ " by " + event.getDelta());
        });


        HorizontalLayout functions = new HorizontalLayout();
        functions.add(new Button("Previous", e -> calendar.previous()));
        functions.add(new Button("Today", e -> calendar.today()));
        functions.add(new Button("Next", e -> calendar.next()));
        Button button = new Button("Clear", e -> calendar.removeAllEntries());
        button.getElement().getThemeList().add("error");
        functions.add(button);

        add(new H2("full calendar"));
        add(functions);
        add(new Hr());
        add(calendar);
    }
}
