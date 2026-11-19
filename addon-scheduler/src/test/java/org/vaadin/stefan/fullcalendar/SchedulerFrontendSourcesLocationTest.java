package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Vaadin 25.2+ warns about add-ons that ship bundle sources under META-INF/resources/frontend (#252).
 */
class SchedulerFrontendSourcesLocationTest {

    @Test
    void frontendSourcesLiveInMetaInfFrontend() {
        ClassLoader classLoader = getClass().getClassLoader();

        assertNotNull(classLoader.getResource("META-INF/frontend/vaadin-full-calendar/full-calendar-scheduler.ts"));
        assertNull(classLoader.getResource("META-INF/resources/frontend/vaadin-full-calendar/full-calendar-scheduler.ts"));
    }
}
