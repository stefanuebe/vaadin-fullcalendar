package org.vaadin.stefan.ui.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.SerializableConsumer;
import lombok.Setter;
import org.vaadin.stefan.fullcalendar.Delta;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.ResourceEntry;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;

public class DemoDialog extends Dialog {
	private static final long serialVersionUID = 1L;

    private static final String[] COLORS = {"tomato", "orange", "dodgerblue", "mediumseagreen", "gray", "slateblue", "violet"};
    private final VerticalLayout componentsLayout;

    @Setter
    private SerializableConsumer<Entry> onSaveConsumer;

    @Setter
    private SerializableConsumer<Entry> onDeleteConsumer;

    private final Entry tmpEntry;

    private final CustomDateTimePicker fieldStart;
    private final CustomDateTimePicker fieldEnd;
    private final MultiSelectComboBox<DayOfWeek> fieldRDays;
    private final Binder<Entry> binder;
    private boolean recurring;
    private final Entry entry;
    private boolean resetPeriodOnAllDayChange;
    private boolean anyTimeHasChangedByUser;

    public DemoDialog(Entry entry, boolean newInstance) {
        this.entry = entry;
        this.resetPeriodOnAllDayChange = entry.isAllDay();

        // tmp entry is a copy. we will use its start and end to represent either the start/end or the recurring start/end
        this.tmpEntry = entry.copyAsType(entry.getClass());

        this.recurring = entry.isRecurring();
        tmpEntry.setStart(recurring ? entry.getRecurringStart() : entry.getStartWithOffset());
        tmpEntry.setEnd(recurring ? entry.getRecurringEnd() : entry.getEndWithOffset());

        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setWidth("500px");

        // init fields

        TextField fieldTitle = new TextField("Title");
        ComboBox<String> fieldColor = new ComboBox<>("Color", COLORS);
        fieldColor.setAllowCustomValue(true);
        fieldColor.addCustomValueSetListener(event -> fieldColor.setValue(event.getDetail()));
        fieldColor.setClearButtonVisible(true);

        TextArea fieldDescription = new TextArea("Description");

        Checkbox fieldRecurring = new Checkbox("Recurring event");
        Checkbox fieldAllDay = new Checkbox("All day event");

        fieldStart = new CustomDateTimePicker("Start");
        fieldEnd = new CustomDateTimePicker("End");

//        boolean allDay = this.tmpEntry.isAllDay();
//        fieldStart.setDateOnly(allDay);
//        fieldEnd.setDateOnly(allDay);

        Span infoEnd = new Span("End is always exclusive, e.g. for a 1 day event you need to set for instance 4th of May as start and 5th of May as end.");
        infoEnd.getStyle().set("font-size", "0.8em");
        infoEnd.getStyle().set("color", "gray");

        fieldRDays = new MultiSelectComboBox<>("Recurrence days of week", DayOfWeek.values());
        fieldRDays.setItemLabelGenerator(item -> item.getDisplayName(TextStyle.FULL, getLocale()));

        // layouting - MUST be initialized here, otherwise might lead to null pointer exception
        componentsLayout = new VerticalLayout(fieldTitle, fieldColor, fieldDescription,
                new HorizontalLayout(fieldAllDay, fieldRecurring),
                fieldStart, fieldEnd, infoEnd, fieldRDays);

        componentsLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        componentsLayout.setSizeFull();
        componentsLayout.setSpacing(false);

        fieldAllDay.addValueChangeListener(event -> {
            fieldStart.setDateOnly(event.getValue());
            fieldEnd.setDateOnly(event.getValue());

            if (resetPeriodOnAllDayChange && event.isFromClient()) {
                if (event.getValue()) {
                    LocalDateTime start = fieldStart.getValue().toLocalDate().atStartOfDay();

                    // reset the start to the same day with one hour difference
                    fieldStart.setValue(start);
                    fieldEnd.setValue(start.plusDays(1));

                } else {
                    LocalDateTime start = fieldStart.getValue().toLocalDate().atTime(LocalTime.now());

                    // reset the start to the same day with one hour difference
                    fieldStart.setValue(start);
                    fieldEnd.setValue(start.plusHours(1));
                }
            }
        });

        fieldRecurring.addValueChangeListener(event -> onRecurringChanged(event.getValue()));


        // init binder

        binder = new Binder<>(Entry.class);

        // required fields
        binder.forField(fieldTitle).asRequired().bind(Entry::getTitle, Entry::setTitle);
        binder.forField(fieldStart).asRequired().bind(Entry::getStart, Entry::setStart);
        binder.forField(fieldEnd).asRequired().bind(Entry::getEnd, Entry::setEnd);

        // optional fields
        binder.bind(fieldColor, Entry::getColor, Entry::setColor);
        binder.bind(fieldDescription, Entry::getDescription, Entry::setDescription);
        binder.bind(fieldAllDay, Entry::isAllDay, Entry::setAllDay);
        binder.bind(fieldRecurring, item -> this.recurring, (item, value) -> this.recurring = value);
        binder.bind(fieldRDays, Entry::getRecurringDaysOfWeek, Entry::setRecurringDaysOfWeek);

        binder.setBean(this.tmpEntry);

        fieldStart.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                resetPeriodOnAllDayChange = false;
            }

