package org.vaadin.stefan.fullcalendar.prototype;

import lombok.Getter;
import lombok.Setter;
import org.vaadin.stefan.fullcalendar.converters.LocaleConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;

import java.util.Locale;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. Options of the whole calendar. Holds the add-on defaults as
 * initial values.
 */
@Getter
@Setter
public class CalendarOptions extends ViewOptions {

    // FC-UPDATE: check the global options of the new FullCalendar version against these fields, see the note in
    // ViewOptions.

    /**
     * The locale of the calendar texts. Not named {@code locale}, because {@code getLocale()} would override
     * {@code Component#getLocale()}, the locale of the UI.
     */
    @JsonName("locale")
    @JsonConverter(LocaleConverter.class)
    private Locale calendarLocale;

    public CalendarOptions() {
        setEditable(true); // add-on default, replaces addonDefaultOptions
    }
}
