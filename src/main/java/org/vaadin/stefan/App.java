package org.vaadin.stefan;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Event;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Route("")
public class App extends VerticalLayout {
    public App() {
        FullCalendar calendar = new FullCalendar();
        calendar.addDayClickListener(event -> {
            Optional<LocalDateTime> optionalDateTime = event.getClickedDateTime();
            Optional<LocalDate> optionalDate = event.getClickedDate();

            if (optionalDateTime.isPresent()) { // check if user clicked a time slot
                LocalDateTime time = optionalDateTime.get();
                calendar.addEvent(new Event(time.toString(), time, time.plusHours(1)));

            } else if (optionalDate.isPresent()) { // check if user clicked a day slot
                LocalDate date = optionalDate.get();
                calendar.addEvent(new Event(date.toString(), date));

            }
        });


        HorizontalLayout functions = new HorizontalLayout();
        functions.add(new Button("Previous", e -> calendar.previous()));
        functions.add(new Button("Next", e -> calendar.next()));

        add(new H2("full calendar"));
        add(functions);
        add(new Hr());
        add(calendar);
    }
}
