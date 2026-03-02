package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FullCalendarBuilderTest {

    public static final String EVENT_LIMIT = "eventLimit";
    private FullCalendarBuilder builder;

    @BeforeEach
    public void init() {
        builder = FullCalendarBuilder.create();
    }

    @Test
    void testBuildingDefaultInstance() {
        FullCalendar build = builder.build();

        Assertions.assertEquals(FullCalendar.class, build.getClass());
        Assertions.assertFalse(Boolean.parseBoolean(build.getElement().getProperty(EVENT_LIMIT)));
    }

    @Test
    void testFailOnUsingScheduler() {
        builder = builder.withScheduler();
        Assertions.assertThrows(FullCalendarBuilder.ExtensionNotFoundException.class, () -> builder.build());
    }

    void testEntryLimitFalseOnNegativeNumber() {
        builder = builder.withCalendarItemLimit(-1);

        Assertions.assertFalse(Boolean.parseBoolean(builder.build().getElement().getProperty(EVENT_LIMIT)));
    }

    void testEntryLimitFalseOnZero() {
        builder = builder.withCalendarItemLimit(0);

        Assertions.assertFalse(Boolean.parseBoolean(builder.build().getElement().getProperty(EVENT_LIMIT)));
    }

    void testEntryLimit() {
        builder = builder.withCalendarItemLimit(17);

        Assertions.assertEquals(17, Integer.parseInt(builder.build().getElement().getProperty(EVENT_LIMIT)));
    }
}
