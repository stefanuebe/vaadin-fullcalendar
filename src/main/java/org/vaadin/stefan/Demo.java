package org.vaadin.stefan;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import elemental.css.RGBColor;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Route("")
@Push
public class Demo extends VerticalLayout {

    private static final String[] COLORS = {"tomato", "orange", "dodgerblue", "mediumseagreen", "gray", "slateblue", "violet"};

    public Demo() {
        FullCalendar calendar = new FullCalendar();
        calendar.addDayClickListener(event -> {
            Optional<LocalDateTime> optionalDateTime = event.getClickedDateTime();
            Optional<LocalDate> optionalDate = event.getClickedDate();

            if (optionalDateTime.isPresent()) { // check if user clicked a time slot
                LocalDateTime time = optionalDateTime.get();

                Entry entry = new Entry();
                entry.setStart(time);
                entry.setEnd(time.plusHours(FullCalendar.DEFAULT_TIMED_EVENT_DURATION));
                entry.setAllDay(false);

                new DemoDialog(calendar, entry, true).open();

            } else if (optionalDate.isPresent()) { // check if user clicked a day slot
                LocalDateTime date = optionalDate.get().atStartOfDay();

                Entry entry = new Entry();
                entry.setStart(date);
                entry.setEnd(date.plusDays(FullCalendar.DEFAULT_DAY_EVENT_DURATION));
                entry.setAllDay(true);

                new DemoDialog(calendar, entry, true).open();
            }
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
        Button button = new Button("Remove all!", e -> calendar.removeAllEntries());
        button.getElement().getThemeList().add("error");
        functions.add(button);

        add(new H2("full calendar"));
        add(functions);
        add(new Hr());
        add(calendar);
    }

    public static class DemoDialog extends Dialog {
        private final Entry entry;

        public DemoDialog(FullCalendar calendar, Entry entry, boolean newInstance) {
            this.entry = entry;

            setCloseOnEsc(true);
            setCloseOnOutsideClick(false);
            setWidth("500px");

            VerticalLayout layout = new VerticalLayout();
            layout.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
            layout.setSizeFull();

            TextField title = new TextField("Title");
            ComboBox<String> color = new ComboBox<>("Color", COLORS);
            TextArea description = new TextArea("Description");

            TextField start = new TextField("Start");
            start.setValue(entry.getStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")));
            start.setEnabled(false);

            TextField end = new TextField("End");
            end.setValue(entry.getEnd().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")));
            end.setEnabled(false);

            Checkbox allDay = new Checkbox("All day event");
            allDay.setValue(entry.isAllDay());
            allDay.setEnabled(false);

            layout.add(title, color, description, start, end, allDay);

            Binder<Entry> binder = new Binder<>(Entry.class);
            binder.forField(title)
                    .asRequired()
                    .bind(Entry::getTitle, Entry::setTitle);

            binder.bind(color, Entry::getColor, Entry::setColor);
            binder.bind(description, Entry::getDescription, Entry::setDescription);
            binder.setBean(entry);

            HorizontalLayout buttons = new HorizontalLayout();
            Button save;
            if (newInstance) {
                save = new Button("Create", e -> {
                    if (binder.validate().isOk()) {
                        calendar.addEntry(entry);
                    }
                });
            } else {
                save = new Button("Save", e -> {
                    if (binder.validate().isOk()) {
                        calendar.updateEntry(entry);
                    }
                });
            }
            save.addClickListener(e -> close());
            buttons.add(save);

            Button cancel = new Button("Cancel", e -> {
                close();
            });
            cancel.getElement().getThemeList().add("tertiary");
            buttons.add(cancel);

            if (!newInstance) {
                Button remove = new Button("Remove", e -> {
                    calendar.removeEntry(entry);
                    close();
                });
                ThemeList themeList = remove.getElement().getThemeList();
                themeList.add("error");
                themeList.add("tertiary");
                buttons.add(remove);
            }


            add(layout, buttons);
        }


    }
}
