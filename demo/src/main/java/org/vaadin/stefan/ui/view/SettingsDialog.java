package org.vaadin.stefan.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.stefan.fullcalendar.CalendarLocale;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.Timezone;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SettingsDialog extends Dialog {
    public static final List<Timezone> SOME_TIMEZONES = Arrays.asList(Timezone.UTC, Timezone.getSystem(), new Timezone(ZoneId.of("America/Los_Angeles")), new Timezone(ZoneId.of("Japan")));
    private static final long serialVersionUID = 1L;

    public SettingsDialog(FullCalendar calendar) {
        setDraggable(true);

        VerticalLayout layout = new VerticalLayout();
        Timezone initialTimezone = calendar.getTimezone();

        Button toogleFixedWeekCount = new Button("Toggle fixedWeekCount", event -> {
            calendar.setFixedWeekCount(!calendar.getFixedWeekCount());
            Notification.show("Updated fixedWeekCount value from " + Boolean.toString(!calendar.getFixedWeekCount()) + " to " + Boolean.toString(calendar.getFixedWeekCount()));
        });


        List<Locale> items = Arrays.asList(CalendarLocale.getAvailableLocales());
        ComboBox<Locale> comboBoxLocales = new ComboBox<>("Locale");
        comboBoxLocales.setItems(items);
        comboBoxLocales.setValue(calendar.getLocale());
        comboBoxLocales.addValueChangeListener(event -> {
            Locale value = event.getValue();
            calendar.setLocale(value != null ? value : CalendarLocale.getDefault());
            Notification.show("Locale changed to " + calendar.getLocale().toLanguageTag());
        });
        comboBoxLocales.setRequired(true);
        comboBoxLocales.setPreventInvalidInput(true);

        Checkbox showOnlySomeTimezones = new Checkbox("Show only some timezones", true);

        ComboBox<Timezone> timezoneComboBox = new ComboBox<>("Timezone");
        timezoneComboBox.setItemLabelGenerator(Timezone::getClientSideValue);
        updateTimezonesComboBox(calendar, timezoneComboBox, showOnlySomeTimezones.getValue());
        timezoneComboBox.addValueChangeListener(event -> {
            if (!Objects.equals(calendar.getTimezone(), event.getValue())) {

                Timezone value = event.getValue();
                calendar.setTimezone(value != null ? value : initialTimezone);
                Notification.show("Timezone changed to " + calendar.getTimezone());
            }
        });
        showOnlySomeTimezones.addValueChangeListener(event -> updateTimezonesComboBox(calendar, timezoneComboBox, event.getValue()));

        VerticalLayout timeboxLayout = new VerticalLayout(timezoneComboBox, showOnlySomeTimezones);
        timeboxLayout.setPadding(false);
        timeboxLayout.setSpacing(false);

        layout.add(toogleFixedWeekCount, comboBoxLocales, timeboxLayout);
        add(layout);
    }

    private void updateTimezonesComboBox(FullCalendar calendar, ComboBox<Timezone> timezoneComboBox, boolean showOnlySome) {
        if (showOnlySome) {
            timezoneComboBox.setItems(SOME_TIMEZONES);
        } else {
            timezoneComboBox.setItems(Timezone.getAvailableZones());
        }

        if (!SOME_TIMEZONES.contains(calendar.getTimezone())) {
            timezoneComboBox.setValue(Timezone.UTC);
        } else {
            timezoneComboBox.setValue(calendar.getTimezone());
        }
    }
}
