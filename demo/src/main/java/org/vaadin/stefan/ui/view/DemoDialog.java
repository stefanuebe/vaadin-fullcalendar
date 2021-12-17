package org.vaadin.stefan.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
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
import lombok.Data;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.ResourceEntry;
import org.vaadin.stefan.fullcalendar.Timezone;

import java.time.*;
import java.util.Set;

public class DemoDialog extends Dialog {
	private static final long serialVersionUID = 1L;
	
    private static final String[] COLORS = {"tomato", "orange", "dodgerblue", "mediumseagreen", "gray", "slateblue", "violet"};
    private final DialogEntry dialogEntry;
    private final CustomDateTimePicker fieldStart;
    private final CustomDateTimePicker fieldEnd;
    private final CheckboxGroup<DayOfWeek> fieldRDays;

    public DemoDialog(FullCalendar calendar, ResourceEntry entry, boolean newInstance) {
        this.dialogEntry = DialogEntry.of(entry, calendar.getTimezoneClient());

        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        setWidth("500px");

        // init fields

        TextField fieldTitle = new TextField("Title");
        ComboBox<String> fieldColor = new ComboBox<>("Color", COLORS);
        fieldColor.setPreventInvalidInput(false);
        fieldColor.setAllowCustomValue(true);
        fieldColor.addCustomValueSetListener(event -> fieldColor.setValue(event.getDetail()));
        fieldColor.setClearButtonVisible(true);

        TextArea fieldDescription = new TextArea("Description");

        Checkbox fieldRecurring = new Checkbox("Recurring event");
        Checkbox fieldAllDay = new Checkbox("All day event");

        fieldStart = new CustomDateTimePicker("Start");
        fieldEnd = new CustomDateTimePicker("End");

        boolean allDay = dialogEntry.isAllDay();
        fieldStart.setDateOnly(allDay);
        fieldEnd.setDateOnly(allDay);

        Span infoEnd = new Span("End is always exclusive, e.g. for a 1 day event you need to set for instance 4th of May as start and 5th of May as end.");
        infoEnd.getStyle().set("font-size", "0.8em");
        infoEnd.getStyle().set("color", "gray");

        fieldRDays = new CheckboxGroup<>();
        fieldRDays.setLabel("Recurrence days of week");
        fieldRDays.setItems(DayOfWeek.values());
        fieldRDays.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        fieldAllDay.addValueChangeListener(event -> {
            fieldStart.setDateOnly(event.getValue());
            fieldEnd.setDateOnly(event.getValue());
        });

        fieldRecurring.addValueChangeListener(event -> updateRecurringFieldsState(event.getValue()));


        // init binder

        Binder<DialogEntry> binder = new Binder<>(DialogEntry.class);

        // required fields
        binder.forField(fieldTitle).asRequired().bind(DialogEntry::getTitle, DialogEntry::setTitle);
        binder.forField(fieldStart).asRequired().bind(DialogEntry::getStart, DialogEntry::setStart);
        binder.forField(fieldEnd).asRequired().bind(DialogEntry::getEnd, DialogEntry::setEnd);

        // optional fields
        binder.bind(fieldColor, DialogEntry::getColor, DialogEntry::setColor);
        binder.bind(fieldDescription, DialogEntry::getDescription, DialogEntry::setDescription);
        binder.bind(fieldAllDay, DialogEntry::isAllDay, DialogEntry::setAllDay);
        binder.bind(fieldRecurring, DialogEntry::isRecurring, DialogEntry::setRecurring);
        binder.bind(fieldRDays, DialogEntry::getRecurringDays, DialogEntry::setRecurringDays);

        binder.setBean(dialogEntry);


        // init buttons

        Button buttonSave = new Button("Save", e -> {
            if (binder.validate().isOk()) {
                // to prevent accidentally "disappearing" days
                if (dialogEntry.isAllDay() && dialogEntry.getStart().toLocalDate().equals(dialogEntry.getEnd().toLocalDate())) {
                    dialogEntry.setEnd(dialogEntry.getEnd().plusDays(1));
                }

                if (newInstance) {
                    calendar.addEntry(dialogEntry.updateEntry());
                } else {
                    calendar.updateEntry(dialogEntry.updateEntry());
                }
                close();
            }
        });
        buttonSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button buttonCancel = new Button("Cancel", e -> close());
        buttonCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttons = new HorizontalLayout(buttonSave, buttonCancel);

        if (!newInstance) {
            Button buttonRemove = new Button("Remove", e -> {
                calendar.removeEntry(entry);
                close();
            });
            buttonRemove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            buttons.add(buttonRemove);
        }


        // TODO add resource assignment widget

        // layouting

        VerticalLayout mainLayout = new VerticalLayout(fieldTitle, fieldColor, fieldDescription,
                new HorizontalLayout(fieldAllDay, fieldRecurring),
                fieldStart, fieldEnd, infoEnd, fieldRDays);

        mainLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        mainLayout.setSizeFull();

        mainLayout.getElement().getStyle().set("overflow-y", "auto");

        add(mainLayout, buttons);


        // additional layout init

        updateRecurringFieldsState(dialogEntry.isRecurring());
        fieldTitle.focus();
    }

