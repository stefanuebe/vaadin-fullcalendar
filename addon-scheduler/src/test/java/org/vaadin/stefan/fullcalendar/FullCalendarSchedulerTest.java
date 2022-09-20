package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.EagerInMemoryEntryProvider;

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
    void testSetResourceAreaHeaderContent() {
        calendar.setResourceAreaHeaderContent("Hello");

        Optional<Object> option = calendar.getOption("resourceAreaHeaderContent");

        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals("Hello", option.get());
    }
    
    @Test
    void testSetResourceAreaWidtht() {
        calendar.setResourceAreaWidth("10%");

        Optional<Object> option = calendar.getOption("resourceAreaWidth");

        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals("10%", option.get());
    }
    
    @Test
    void testSetSlotWidtht() {
        calendar.setSlotMinWidth("100");

        Optional<Object> option = calendar.getOption("slotMinWidth");

        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals("100", option.get());
    }
    
    @Test
    void testSetResourceInitiallyExpanded() {
        calendar.setResourcesInitiallyExpanded(false);

        Optional<Object> option = calendar.getOption("resourcesInitiallyExpanded");

        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(false, option.get());
    }
    
    @Test
    void testSetFilterResourcesWithEvents() {
        calendar.setFilterResourcesWithEvents(true);

        Optional<Object> option = calendar.getOption("filterResourcesWithEvents");

        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(true, option.get());
    }
    
    @Test
    void testSetResourceOrder() {
        calendar.setResourceOrder("-title");

        Optional<Object> option = calendar.getOption("resourceOrder");

        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals("-title", option.get());
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

        Assertions.assertEquals(entryLimit, calendar.getElement().getProperty("dayMaxEvents", -1));
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
        calendar.addResources(resource);

        Optional<Resource> optional = calendar.getResourceById(resource.getId());
        Assertions.assertNotNull(optional);
        assertOptionalEquals(resource, optional);
    }

    @Test
    void testAddResources() {
        Resource resource1 = new Resource();
        Resource resource2 = new Resource();
        Resource resource3 = new Resource();

        calendar.addResources(resource1, resource2, resource3);

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

        calendar.addResources(resource1, resource2, resource3);
        calendar.removeResources(resource2);

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
        calendar.addResources(new Resource(), new Resource(), new Resource());

        Assertions.assertEquals(3, calendar.getResources().size());

        calendar.removeAllResources();
        Assertions.assertEquals(0, calendar.getResources().size());
    }

    @Test
    void testGetResourcesReturnListCopy() {
        calendar.addResources(new Resource(), new Resource(), new Resource());

        Collection<Resource> resources = calendar.getResources();
        Assertions.assertEquals(3, resources.size());

        calendar.removeAllResources();
        Assertions.assertEquals(3, resources.size());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // we want that here explicitely
    private <T> void assertOptionalEquals(T expected, Optional<T> value) {
        Assertions.assertTrue(value.isPresent());
        Assertions.assertEquals(expected, value.get());
    }

    @Test
    void testAddEntriesArray() {
        FullCalendar calendar = new FullCalendar();

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        calendar.setEntries(entry1, entry2, entry3, entry3);

        Collection<Entry> entries = getEntryProvider(calendar).getEntries();
        Assertions.assertEquals(3, entries.size());

        Assertions.assertTrue(entries.contains(entry1));
        Assertions.assertTrue(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));

        assertOptionalEquals(entry1, getEntryProvider(calendar).getEntryById(entry1.getId()));
        assertOptionalEquals(entry2, getEntryProvider(calendar).getEntryById(entry2.getId()));
        assertOptionalEquals(entry3, getEntryProvider(calendar).getEntryById(entry3.getId()));
    }

    @Test
    void testAddEntriesIterable() {
        FullCalendar calendar = new FullCalendar();

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        calendar.setEntries(Arrays.asList(entry1, entry2, entry3, entry3));

        Collection<Entry> entries = getEntryProvider(calendar).getEntries();
        Assertions.assertEquals(3, entries.size());

        Assertions.assertTrue(entries.contains(entry1));
        Assertions.assertTrue(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));

        assertOptionalEquals(entry1, getEntryProvider(calendar).getEntryById(entry1.getId()));
        assertOptionalEquals(entry2, getEntryProvider(calendar).getEntryById(entry2.getId()));
        assertOptionalEquals(entry3, getEntryProvider(calendar).getEntryById(entry3.getId()));
    }

    @Test
    void testRemoveEntriesArray() {
        FullCalendar calendar = new FullCalendar();

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        calendar.setEntries(entry1, entry2, entry3);
        getEntryProvider(calendar).removeEntries(entry1, entry2);

        Collection<Entry> entries = getEntryProvider(calendar).getEntries();
        Assertions.assertEquals(1, entries.size());

        Assertions.assertFalse(entries.contains(entry1));
        Assertions.assertFalse(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));
    }

    @Test
    void testRemoveEntriesIterable() {
        FullCalendar calendar = new FullCalendar();

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        getEntryProvider(calendar).addEntries(entry1, entry2, entry3);
        getEntryProvider(calendar).removeEntries(Arrays.asList(entry1, entry2));

        Collection<Entry> entries = getEntryProvider(calendar).getEntries();
        Assertions.assertEquals(1, entries.size());

        Assertions.assertFalse(entries.contains(entry1));
        Assertions.assertFalse(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));
    }

    private EagerInMemoryEntryProvider<Entry> getEntryProvider(FullCalendar calendar) {
        return calendar.getEntryProvider();
    }
}
