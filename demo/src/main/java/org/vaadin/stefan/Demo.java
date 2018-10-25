package org.vaadin.stefan;

import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarView;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Route("")
@Push
@HtmlImport("frontend://demo-style.html")
public class Demo extends VerticalLayout {

    private static final String[] COLORS = {"tomato", "orange", "dodgerblue", "mediumseagreen", "gray", "slateblue", "violet"};

    public Demo() {
        FullCalendar calendar = new FullCalendar();
        calendar.addDayClickListener(event -> {
            Optional<LocalDateTime> optionalDateTime = event.getClickedDateTime();
            Optional<LocalDate> optionalDate = event.getClickedDate();

            Entry entry = new Entry();
            if (optionalDateTime.isPresent()) { // check if user clicked a time slot
                LocalDateTime time = optionalDateTime.get();

                entry.setStart(time);
                entry.setEnd(time.plusHours(FullCalendar.DEFAULT_TIMED_EVENT_DURATION));
                entry.setAllDay(false);

            } else if (optionalDate.isPresent()) { // check if user clicked a day slot
                LocalDateTime date = optionalDate.get().atStartOfDay();

                entry.setStart(date);
                entry.setEnd(date.plusDays(FullCalendar.DEFAULT_DAY_EVENT_DURATION));
                entry.setAllDay(true);

            }
            entry.setColor("dodgerblue");
            new DemoDialog(calendar, entry, true).open();
        });

        calendar.addEntryClickListener(event -> new DemoDialog(calendar, event.getEntry(), false).open());

        calendar.addEntryResizeListener(event -> {
            Entry entry = event.getEntry();
            Notification.show(entry.getTitle() + " resized to " + entry.getStart() + " - " + entry.getEnd() + " by " + event.getDelta());
        });
        calendar.addEntryDropListener(event -> {
            Entry entry = event.getEntry();
            boolean allDay = entry.isAllDay();
            LocalDateTime start = entry.getStart();
            LocalDateTime end = entry.getEnd();

            Notification.show(entry.getTitle() + " moved to " + start + " - " + end+ " by " + event.getDelta());
        });

        HorizontalLayout functions = new HorizontalLayout();
        functions.add(new Button("Previous", e -> calendar.previous()));
        functions.add(new Button("Today", e -> calendar.today()));
        functions.add(new Button("Next", e -> calendar.next()));
        ComboBox<CalendarView> comboBox = new ComboBox<>("", CalendarView.values());
        comboBox.addValueChangeListener(e -> {
            CalendarView value = e.getValue();
            calendar.changeView(value == null ? CalendarView.MONTH : value);
        });
        comboBox.setValue(CalendarView.MONTH);

        functions.add(comboBox);


        // simulate the date picker light that we can use in polymer
        DatePicker gotoDate = new DatePicker();
        gotoDate.addValueChangeListener(event1 -> calendar.gotoDate(event1.getValue()));
        gotoDate.getElement().getStyle().set("visibility", "hidden");
        gotoDate.getElement().getStyle().set("position", "fixed");
        gotoDate.setWidth("0px");
        gotoDate.setHeight("0px");

        Button interval = new Button();
        interval.getElement().appendChild(gotoDate.getElement());
        interval.addClickListener(event -> {
            gotoDate.open();
        });
        functions.add(interval);
        calendar.addViewRenderedListener(event -> updateIntervalLabel(interval, comboBox.getValue(), event.getIntervalStart()));


        add(new H2("full calendar"));
        add(functions);
        add(new Hr());
        add(calendar);

        setFlexGrow(1, calendar);
        addClassName("demo");
        setSizeUndefined();
        setMargin(false);
        setSpacing(false);
        setPadding(false);

        calendar.setFirstDay(1);
    }

    public void updateIntervalLabel(HasText intervalLabel, CalendarView view, LocalDate intervalStart) {
        String text;
        switch (view) {
            default:
            case MONTH:
                text = intervalStart.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                break;
            case AGENDA_DAY:
            case BASIC_DAY:
                text = intervalStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                break;
            case AGENDA_WEEK:
            case BASIC_WEEK:
                text = intervalStart.format(DateTimeFormatter.ofPattern("ww/yyyy"));
                break;
        }

        intervalLabel.setText(text);
    }

    public static class DemoDialog extends Dialog {

        public DemoDialog(FullCalendar calendar, Entry entry, boolean newInstance) {
            setCloseOnEsc(true);
            setCloseOnOutsideClick(false);

            VerticalLayout layout = new VerticalLayout();
            layout.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
            layout.setSizeFull();

            TextField fieldTitle = new TextField("Title");
            fieldTitle.focus();

            ComboBox<String> fieldColor = new ComboBox<>("Color", COLORS);
            TextArea fieldDescription = new TextArea("Description");

            TextField fieldStart = new TextField("Start");
            fieldStart.setEnabled(false);

            TextField fieldEnd = new TextField("End");
            fieldEnd.setEnabled(false);

            fieldStart.setValue(entry.getStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")));
            fieldEnd.setValue(entry.getEnd().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")));

            Checkbox fieldAllDay = new Checkbox("All day event");
            fieldAllDay.setValue(entry.isAllDay());
            fieldAllDay.setEnabled(false);

            layout.add(fieldTitle, fieldColor, fieldDescription, fieldStart, fieldEnd, fieldAllDay);

            Binder<Entry> binder = new Binder<>(Entry.class);
            binder.forField(fieldTitle)
                    .asRequired()
                    .bind(Entry::getTitle, Entry::setTitle);

            binder.bind(fieldColor, Entry::getColor, Entry::setColor);
            binder.bind(fieldDescription, Entry::getDescription, Entry::setDescription);
            binder.setBean(entry);

            HorizontalLayout buttons = new HorizontalLayout();
            Button buttonSave;
            if (newInstance) {
                buttonSave = new Button("Create", e -> {
                    if (binder.validate().isOk()) {
                        calendar.addEntry(entry);
                    }
                });
            } else {
                buttonSave = new Button("Save", e -> {
                    if (binder.validate().isOk()) {
                        calendar.updateEntry(entry);
                    }
                });
            }
            buttonSave.addClickListener(e -> close());
            buttons.add(buttonSave);

            Button buttonCancel = new Button("Cancel", e -> {
                close();
            });
            buttonCancel.getElement().getThemeList().add("tertiary");
            buttons.add(buttonCancel);

            if (!newInstance) {
                Button buttonRemove = new Button("Remove", e -> {
                    calendar.removeEntry(entry);
                    close();
                });
                ThemeList themeList = buttonRemove.getElement().getThemeList();
                themeList.add("error");
                themeList.add("tertiary");
                buttons.add(buttonRemove);
            }

            add(layout, buttons);
        }
    }
}
