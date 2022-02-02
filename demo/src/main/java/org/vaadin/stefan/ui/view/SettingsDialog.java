package org.vaadin.stefan.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import org.vaadin.stefan.fullcalendar.CalendarLocale;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.Timezone;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SettingsDialog extends Dialog {
    private static final long serialVersionUID = 1L;

    public SettingsDialog(FullCalendar calendar) {
        setDraggable(true);

        FormLayout layout = new FormLayout();
        Timezone initialTimezone = calendar.getTimezoneClient();

        Button toogleFixedWeekCount = new Button("Toggle fixedWeekCount", event -> {
            calendar.setFixedWeekCount(!calendar.getFixedWeekCount());
            Notification.show("Updated fixedWeekCount value from " + Boolean.toString(!calendar.getFixedWeekCount()) + " to " + Boolean.toString(calendar.getFixedWeekCount()));
        });

        toogleFixedWeekCount.setWidthFull();

        layout.add(toogleFixedWeekCount);

        List<Locale> items = Arrays.asList(CalendarLocale.getAvailableLocales());
        ComboBox<Locale> comboBoxLocales = new ComboBox<>();
        comboBoxLocales.setItems(items);
        comboBoxLocales.setValue(calendar.getLocale());
        comboBoxLocales.addValueChangeListener(event -> {
            Locale value = event.getValue();
            calendar.setLocale(value != null ? value : CalendarLocale.getDefault());
            Notification.show("Locale changed to " + calendar.getLocale().toLanguageTag());
        });
        comboBoxLocales.setRequired(true);
        comboBoxLocales.setPreventInvalidInput(true);
        comboBoxLocales.setWidthFull();

        layout.add(comboBoxLocales);

        ComboBox<Timezone> timezoneComboBox = new ComboBox<>("");
        timezoneComboBox.setItemLabelGenerator(Timezone::getClientSideValue);
//        timezoneComboBox.setItems(Timezone.UTC, Timezone.getSystem(), new Timezone(ZoneId.of("America/Los_Angeles")));
        timezoneComboBox.setItems(Timezone.getAvailableZones());
        timezoneComboBox.setValue(calendar.getTimezoneClient());
        timezoneComboBox.addValueChangeListener(event -> {
            Timezone value = event.getValue();
            calendar.setTimezoneClient(value != null ? value : initialTimezone);
            Notification.show("Timezone changed to " + calendar.getTimezoneClient());
        });
        timezoneComboBox.setWidthFull();

        layout.add(timezoneComboBox);

        add(layout);
    }
}