            LocalDateTime oldStart = event.getOldValue();
            LocalDateTime newStart = event.getValue();
            LocalDateTime end = fieldEnd.getValue();


            if (oldStart != null && newStart != null && end != null) {
                Delta delta = Delta.fromLocalDates(oldStart, newStart);
                end = delta.applyOn(end);
                fieldEnd.setValue(end);
            }
        });

        fieldEnd.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                resetPeriodOnAllDayChange = false;
            }
        });

        // init buttons
        Button buttonSave = new Button("Save");
        buttonSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonSave.addClickListener(e -> onSave());

        Button buttonCancel = new Button("Cancel", e -> close());
        buttonCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttons = new HorizontalLayout(buttonSave, buttonCancel);
        buttons.setPadding(true);
        buttons.getStyle().set("border-top", "1px solid #ddd");

        if (!newInstance) {
            Button buttonRemove = new Button("Remove", e -> onRemove());
            buttonRemove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            buttons.add(buttonRemove);
        }

        Scroller scroller = new Scroller(componentsLayout);
        VerticalLayout outer = new VerticalLayout();
        outer.add(scroller, buttons);
        outer.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        outer.setFlexGrow(1, scroller);
        outer.setSizeFull();
        outer.setPadding(false);
        outer.setSpacing(false);

        add(outer);

        // additional layout init
        onRecurringChanged(this.tmpEntry.isRecurring());
        fieldTitle.focus();
    }

    @Override
    public void open() {
        // DO NOT COMMIT THIS
    }

    protected void onSave() {
        if (onSaveConsumer == null) {
            throw new UnsupportedOperationException("No save consumer set");
        }

        if (binder.validate().isOk()) {
            // to prevent accidentally "disappearing" days
            if (this.tmpEntry.isAllDay() && this.tmpEntry.getStart().toLocalDate().equals(this.tmpEntry.getEnd().toLocalDate())) {
                this.tmpEntry.setEnd(this.tmpEntry.getEnd().plusDays(1));
            }

            // we can also create a fresh copy and leave the initial entry totally untouched
            entry.copyFrom(tmpEntry);
            if (recurring) {
                entry.clearStart();
                entry.clearEnd();
                entry.setRecurringStart(tmpEntry.getStart());
                entry.setRecurringEnd(tmpEntry.getEnd());
            } else {
                entry.setStartWithOffset(tmpEntry.getStart());
                entry.setEndWithOffset(tmpEntry.getEnd());
                entry.setRecurringDaysOfWeek(); // remove the DoW
                entry.clearRecurringStart();
                entry.clearRecurringEnd();
            }

            onSaveConsumer.accept(this.entry);
            close();
        }
    }

    protected void onRemove() {
        if (onDeleteConsumer == null) {
            throw new UnsupportedOperationException("No remove consumer set");
        }
        onDeleteConsumer.accept(entry);
        close();
    }

    protected void onRecurringChanged(boolean recurring) {
        if (recurring) {
            fieldStart.setLabel("Start of recurrence");
            fieldEnd.setLabel("End of recurrence");
            if (!fieldRDays.getParent().isPresent()) {
                componentsLayout.add(fieldRDays);
            }
        } else {
            fieldStart.setLabel("Start");
            fieldEnd.setLabel("End");
            fieldRDays.getElement().removeFromParent();
        }


        fieldRDays.setVisible(recurring);
    }

    public void setDeleteConsumer(SerializableConsumer<Entry> onDeleteConsumer) {
        this.onDeleteConsumer = onDeleteConsumer;
    }

    public void setSaveConsumer(SerializableConsumer<Entry> onSaveConsumer) {
        this.onSaveConsumer = onSaveConsumer;
    }
}
