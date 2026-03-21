package org.vaadin.stefan.ui.view.playground;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.vaadin.stefan.fullcalendar.Entry;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Simplified entry edit/create dialog for E2E tests.
 */
public class PlaygroundDialog extends Dialog {

    private Consumer<Entry> saveConsumer;
    private Consumer<Entry> deleteConsumer;

    public PlaygroundDialog(Entry entry, boolean newInstance) {
        Entry tmpEntry = entry.copyAsType(entry.getClass());

        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setWidth("500px");

        // --- Fields ---
        TextField fieldTitle = new TextField("Title");
        fieldTitle.setValue(tmpEntry.getTitle() != null ? tmpEntry.getTitle() : "");
        fieldTitle.setWidthFull();

        Checkbox fieldAllDay = new Checkbox("All day event");
        fieldAllDay.setValue(tmpEntry.isAllDay());

        Checkbox fieldRecurring = new Checkbox("Recurring event");
        fieldRecurring.setValue(tmpEntry.isRecurring());

        DateTimePicker fieldStart = new DateTimePicker();
        fieldStart.setLabel("Start");
        fieldStart.setWidthFull();

        DateTimePicker fieldEnd = new DateTimePicker();
        fieldEnd.setLabel("End");
        fieldEnd.setWidthFull();

        MultiSelectComboBox<DayOfWeek> fieldRDays = new MultiSelectComboBox<>("Recurring days");
        fieldRDays.setItems(DayOfWeek.values());
        fieldRDays.setWidthFull();
        fieldRDays.setVisible(tmpEntry.isRecurring());

        // Init date/time values
        if (tmpEntry.isRecurring()) {
            fieldStart.setLabel("Start of recurrence");
            LocalDate rsd = tmpEntry.getRecurringStartDate();
            LocalTime rst = tmpEntry.getRecurringStartTimeAsLocalTime();
            fieldStart.setValue(rsd != null ? rsd.atTime(rst != null ? rst : LocalTime.MIN) : null);
            LocalDate red = tmpEntry.getRecurringEndDate();
            LocalTime ret = tmpEntry.getRecurringEndTimeAsLocalTime();
            fieldEnd.setValue(red != null ? red.atTime(ret != null ? ret : LocalTime.MIN) : null);
            fieldRDays.setValue(tmpEntry.getRecurringDaysOfWeek());
        } else {
            fieldStart.setValue(tmpEntry.getStartWithOffset());
            fieldEnd.setValue(tmpEntry.getEndWithOffset());
        }

        // --- Listeners ---
        fieldAllDay.addValueChangeListener(e -> {
            // No special date-only mode needed for tests — just track the value
        });

        fieldRecurring.addValueChangeListener(e -> {
            boolean recurring = e.getValue();
            fieldRDays.setVisible(recurring);
            fieldStart.setLabel(recurring ? "Start of recurrence" : "Start");
            fieldEnd.setLabel(recurring ? "End of recurrence" : "End");
        });

        // --- Buttons ---
        Button btnSave = new Button("Save", e -> {
            // Apply field values back to original entry
            entry.setTitle(fieldTitle.getValue());
            entry.setAllDay(fieldAllDay.getValue());

            if (fieldRecurring.getValue()) {
                // Recurring mode
                LocalDateTime startVal = fieldStart.getValue();
                LocalDateTime endVal = fieldEnd.getValue();
                entry.setStart((LocalDateTime) null);
                entry.setEnd((LocalDateTime) null);
                entry.setRecurringStartDate(startVal != null ? startVal.toLocalDate() : null);
                entry.setRecurringStartTime(startVal != null ? startVal.toLocalTime() : null);
                entry.setRecurringEndDate(endVal != null ? endVal.toLocalDate() : null);
                entry.setRecurringEndTime(endVal != null ? endVal.toLocalTime() : null);
                Set<DayOfWeek> days = fieldRDays.getValue();
                entry.setRecurringDaysOfWeek(days.toArray(new DayOfWeek[0]));
            } else {
                // Non-recurring
                entry.clearRecurringStart();
                entry.clearRecurringEnd();
                entry.setRecurringDaysOfWeek();
                LocalDateTime startVal = fieldStart.getValue();
                LocalDateTime endVal = fieldEnd.getValue();

                if (fieldAllDay.getValue()) {
                    entry.setAllDay(true);
                    entry.setStart(startVal != null ? startVal.toLocalDate() : LocalDate.now());
                    entry.setEnd(endVal != null ? endVal.toLocalDate().plusDays(1) : entry.getStartAsLocalDate().plusDays(1));
                } else {
                    entry.setAllDay(false);
                    // Ensure we have proper times (not midnight) for timed entries
                    if (startVal != null && startVal.toLocalTime().equals(LocalTime.MIN)) {
                        startVal = startVal.withHour(10).withMinute(0);
                    }
                    if (endVal != null && endVal.toLocalTime().equals(LocalTime.MIN)) {
                        endVal = startVal != null ? startVal.plusHours(1) : endVal;
                    }
                    if (startVal == null) {
                        startVal = LocalDate.now().atTime(10, 0);
                    }
                    if (endVal == null) {
                        endVal = startVal.plusHours(1);
                    }
                    entry.setStart(startVal);
                    entry.setEnd(endVal);
                }
            }

            if (saveConsumer != null) {
                saveConsumer.accept(entry);
            }
            close();
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancel = new Button("Cancel", e -> close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttons = new HorizontalLayout(btnSave, btnCancel);

        if (!newInstance) {
            Button btnRemove = new Button("Remove", e -> {
                if (deleteConsumer != null) {
                    deleteConsumer.accept(entry);
                }
                close();
            });
            btnRemove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            buttons.add(btnRemove);
        }

        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // --- Layout ---
        VerticalLayout layout = new VerticalLayout(
                fieldTitle, fieldAllDay, fieldRecurring,
                fieldStart, fieldEnd, fieldRDays, buttons
        );
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setSizeFull();

        add(layout);
    }

    public void setSaveConsumer(Consumer<Entry> saveConsumer) {
        this.saveConsumer = saveConsumer;
    }

    public void setDeleteConsumer(Consumer<Entry> deleteConsumer) {
        this.deleteConsumer = deleteConsumer;
    }
}
