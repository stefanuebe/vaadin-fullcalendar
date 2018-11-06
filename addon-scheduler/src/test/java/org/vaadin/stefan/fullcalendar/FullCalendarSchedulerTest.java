package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class FullCalendarSchedulerTest {
    @Test
    void testSetLicenseKey() {
        FullCalendarScheduler scheduler = new FullCalendarScheduler();
        scheduler.setSchedulerLicenseKey("123456");

        Optional<Object> option = scheduler.getOption("schedulerLicenseKey");

        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals("123456", option.get());
    }
}
