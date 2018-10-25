# vaadin_fullcalendar
Integration of FullCalendar as Flow component for Vaadin Platform / Vaadin 10+.

## Early usage example

```
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
```
