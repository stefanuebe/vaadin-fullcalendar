package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FullCalendarBuilderTest {

    private FullCalendarBuilder builder;

    @BeforeAll
    static void beforeAll() {
        TestUtils.initVaadinService(FullCalendarSchedulerTest.COMPONENT_HTMLS);
    }

    @BeforeEach
    public void init() {
        builder = FullCalendarBuilder.create();
    }

    @Test
    void testDontFailOnUsingScheduler() {
        FullCalendar build = builder.withScheduler().build();

        Assertions.assertEquals(FullCalendarScheduler.class, build.getClass());
        Assertions.assertTrue(build instanceof Scheduler);
    }
}
