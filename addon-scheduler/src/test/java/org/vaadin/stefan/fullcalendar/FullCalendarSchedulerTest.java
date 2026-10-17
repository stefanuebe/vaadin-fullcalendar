package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.Order;
import org.vaadin.stefan.fullcalendar.option.LocaleValue;
import org.vaadin.stefan.fullcalendar.option.CalendarLocale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import tools.jackson.databind.node.ObjectNode;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import org.vaadin.stefan.fullcalendar.option.WidthValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class FullCalendarSchedulerTest {
    public static final String[] COMPONENT_HTMLS = {"fullcalendar/full-calendar.html", "fullcalendar/full-calendar-scheduler.html"};
    private FullCalendarScheduler calendar;

    @BeforeEach
    void beforeEach() {
        calendar = new FullCalendarScheduler();
    }

    private ObjectNode initialOptions() {
        return (ObjectNode) calendar.getElement().getPropertyRaw("initialOptions");
    }

    @Test
    void testSetLicenseKey() {
        calendar.options().setSchedulerLicenseKey("123456");

        Assertions.assertEquals("123456", calendar.options().getSchedulerLicenseKey());
        Assertions.assertEquals("123456", initialOptions().get("schedulerLicenseKey").asString());
    }

    @Test
    void testSetResourceColumnHeaderContent() {
        calendar.options().setResourceColumnHeaderContent("Hello");

        Assertions.assertEquals(TextContent.of("Hello"), calendar.options().getResourceColumnHeaderContent());
        Assertions.assertEquals("Hello", initialOptions().get("resourceColumnHeaderContent").asString());
    }

    @Test
    void testSetResourceColumnsWidth() {
        calendar.options().setResourceColumnsWidth(WidthValue.ofPercent(10));

        Assertions.assertEquals(WidthValue.ofPercent(10), calendar.options().getResourceColumnsWidth());
        Assertions.assertEquals("10%", initialOptions().get("resourceColumnsWidth").asString());
    }

    @Test
    void testSetSlotWidtht() {
        calendar.options().setSlotMinWidth(100);

        Assertions.assertEquals(100, calendar.options().getSlotMinWidth());
        Assertions.assertEquals(100, initialOptions().get("slotMinWidth").asInt());
    }

    @Test
    void testSetResourceInitiallyExpanded() {
        calendar.options().setResourcesInitiallyExpanded(false);

        Assertions.assertEquals(false, calendar.options().getResourcesInitiallyExpanded());
        Assertions.assertFalse(initialOptions().get("resourcesInitiallyExpanded").asBoolean());
    }

    @Test
    void testSetFilterResourcesWithEvents() {
        calendar.options().setFilterResourcesWithEntries(true);

        Assertions.assertEquals(true, calendar.options().getFilterResourcesWithEntries());
        Assertions.assertTrue(initialOptions().get("filterResourcesWithEvents").asBoolean());
    }

    @Test
    void testSetResourceOrder() {
        calendar.options().setResourceOrder("-title");

        Assertions.assertEquals(Order.of("-title"), calendar.options().getResourceOrder());
        Assertions.assertEquals("-title", initialOptions().get("resourceOrder").get(0).asString());
    }

    @Test
    void noArgConstructor_setsDefaultLocale() {
        Assertions.assertEquals(LocaleValue.of(CalendarLocale.getDefaultLocale()),
                new FullCalendarScheduler().options().getLocale());
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

        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();
        entryProvider.addEntries(entry1, entry2, entry3, entry3);

        Collection<Entry> entries = entryProvider.getEntries();
        Assertions.assertEquals(3, entries.size());

        Assertions.assertTrue(entries.contains(entry1));
        Assertions.assertTrue(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));

        assertOptionalEquals(entry1, entryProvider.getEntryById(entry1.getId()));
        assertOptionalEquals(entry2, entryProvider.getEntryById(entry2.getId()));
        assertOptionalEquals(entry3, entryProvider.getEntryById(entry3.getId()));
    }

    @Test
    void testAddEntriesIterable() {
        FullCalendar calendar = new FullCalendar();

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();
        entryProvider.addEntries(Arrays.asList(entry1, entry2, entry3, entry3));

        Collection<Entry> entries = entryProvider.getEntries();
        Assertions.assertEquals(3, entries.size());

        Assertions.assertTrue(entries.contains(entry1));
        Assertions.assertTrue(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));

        assertOptionalEquals(entry1, entryProvider.getEntryById(entry1.getId()));
        assertOptionalEquals(entry2, entryProvider.getEntryById(entry2.getId()));
        assertOptionalEquals(entry3, entryProvider.getEntryById(entry3.getId()));
    }

    @Test
    void testRemoveEntriesArray() {
        FullCalendar calendar = new FullCalendar();

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();
        entryProvider.addEntries(entry1, entry2, entry3);
        entryProvider.removeEntries(entry1, entry2);

        Collection<Entry> entries = entryProvider.getEntries();
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

        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();
        entryProvider.addEntries(entry1, entry2, entry3);
        entryProvider.removeEntries(Arrays.asList(entry1, entry2));

        Collection<Entry> entries = entryProvider.getEntries();
        Assertions.assertEquals(1, entries.size());

        Assertions.assertFalse(entries.contains(entry1));
        Assertions.assertFalse(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));
    }

    private InMemoryEntryProvider<Entry> getEntryProvider(FullCalendar calendar) {
        return calendar.getEntryProvider();
    }
}
