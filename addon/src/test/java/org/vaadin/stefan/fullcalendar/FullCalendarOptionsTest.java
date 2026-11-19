package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.option.InitialDateValue;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FullCalendarOptionsTest {

    @Test
    void initialDate_isKeptAsGiven() {
        FullCalendar calendar = new FullCalendar();
        LocalDate date = LocalDate.of(2025, 3, 1);

        calendar.options().setInitialDate(date);

        assertEquals(InitialDateValue.ofDay(date), calendar.options().getInitialDate());
    }

    @Test
    void rawOption_isRemovedBySettingNull() {
        FullCalendar calendar = new FullCalendar();

        calendar.options().setRawOption("weekends", false);
        calendar.options().setRawOption("weekends", null);

        assertEquals(Optional.empty(), calendar.options().getRawOption("weekends"));
    }

    @Test
    void optionOfTheInitialJsonOptionsIsNoFieldValue() {
        ObjectNode initialOptions = JsonFactory.createObject();
        initialOptions.put("editable", false);
        FullCalendar calendar = new FullCalendar(initialOptions);

        assertNull(calendar.options().getEditable());

        calendar.options().setEditable(true);
        calendar.options().setEditable(null);
        // the client falls back to the initial JSON options
        assertNull(calendar.options().getEditable());
    }
}
