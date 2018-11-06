package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class FullCalendarSchedulerTest {
    public static final String[] COMPONENT_HTMLS = {"fullcalendar/full-calendar.html", "fullcalendar/full-calendar-scheduler.html"};
    private FullCalendarScheduler calendar;

    @BeforeAll
    static void beforeAll() {
        TestUtils.initVaadinService(COMPONENT_HTMLS);
    }

    @BeforeEach
    void beforeEach() {
        calendar = new FullCalendarScheduler();
    }

    @Test
    void testSetLicenseKey() {
        calendar.setSchedulerLicenseKey("123456");

        Optional<Object> option = calendar.getOption("schedulerLicenseKey");

        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals("123456", option.get());
    }

    @Test
    void testNonArgsConstructor() {

        // this shall assure that all init options are handled
        assertExistingOptionCount(calendar, 1);
        Assertions.assertSame(CalendarLocale.getDefault(), calendar.getLocale());
    }

    @Test
    void testArgsConstructor() {
        int entryLimit = 5;
        FullCalendar calendar = new FullCalendarScheduler(entryLimit);

        // this shall assure that all init options are handled
        assertExistingOptionCount(calendar, 1);
        Assertions.assertSame(CalendarLocale.getDefault(), calendar.getLocale());

        Assertions.assertEquals(entryLimit, calendar.getElement().getProperty("eventLimit", -1));
    }

    private void assertExistingOptionCount(FullCalendar calendar, int expectedOptionsCount) {
        Assertions.assertEquals(expectedOptionsCount, Arrays.stream(FullCalendar.Option.values()).map(calendar::getOption).filter(Optional::isPresent).count());
    }


    @Test
    void testEmptyOptionalOnFetchingNonExistingResourceById() {
        Optional<Resource> optional = calendar.getResourceById("");
        Assertions.assertNotNull(optional);
        Assertions.assertFalse(optional.isPresent());
    }

    @Test
    void testFetchingExistingResourceById() {
        Resource resource = new Resource();
        calendar.addResource(resource);

        Optional<Resource> optional = calendar.getResourceById(resource.getId());
        Assertions.assertNotNull(optional);
        assertOptionalEquals(resource, optional);
    }

    @Test
    void testAddResource() {
        Resource resource1 = new Resource();
        Resource resource2 = new Resource();
        Resource resource3 = new Resource();

        calendar.addResource(resource1);
        calendar.addResource(resource2);
        calendar.addResource(resource3);

        Collection<Resource> resources = calendar.getResources();
        Assertions.assertEquals(3, resources.size());

        Assertions.assertTrue(resources.contains(resource1));
        Assertions.assertTrue(resources.contains(resource2));
        Assertions.assertTrue(resources.contains(resource3));

        assertOptionalEquals(resource1, calendar.getResourceById(resource1.getId()));
        assertOptionalEquals(resource2, calendar.getResourceById(resource2.getId()));
        assertOptionalEquals(resource3, calendar.getResourceById(resource3.getId()));
    }

    @Test
    void testRemoveContent() {

        Resource resource1 = new Resource();
        Resource resource2 = new Resource();
        Resource resource3 = new Resource();

        calendar.addResource(resource1);
        calendar.addResource(resource2);
        calendar.addResource(resource3);

        calendar.removeResource(resource2);

        Collection<Resource> resources = calendar.getResources();
        Assertions.assertEquals(2, resources.size());

        Assertions.assertTrue(resources.contains(resource1));
        Assertions.assertFalse(resources.contains(resource2));
        Assertions.assertTrue(resources.contains(resource3));

        assertOptionalEquals(resource1, calendar.getResourceById(resource1.getId()));
        assertOptionalEquals(resource3, calendar.getResourceById(resource3.getId()));

        Assertions.assertFalse(calendar.getResourceById(resource2.getId()).isPresent());
    }

    @Test
    void testInitialEmptyCollection() {
        Collection<Resource> resources = calendar.getResources();
        Assertions.assertNotNull(resources);
        Assertions.assertEquals(0, resources.size());
    }

    @Test
    void testRemoveAll() {
        calendar.addResource(new Resource());
        calendar.addResource(new Resource());
        calendar.addResource(new Resource());

        Assertions.assertEquals(3, calendar.getResources().size());

        calendar.removeAllResources();
        Assertions.assertEquals(0, calendar.getResources().size());
    }

    @Test
    void testGetResourcesReturnListCopy() {
        calendar.addResource(new Resource());
        calendar.addResource(new Resource());
        calendar.addResource(new Resource());

        Collection<Resource> resources = calendar.getResources();
        Assertions.assertEquals(3, resources.size());

        calendar.removeAllResources();
        Assertions.assertEquals(3, resources.size());
    }

    private <T> void assertOptionalEquals(T expected, Optional<T> value) {
        Assertions.assertTrue(value.isPresent());
        Assertions.assertEquals(expected, value.get());
    }
}
