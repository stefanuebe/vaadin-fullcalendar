package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventBusUtil;
import com.vaadin.flow.component.ComponentUtil;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.CalendarItemProvider;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CalendarItemDroppedSchedulerEvent and SchedulerCalendarItemChanges.
 */
@SuppressWarnings("ALL")
class CalendarItemDroppedSchedulerEventTest {

    // ---- Inner POJO ----

    static class Meeting {
        private final String id;
        private String subject;
        private LocalDateTime begin;
        private LocalDateTime finish;
        private boolean allDay;
        private Set<String> resourceIds;

        Meeting(String id, String subject, LocalDateTime begin, LocalDateTime finish) {
            this.id = id;
            this.subject = subject;
            this.begin = begin;
            this.finish = finish;
        }

        public String getId() { return id; }
        public String getSubject() { return subject; }
        public LocalDateTime getBegin() { return begin; }
        public void setBegin(LocalDateTime begin) { this.begin = begin; }
        public LocalDateTime getFinish() { return finish; }
        public void setFinish(LocalDateTime finish) { this.finish = finish; }
        public boolean isAllDay() { return allDay; }
        public void setAllDay(boolean allDay) { this.allDay = allDay; }
        public Set<String> getResourceIds() { return resourceIds; }
        public void setResourceIds(Set<String> resourceIds) { this.resourceIds = resourceIds; }
    }

    // ---- Helpers ----

    private CalendarItemPropertyMapper<Meeting> createMapper() {
        return CalendarItemPropertyMapper.of(Meeting.class)
                .id(Meeting::getId)
                .title(Meeting::getSubject)
                .start(Meeting::getBegin, Meeting::setBegin)
                .end(Meeting::getFinish, Meeting::setFinish)
                .allDay(Meeting::isAllDay, Meeting::setAllDay)
                .resourceIds(Meeting::getResourceIds, Meeting::setResourceIds);
    }

    private FullCalendarScheduler<Meeting> createSchedulerWithCachedItem(Meeting meeting) {
        FullCalendarScheduler<Meeting> scheduler = new FullCalendarScheduler<>();
        CalendarItemProvider<Meeting> provider = CalendarItemProvider.fromCallbacks(
                query -> List.of(meeting).stream(),
                id -> meeting.getId().equals(id) ? meeting : null
        );
        scheduler.setCalendarItemProvider(provider, createMapper());

        // Simulate a fetch so the item ends up in lastFetchedItems
        ObjectNode query = JsonFactory.createObject();
        scheduler.fetchEntriesFromServer(query);

        return scheduler;
    }

    private ObjectNode buildItemJson(String id, LocalDateTime start, LocalDateTime end, boolean allDay) {
        ObjectNode json = JsonFactory.createObject();
        json.put("id", id);
        if (start != null) json.put("start", JsonUtils.formatClientSideDateTimeString(start));
        if (end != null) json.put("end", JsonUtils.formatClientSideDateTimeString(end));
        json.put("allDay", allDay);
        return json;
    }

    private ObjectNode buildDeltaJson(int years, int months, int days, long milliseconds) {
        ObjectNode delta = JsonFactory.createObject();
        delta.put("years", years);
        delta.put("months", months);
        delta.put("days", days);
        delta.put("milliseconds", milliseconds);
        return delta;
    }

    // ---- Tests ----

    @Test
    void constructorResolvableViaComponentEventBusUtil() throws Exception {
        Constructor<CalendarItemDroppedSchedulerEvent> ctor =
                ComponentEventBusUtil.getEventConstructor(CalendarItemDroppedSchedulerEvent.class);
        assertNotNull(ctor);
    }

    @Test
    void resolvesOldAndNewResourceFromJson() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        Meeting meeting = new Meeting("m1", "Standup", begin, finish);

        FullCalendarScheduler<Meeting> scheduler = createSchedulerWithCachedItem(meeting);

        // Register resources
        Resource oldResource = new Resource("r-old", "Room A", null);
        Resource newResource = new Resource("r-new", "Room B", null);
        scheduler.addResources(oldResource, newResource);

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        itemJson.put("oldResource", "r-old");
        itemJson.put("newResource", "r-new");
        ObjectNode deltaJson = buildDeltaJson(0, 0, 0, 0L);

        CalendarItemDroppedSchedulerEvent<Meeting> event =
                new CalendarItemDroppedSchedulerEvent<>(scheduler, true, itemJson, deltaJson);

        assertTrue(event.getOldResource().isPresent());
        assertEquals("r-old", event.getOldResource().get().getId());

