package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerCallbackOptionTest {

    @Test
    void allEntriesHaveNonNullClientSideValue() {
        for (SchedulerCallbackOption opt : SchedulerCallbackOption.values()) {
            assertNotNull(opt.getClientSideValue(), opt.name() + " has null clientSideValue");
            assertFalse(opt.getClientSideValue().isBlank(), opt.name() + " has blank clientSideValue");
        }
    }

    @Test
    void allClientSideValuesAreUnique() {
        long unique = Arrays.stream(SchedulerCallbackOption.values())
            .map(SchedulerCallbackOption::getClientSideValue)
            .distinct()
            .count();
        assertEquals(SchedulerCallbackOption.values().length, unique);
    }

    @Test
    void setCallbackOption_allSchedulerOptions_doesNotThrow() {
        FullCalendarScheduler calendar = FullCalendarBuilder.create()
            .withScheduler("GPL-My-Project-Is-Open-Source")
            .build();
        for (SchedulerCallbackOption option : SchedulerCallbackOption.values()) {
            assertDoesNotThrow(() ->
                calendar.setCallbackOption(option.getClientSideValue(), "function() {}"),
                "setCallbackOption threw for " + option.name());
        }
    }
}
