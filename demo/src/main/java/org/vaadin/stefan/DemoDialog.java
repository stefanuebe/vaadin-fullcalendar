package org.vaadin.stefan;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.ThemeList;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.Timezone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DemoDialog extends Dialog {

    private static final String[] COLORS = {"tomato", "orange", "dodgerblue", "mediumseagreen", "gray", "slateblue", "violet"};

    public DemoDialog(FullCalendar calendar, Entry entry, boolean newInstance) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        layout.setSizeFull();

        TextField fieldTitle = new TextField("Title");
        fieldTitle.focus();

        ComboBox<String> fieldColor = new ComboBox<>("Color", COLORS);
        TextArea fieldDescription = new TextArea("Description");

        layout.add(fieldTitle, fieldColor, fieldDescription);

//        TextField fieldStart = new TextField("Start");
//        fieldStart.setReadOnly(true);
//
//        TextField fieldEnd = new TextField("End");
//        fieldEnd.setReadOnly(true);

        CustomDateTimePicker fieldStart = new CustomDateTimePicker("Start");
        CustomDateTimePicker fieldEnd = new CustomDateTimePicker("End");

        Checkbox fieldAllDay = new Checkbox("All day event");

        boolean allDay = entry.isAllDay();
        fieldStart.setDateOnly(allDay);
        fieldEnd.setDateOnly(allDay);

        fieldAllDay.addValueChangeListener(event -> fieldStart.setDateOnly(event.getValue()));
        fieldAllDay.addValueChangeListener(event -> fieldEnd.setDateOnly(event.getValue()));

        Span infoEnd = new Span("End is always exclusive, e.g. for a 1 day event you need to set for instance 4th of May as start and 5th of May as end.");
        infoEnd.getStyle().set("font-size", "0.8em");
        infoEnd.getStyle().set("color", "gray");

        layout.add(fieldStart, fieldEnd, infoEnd, fieldAllDay);

        Binder<Entry> binder = new Binder<>(Entry.class);
        binder.forField(fieldTitle)
                .asRequired()
                .bind(Entry::getTitle, Entry::setTitle);

        binder.bind(fieldColor, Entry::getColor, Entry::setColor);
        binder.bind(fieldDescription, Entry::getDescription, Entry::setDescription);
        Timezone timezone = calendar.getTimezone();
        binder.bind(fieldStart, e -> e.getStart(timezone), (e, start) -> e.setStart(start, timezone));
        binder.bind(fieldEnd, e -> e.getEnd(timezone), (e, end) -> e.setEnd(end, timezone));
        binder.bind(fieldAllDay, Entry::isAllDay, Entry::setAllDay);

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

        Button buttonCancel = new Button("Cancel", e -> close());
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

    /**
     * see https://vaadin.com/components/vaadin-custom-field/java-examples
     */
    public static class CustomDateTimePicker extends CustomField<LocalDateTime> {

        private final DatePicker datePicker = new DatePicker();
        private final TimePicker timePicker = new TimePicker();
        private boolean dateOnly;

        CustomDateTimePicker(String label) {
            setLabel(label);
            add(datePicker, timePicker);
        }

        @Override
        protected LocalDateTime generateModelValue() {
            final LocalDate date = datePicker.getValue();
            final LocalTime time = timePicker.getValue();

            if (date != null) {
                if (dateOnly || time == null) {
                    return date.atStartOfDay();
                }

                return LocalDateTime.of(date, time);
            }

            return null;

        }

        @Override
        protected void setPresentationValue(
                LocalDateTime newPresentationValue) {
            datePicker.setValue(newPresentationValue != null ? newPresentationValue.toLocalDate() : null);
            timePicker.setValue(newPresentationValue != null ? newPresentationValue.toLocalTime() : null);
        }

        public void setDateOnly(boolean dateOnly) {
            this.dateOnly = dateOnly;
            timePicker.setVisible(!dateOnly);
        }
    }

}