    protected void updateRecurringFieldsState(boolean recurring) {
        if (recurring) {
            fieldStart.setLabel("Start of recurrence");
            fieldEnd.setLabel("End of recurrence");
        } else {
            fieldStart.setLabel("Start");
            fieldEnd.setLabel("End");
        }
        fieldRDays.setVisible(recurring);
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

    @Data
    private static class DialogEntry {
        private String id;
        private String title;
        private String color;
        private String description;
        private LocalDateTime start;
        private LocalDateTime end;
        private boolean allDay;
        private boolean recurring;
        private Set<DayOfWeek> recurringDays;
        private Timezone timezone;
        private ResourceEntry entry;

        public static DialogEntry of(ResourceEntry entry, Timezone timezone) {
            DialogEntry dialogEntry = new DialogEntry();

            dialogEntry.setTimezone(timezone);
            dialogEntry.setEntry(entry);

            dialogEntry.setTitle(entry.getTitle());
            dialogEntry.setColor(entry.getColor());
            dialogEntry.setDescription(entry.getDescription());
            dialogEntry.setAllDay(entry.isAllDay());

            boolean recurring = entry.isRecurring();
            dialogEntry.setRecurring(recurring);

            if (recurring) {
                dialogEntry.setRecurringDays(entry.getRecurringDaysOfWeeks());

                LocalDate startDate = entry.getRecurringStartDate(timezone);
                LocalDate endDate = entry.getRecurringEndDate(timezone);

                dialogEntry.setStart(entry.isAllDay() ? startDate.atStartOfDay() : startDate.atTime(entry.getRecurringStartTime()));
                dialogEntry.setEnd(entry.isAllDay() ? endDate.atStartOfDay().plusDays(1) : endDate.atTime(entry.getRecurringEndTime()));
            } else {
                dialogEntry.setStart(entry.getStart(timezone));
                dialogEntry.setEnd(entry.getEnd(timezone));
            }

            return dialogEntry;
        }

        /**
         * Updates the stored entry instance and returns it after updating.
         *
         * @return entry instnace
         */
        private Entry updateEntry() {
            entry.setTitle(title);
            entry.setColor(color);
            entry.setDescription(description);
            entry.setAllDay(allDay);

            if (recurring) {
                entry.setRecurringDaysOfWeeks(getRecurringDays());

                entry.setStart((Instant) null);
                entry.setEnd((Instant) null);

                entry.setRecurringStartDate(start.toLocalDate(), timezone);
                entry.setRecurringStartTime(allDay ? null : start.toLocalTime());

                entry.setRecurringEndDate(end.toLocalDate(), timezone);
                entry.setRecurringEndTime(allDay ? null : end.toLocalTime());
            } else {
                entry.setStart(start, timezone);
                entry.setEnd(end, timezone);

                entry.setRecurringStartDate((Instant) null);
                entry.setRecurringStartTime(null);
                entry.setRecurringEndDate((Instant) null);
                entry.setRecurringEndTime(null);
                entry.setRecurringDaysOfWeeks(null);
            }

            return entry;
        }
    }
}
