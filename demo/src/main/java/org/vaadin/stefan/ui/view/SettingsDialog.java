package org.vaadin.stefan.ui.view;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.vaadin.stefan.fullcalendar.CalendarLocale;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.Timezone;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;

public class SettingsDialog extends Dialog {
	private static final long serialVersionUID = 1L;
	
	public SettingsDialog(FullCalendar calendar, Timezone timezone) {
        FormLayout layout = new FormLayout();
        
        Button toogleFixedWeekCount = new Button("Toggle fixedWeekCount", event -> {
        	calendar.setFixedWeekCount(!calendar.getFixedWeekCount());
        	Notification.show("Updated fixedWeekCount value from " + Boolean.toString(!calendar.getFixedWeekCount()) + " to " + Boolean.toString(calendar.getFixedWeekCount()));
        });
        
        toogleFixedWeekCount.setWidthFull();
        
        layout.add(toogleFixedWeekCount);
        
        List<Locale> items = Arrays.asList(CalendarLocale.getAvailableLocales());
        ComboBox<Locale> comboBoxLocales = new ComboBox<>();
        comboBoxLocales.setItems(items);
        comboBoxLocales.setValue(CalendarLocale.getDefault());
        comboBoxLocales.addValueChangeListener(event -> calendar.setLocale(event.getValue()));
        comboBoxLocales.setRequired(true);
        comboBoxLocales.setPreventInvalidInput(true);
        comboBoxLocales.setWidthFull();
        
        layout.add(comboBoxLocales);

        ComboBox<Timezone> timezoneComboBox = new ComboBox<>("");
        timezoneComboBox.setItemLabelGenerator(Timezone::getClientSideValue);
        timezoneComboBox.setItems(Timezone.getAvailableZones());
        timezoneComboBox.setValue(timezone != null ? timezone : Timezone.UTC);
        timezoneComboBox.addValueChangeListener(event -> {
            Timezone value = event.getValue();
            calendar.setTimezone(value != null ? value : Timezone.UTC);
        });
        timezoneComboBox.setWidthFull();
        
        layout.add(timezoneComboBox);

        add(layout);
    }
}
