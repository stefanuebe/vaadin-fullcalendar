package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerCallbackOptionTest {

    @Test
    void allEntriesHaveNonNullClientSideValue() {
        for (FullCalendarScheduler.SchedulerCallbackOption opt : FullCalendarScheduler.SchedulerCallbackOption.values()) {
            assertNotNull(opt.getClientSideValue(), opt.name() + " has null clientSideValue");
            assertFalse(opt.getClientSideValue().isBlank(), opt.name() + " has blank clientSideValue");
        }
    }

    @Test
    void allClientSideValuesAreUnique() {
        long unique = Arrays.stream(FullCalendarScheduler.SchedulerCallbackOption.values())
            .map(FullCalendarScheduler.SchedulerCallbackOption::getClientSideValue)
            .distinct()
            .count();
        assertEquals(FullCalendarScheduler.SchedulerCallbackOption.values().length, unique);
    }

    @Test
    void setCallbackOption_allSchedulerOptions_doesNotThrow() {
        FullCalendarScheduler calendar = FullCalendarBuilder.create()
            .withScheduler("GPL-My-Project-Is-Open-Source")
            .build();
        for (FullCalendarScheduler.SchedulerCallbackOption option : FullCalendarScheduler.SchedulerCallbackOption.values()) {
            assertDoesNotThrow(() ->
                calendar.setCallbackOption(option, "function() {}"),
                "setCallbackOption threw for " + option.name());
        }
    }
}
