package org.vaadin.stefan.ui.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.stefan.fullcalendar.option.CalendarLocale;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import org.vaadin.stefan.ui.view.CalendarViewToolbar;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SettingsDialog extends Dialog {
    public static final List<Timezone> SOME_TIMEZONES = Arrays.asList(Timezone.LOCAL, Timezone.UTC, Timezone.getSystem(), new Timezone(ZoneId.of("America/Los_Angeles")), new Timezone(ZoneId.of("Japan")));
    private static final long serialVersionUID = 1L;

    public SettingsDialog(FullCalendar calendar) {
        setDraggable(true);

        VerticalLayout layout = new VerticalLayout();
        Timezone initialTimezone = calendar.options().getTimeZone();

        Button toogleFixedWeekCount = new Button("Toggle fixedWeekCount", event -> {
            Boolean fixedWeekCount = calendar.options().getFixedWeekCount();
            boolean current = fixedWeekCount == null || fixedWeekCount; // the library default is true
            calendar.options().setFixedWeekCount(!current);
            Notification.show("Updated fixedWeekCount to " + !current);
        });

        List<Locale> items = Arrays.asList(CalendarLocale.getAvailableLocales());
        ComboBox<Locale> comboBoxLocales = new ComboBox<>("Locale");
        comboBoxLocales.setItems(items);
        comboBoxLocales.setValue(CalendarViewToolbar.getCalendarLocale(calendar));
        comboBoxLocales.addValueChangeListener(event -> {
            Locale value = event.getValue();
            Locale newLocale = value != null ? value : CalendarLocale.getDefaultLocale();
            calendar.options().setLocale(newLocale);
            Notification.show("Locale changed to " + newLocale.toLanguageTag());
        });
        comboBoxLocales.setRequired(true);

        Checkbox showOnlySomeTimezones = new Checkbox("Show only some timezones", true);

        ComboBox<Timezone> timezoneComboBox = new ComboBox<>("Timezone");
        timezoneComboBox.setItemLabelGenerator(Timezone::getClientSideValue);
        updateTimezonesComboBox(calendar, timezoneComboBox, showOnlySomeTimezones.getValue());
        timezoneComboBox.addValueChangeListener(event -> {
            if (!Objects.equals(calendar.options().getTimeZone(), event.getValue())) {

                Timezone value = event.getValue();
                calendar.options().setTimeZone(value != null ? value : initialTimezone);
                Notification.show("Timezone changed to " + calendar.options().getTimeZone());
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

        if (!SOME_TIMEZONES.contains(calendar.options().getTimeZone())) {
            timezoneComboBox.setValue(Timezone.UTC);
        } else {
            timezoneComboBox.setValue(calendar.options().getTimeZone());
        }
    }
}
