package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CallbackOptionTest {

    @Test
    void allEntriesHaveNonNullClientSideValue() {
        for (FullCalendar.CallbackOption opt : FullCalendar.CallbackOption.values()) {
            assertNotNull(opt.getClientSideValue(), opt.name() + " has null clientSideValue");
            assertFalse(opt.getClientSideValue().isBlank(), opt.name() + " has blank clientSideValue");
        }
    }

    @Test
    void allClientSideValuesAreUnique() {
        long unique = Arrays.stream(FullCalendar.CallbackOption.values())
            .map(FullCalendar.CallbackOption::getClientSideValue)
            .distinct()
            .count();
        assertEquals(FullCalendar.CallbackOption.values().length, unique, "Duplicate clientSideValue found");
    }

    @Test
    void setCallbackOption_enum_doesNotThrow() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        assertDoesNotThrow(() ->
            calendar.setCallbackOption(FullCalendar.CallbackOption.DAY_CELL_CLASS_NAMES,
                "function(arg) { return []; }"));
    }

    @Test
    void setCallbackOption_stringKey_doesNotThrow() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        assertDoesNotThrow(() ->
            calendar.setCallbackOption("dayCellClassNames",
                "function(arg) { return []; }"));
    }

    @Test
    void setCallbackOption_null_doesNotThrow() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        assertDoesNotThrow(() ->
            calendar.setCallbackOption(FullCalendar.CallbackOption.DROP_ACCEPT, null));
    }

    @Test
    void setCallbackOption_blank_treatedAsNull_doesNotThrow() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        assertDoesNotThrow(() ->
            calendar.setCallbackOption(FullCalendar.CallbackOption.DROP_ACCEPT, "  "));
    }

    @Test
    void setCallbackOption_stringKey_null_doesNotThrow() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        assertDoesNotThrow(() ->
            calendar.setCallbackOption("dropAccept", null));
    }

    @Test
    void setCallbackOption_allOptions_doesNotThrow() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        for (FullCalendar.CallbackOption option : FullCalendar.CallbackOption.values()) {
            assertDoesNotThrow(() ->
                calendar.setCallbackOption(option, "function() {}"),
                "setCallbackOption threw for " + option.name());
        }
    }
}