        assertTrue(event.getNewResource().isPresent());
        assertEquals("r-new", event.getNewResource().get().getId());
    }

    @Test
    void noResourceChanges_returnsEmptyOptionals() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        Meeting meeting = new Meeting("m1", "Standup", begin, finish);

        FullCalendarScheduler<Meeting> scheduler = createSchedulerWithCachedItem(meeting);

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        ObjectNode deltaJson = buildDeltaJson(0, 0, 1, 0L);

        CalendarItemDroppedSchedulerEvent<Meeting> event =
                new CalendarItemDroppedSchedulerEvent<>(scheduler, true, itemJson, deltaJson);

        assertFalse(event.getOldResource().isPresent());
        assertFalse(event.getNewResource().isPresent());
    }

    @Test
    void getSchedulerChanges_returnsTypedAccessor() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        Meeting meeting = new Meeting("m1", "Standup", begin, finish);

        FullCalendarScheduler<Meeting> scheduler = createSchedulerWithCachedItem(meeting);

        Resource oldRes = new Resource("r1", "Room 1", null);
        Resource newRes = new Resource("r2", "Room 2", null);
        scheduler.addResources(oldRes, newRes);

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        itemJson.put("oldResource", "r1");
        itemJson.put("newResource", "r2");
        ObjectNode deltaJson = buildDeltaJson(0, 0, 0, 0L);

        CalendarItemDroppedSchedulerEvent<Meeting> event =
                new CalendarItemDroppedSchedulerEvent<>(scheduler, true, itemJson, deltaJson);

        SchedulerCalendarItemChanges changes = event.getSchedulerChanges();
        assertNotNull(changes);

        assertTrue(changes.getOldResourceId().isPresent());
        assertEquals("r1", changes.getOldResourceId().get());

        assertTrue(changes.getNewResourceId().isPresent());
        assertEquals("r2", changes.getNewResourceId().get());

        assertTrue(changes.getOldResource(scheduler).isPresent());
        assertEquals(oldRes, changes.getOldResource(scheduler).get());

        assertTrue(changes.getNewResource(scheduler).isPresent());
        assertEquals(newRes, changes.getNewResource(scheduler).get());
    }

    @Test
    void schedulerChanges_noResourceChanges_returnsEmpty() {
        ObjectNode jsonDelta = JsonFactory.createObject();
        jsonDelta.put("start", JsonUtils.formatClientSideDateTimeString(LocalDateTime.now()));

        SchedulerCalendarItemChanges changes = new SchedulerCalendarItemChanges(jsonDelta);

        assertFalse(changes.getOldResourceId().isPresent());
        assertFalse(changes.getNewResourceId().isPresent());
    }

    @Test
    void listenerRegistration_firesEvent() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        Meeting meeting = new Meeting("m1", "Standup", begin, finish);

        FullCalendarScheduler<Meeting> scheduler = createSchedulerWithCachedItem(meeting);

        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        AtomicReference<Meeting> receivedItem = new AtomicReference<>();

        scheduler.addCalendarItemDroppedSchedulerListener(event -> {
            listenerCalled.set(true);
            receivedItem.set((Meeting) event.getItem());
        });

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        ObjectNode deltaJson = buildDeltaJson(0, 0, 1, 0L);

        ComponentUtil.fireEvent(scheduler,
                new CalendarItemDroppedSchedulerEvent<>(scheduler, true, itemJson, deltaJson));

        assertTrue(listenerCalled.get());
        assertSame(meeting, receivedItem.get());
    }

    @Test
    void addCalendarItemDroppedListener_delegatesToSchedulerVariant() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        Meeting meeting = new Meeting("m1", "Standup", begin, finish);

        FullCalendarScheduler<Meeting> scheduler = createSchedulerWithCachedItem(meeting);

        Resource oldRes = new Resource("r1", "Room 1", null);
        Resource newRes = new Resource("r2", "Room 2", null);
        scheduler.addResources(oldRes, newRes);

        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        // Use the base class listener method — should be delegated to scheduler variant
        scheduler.addCalendarItemDroppedListener(event -> {
            listenerCalled.set(true);
            // The event should actually be a CalendarItemDroppedSchedulerEvent
            if (event instanceof CalendarItemDroppedSchedulerEvent<?> schedulerEvent) {
                assertTrue(schedulerEvent.getOldResource().isPresent());
            }
        });

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        itemJson.put("oldResource", "r1");
        itemJson.put("newResource", "r2");
        ObjectNode deltaJson = buildDeltaJson(0, 0, 0, 0L);

        ComponentUtil.fireEvent(scheduler,
                new CalendarItemDroppedSchedulerEvent<>(scheduler, true, itemJson, deltaJson));

        assertTrue(listenerCalled.get());
    }

    // ---- Phase 8: Entry event hierarchy unification tests ----

    private FullCalendarScheduler<Entry> createEntrySchedulerWithCachedEntry(Entry entry) {
        FullCalendarScheduler<Entry> scheduler = new FullCalendarScheduler<>();
        InMemoryEntryProvider<Entry> entryProvider = EntryProvider.inMemoryFrom(entry);
        scheduler.setEntryProvider(entryProvider);

        // Simulate a fetch
        ObjectNode query = JsonFactory.createObject();
        scheduler.fetchEntriesFromServer(query);

        return scheduler;
    }

    @Test
    void entryDroppedSchedulerEvent_isInstanceOfCalendarItemDroppedSchedulerEvent() {
        ResourceEntry entry = new ResourceEntry("e1");
        entry.setTitle("Meeting");
        entry.setStart(LocalDateTime.of(2025, 6, 1, 9, 0));
        entry.setEnd(LocalDateTime.of(2025, 6, 1, 10, 0));

        FullCalendarScheduler<Entry> scheduler = createEntrySchedulerWithCachedEntry(entry);

        Resource oldRes = new Resource("r1", "Room 1", null);
        Resource newRes = new Resource("r2", "Room 2", null);
        scheduler.addResources(oldRes, newRes);

        ObjectNode itemJson = buildItemJson("e1", entry.getStart(), entry.getEnd(), false);
        itemJson.put("oldResource", "r1");
        itemJson.put("newResource", "r2");
        ObjectNode deltaJson = buildDeltaJson(0, 0, 1, 0L);

        EntryDroppedSchedulerEvent event = new EntryDroppedSchedulerEvent(scheduler, true, itemJson, deltaJson);

        assertInstanceOf(CalendarItemDroppedSchedulerEvent.class, event);
        assertInstanceOf(CalendarItemDroppedEvent.class, event);
        assertInstanceOf(CalendarItemTimeChangedEvent.class, event);
        assertInstanceOf(CalendarItemDataEvent.class, event);
        assertInstanceOf(CalendarItemEvent.class, event);
    }

    @Test
    void entryDroppedSchedulerEvent_getEntry_and_getItem_returnSameObject() {
        ResourceEntry entry = new ResourceEntry("e1");
        entry.setTitle("Meeting");
        entry.setStart(LocalDateTime.of(2025, 6, 1, 9, 0));
        entry.setEnd(LocalDateTime.of(2025, 6, 1, 10, 0));

        FullCalendarScheduler<Entry> scheduler = createEntrySchedulerWithCachedEntry(entry);

        ObjectNode itemJson = buildItemJson("e1", entry.getStart(), entry.getEnd(), false);
        ObjectNode deltaJson = buildDeltaJson(0, 0, 0, 0L);

        EntryDroppedSchedulerEvent event = new EntryDroppedSchedulerEvent(scheduler, true, itemJson, deltaJson);

        assertSame(event.getEntry(), event.getItem());
        assertSame(entry, event.getEntry());
    }

    @Test
    void entryDroppedSchedulerEvent_resourceResolution_fromParent() {
        ResourceEntry entry = new ResourceEntry("e1");
        entry.setTitle("Meeting");
        entry.setStart(LocalDateTime.of(2025, 6, 1, 9, 0));
        entry.setEnd(LocalDateTime.of(2025, 6, 1, 10, 0));

        FullCalendarScheduler<Entry> scheduler = createEntrySchedulerWithCachedEntry(entry);

        Resource oldRes = new Resource("r1", "Room 1", null);
        Resource newRes = new Resource("r2", "Room 2", null);
        scheduler.addResources(oldRes, newRes);

        ObjectNode itemJson = buildItemJson("e1", entry.getStart(), entry.getEnd(), false);
        itemJson.put("oldResource", "r1");
        itemJson.put("newResource", "r2");
        ObjectNode deltaJson = buildDeltaJson(0, 0, 0, 0L);

        EntryDroppedSchedulerEvent event = new EntryDroppedSchedulerEvent(scheduler, true, itemJson, deltaJson);

        // Resource resolution is inherited from CalendarItemDroppedSchedulerEvent
        assertTrue(event.getOldResource().isPresent());
        assertEquals("r1", event.getOldResource().get().getId());
        assertTrue(event.getNewResource().isPresent());
        assertEquals("r2", event.getNewResource().get().getId());
    }

    @Test
    void entryDroppedSchedulerEvent_applyChangesOnEntry_updatesResources() {
        ResourceEntry entry = new ResourceEntry("e1");
        entry.setTitle("Meeting");
        entry.setStart(LocalDateTime.of(2025, 6, 1, 9, 0));
        entry.setEnd(LocalDateTime.of(2025, 6, 1, 10, 0));

        FullCalendarScheduler<Entry> scheduler = createEntrySchedulerWithCachedEntry(entry);

        Resource oldRes = new Resource("r1", "Room 1", null);
        Resource newRes = new Resource("r2", "Room 2", null);
        scheduler.addResources(oldRes, newRes);
        entry.addResources(oldRes);

        ObjectNode itemJson = buildItemJson("e1", entry.getStart(), entry.getEnd(), false);
        itemJson.put("oldResource", "r1");
        itemJson.put("newResource", "r2");
        ObjectNode deltaJson = buildDeltaJson(0, 0, 0, 0L);

        EntryDroppedSchedulerEvent event = new EntryDroppedSchedulerEvent(scheduler, true, itemJson, deltaJson);
        event.applyChangesOnEntry();

        // After applyChangesOnEntry, old resource should be removed and new added
        assertFalse(entry.getResources().contains(oldRes));
        assertTrue(entry.getResources().contains(newRes));
    }
}
