package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventBusUtil;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.CalendarItemProvider;

import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the CIP parallel event hierarchy:
 * CalendarItemEvent, CalendarItemDataEvent, and the 5 concrete @DomEvent subclasses.
 */
@SuppressWarnings("ALL")
class CalendarItemEventTest {

    // ---- Inner POJO for tests ----

    static class TestMeeting {
        private final String id;
        private String subject;
        private LocalDateTime begin;
        private LocalDateTime finish;
        private boolean allDay;

        TestMeeting(String id, String subject, LocalDateTime begin, LocalDateTime finish, boolean allDay) {
            this.id = id;
            this.subject = subject;
            this.begin = begin;
            this.finish = finish;
            this.allDay = allDay;
        }

        public String getId() { return id; }
        public String getSubject() { return subject; }
        public LocalDateTime getBegin() { return begin; }
        public void setBegin(LocalDateTime begin) { this.begin = begin; }
        public LocalDateTime getFinish() { return finish; }
        public void setFinish(LocalDateTime finish) { this.finish = finish; }
        public boolean isAllDay() { return allDay; }
        public void setAllDay(boolean allDay) { this.allDay = allDay; }
    }

    // ---- Helpers ----

    private CalendarItemPropertyMapper<TestMeeting> createReadOnlyMapper() {
        return CalendarItemPropertyMapper.of(TestMeeting.class)
                .id(TestMeeting::getId)
                .title(TestMeeting::getSubject)
                .start(TestMeeting::getBegin)
                .end(TestMeeting::getFinish)
                .allDay(TestMeeting::isAllDay);
    }

    private CalendarItemPropertyMapper<TestMeeting> createBidirectionalMapper() {
        return CalendarItemPropertyMapper.of(TestMeeting.class)
                .id(TestMeeting::getId)
                .title(TestMeeting::getSubject)
                .start(TestMeeting::getBegin, TestMeeting::setBegin)
                .end(TestMeeting::getFinish, TestMeeting::setFinish)
                .allDay(TestMeeting::isAllDay, TestMeeting::setAllDay);
    }

    private CalendarItemProvider<TestMeeting> createTestProvider(List<TestMeeting> meetings) {
        return CalendarItemProvider.fromCallbacks(
                query -> meetings.stream(),
                id -> meetings.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null)
        );
    }

    /**
     * Sets up a CIP calendar with a single meeting in cache (simulates a fetch).
     */
    private FullCalendar<TestMeeting> createCIPCalendarWithCachedItem(TestMeeting meeting,
            CalendarItemPropertyMapper<TestMeeting> mapper) {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of(meeting));
        calendar.setCalendarItemProvider(provider, mapper);

        // Simulate a fetch so the item ends up in lastFetchedItems
        ObjectNode query = JsonFactory.createObject();
        calendar.fetchEntriesFromServer(query);

        return calendar;
    }

    /** Builds a minimal JSON object that looks like a FullCalendar eventClick event payload. */
    private ObjectNode buildItemJson(String id, LocalDateTime start, LocalDateTime end, boolean allDay) {
        ObjectNode json = JsonFactory.createObject();
        json.put("id", id);
        if (start != null) json.put("start", JsonUtils.formatClientSideDateTimeString(start));
        if (end != null) json.put("end", JsonUtils.formatClientSideDateTimeString(end));
        json.put("allDay", allDay);
        return json;
    }

    /** Builds a minimal delta JSON object. */
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
    void calendarItemClickedEvent_constructorResolvable() throws Exception {
        Constructor<CalendarItemClickedEvent> ctor = ComponentEventBusUtil.getEventConstructor(CalendarItemClickedEvent.class);
        assertNotNull(ctor);
    }

    @Test
    void calendarItemMouseEnterEvent_constructorResolvable() throws Exception {
        Constructor<CalendarItemMouseEnterEvent> ctor = ComponentEventBusUtil.getEventConstructor(CalendarItemMouseEnterEvent.class);
        assertNotNull(ctor);
    }

    @Test
    void calendarItemMouseLeaveEvent_constructorResolvable() throws Exception {
        Constructor<CalendarItemMouseLeaveEvent> ctor = ComponentEventBusUtil.getEventConstructor(CalendarItemMouseLeaveEvent.class);
        assertNotNull(ctor);
    }

    @Test
    void calendarItemDroppedEvent_constructorResolvable() throws Exception {
        Constructor<CalendarItemDroppedEvent> ctor = ComponentEventBusUtil.getEventConstructor(CalendarItemDroppedEvent.class);
        assertNotNull(ctor);
    }

    @Test
    void calendarItemResizedEvent_constructorResolvable() throws Exception {
        Constructor<CalendarItemResizedEvent> ctor = ComponentEventBusUtil.getEventConstructor(CalendarItemResizedEvent.class);
        assertNotNull(ctor);
    }

    @Test
    void calendarItemClickedEvent_resolvesItemFromCache() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createReadOnlyMapper());

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        CalendarItemClickedEvent<TestMeeting> event = new CalendarItemClickedEvent<>(calendar, true, itemJson);

        assertNotNull(event.getItem());
        assertSame(meeting, event.getItem());
        assertEquals("m1", event.getItemId());
    }

    @Test
    void calendarItemDataEvent_getChanges_returnsTypedAccessor() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createReadOnlyMapper());

        LocalDateTime newBegin = LocalDateTime.of(2025, 6, 1, 10, 0);
        LocalDateTime newFinish = LocalDateTime.of(2025, 6, 1, 11, 0);
        ObjectNode itemJson = buildItemJson("m1", newBegin, newFinish, false);

        CalendarItemClickedEvent<TestMeeting> event = new CalendarItemClickedEvent<>(calendar, true, itemJson);

        CalendarItemChanges changes = event.getChanges();
        assertNotNull(changes);
        assertTrue(changes.getChangedStart().isPresent());
        assertEquals(newBegin, changes.getChangedStart().get());
        assertTrue(changes.getChangedEnd().isPresent());
        assertEquals(newFinish, changes.getChangedEnd().get());
    }

    @Test
    void applyChangesOnItem_strategyA_usesMapperSetters() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createBidirectionalMapper());

        LocalDateTime newBegin = LocalDateTime.of(2025, 6, 1, 10, 0);
        LocalDateTime newFinish = LocalDateTime.of(2025, 6, 1, 11, 0);
        ObjectNode itemJson = buildItemJson("m1", newBegin, newFinish, false);

        CalendarItemClickedEvent<TestMeeting> event = new CalendarItemClickedEvent<>(calendar, true, itemJson);
        TestMeeting result = event.applyChangesOnItem();

        assertSame(meeting, result);
        assertEquals(newBegin, meeting.getBegin());
        assertEquals(newFinish, meeting.getFinish());
    }

    @Test
    void applyChangesOnItem_strategyB_callsUpdateHandler() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createReadOnlyMapper());

        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        AtomicReference<TestMeeting> handlerItem = new AtomicReference<>();
        calendar.setCalendarItemUpdateHandler((item, changes) -> {
            handlerCalled.set(true);
            handlerItem.set(item);
        });

        LocalDateTime newBegin = LocalDateTime.of(2025, 6, 1, 10, 0);
        ObjectNode itemJson = buildItemJson("m1", newBegin, finish, false);

        CalendarItemClickedEvent<TestMeeting> event = new CalendarItemClickedEvent<>(calendar, true, itemJson);
        event.applyChangesOnItem();

        assertTrue(handlerCalled.get());
        assertSame(meeting, handlerItem.get());
    }

    @Test
    void calendarItemDroppedEvent_parsesDelta() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createBidirectionalMapper());

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        // 1 day + 2 hours (7200000 ms)
        ObjectNode deltaJson = buildDeltaJson(0, 0, 1, 7_200_000L);

        CalendarItemDroppedEvent<TestMeeting> event = new CalendarItemDroppedEvent<>(calendar, true, itemJson, deltaJson);

        Delta delta = event.getDelta();
        assertNotNull(delta);
        assertEquals(0, delta.getYears());
        assertEquals(0, delta.getMonths());
        assertEquals(1, delta.getDays());
        assertEquals(2, delta.getHours());
        assertEquals(0, delta.getMinutes());
        assertEquals(0, delta.getSeconds());
    }

    @Test
    void calendarItemResizedEvent_parsesDelta() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createBidirectionalMapper());

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        // 30 minutes (1800000 ms)
        ObjectNode deltaJson = buildDeltaJson(0, 0, 0, 1_800_000L);

        CalendarItemResizedEvent<TestMeeting> event = new CalendarItemResizedEvent<>(calendar, true, itemJson, deltaJson);

        Delta delta = event.getDelta();
        assertNotNull(delta);
        assertEquals(0, delta.getYears());
        assertEquals(0, delta.getMonths());
        assertEquals(0, delta.getDays());
        assertEquals(0, delta.getHours());
        assertEquals(30, delta.getMinutes());
        assertEquals(0, delta.getSeconds());
    }

    @Test
    void calendarItemEvent_throwsWhenItemNotInCache() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();
        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper();
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of());
        calendar.setCalendarItemProvider(provider, mapper);

        // No fetch performed, so no items in cache
        ObjectNode itemJson = buildItemJson("unknown-id", null, null, false);

        assertThrows(IllegalArgumentException.class, () ->
                new CalendarItemClickedEvent<>(calendar, true, itemJson));
    }

    @Test
    void addCalendarItemClickedListener_registersSuccessfully() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createReadOnlyMapper());

        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        AtomicReference<TestMeeting> receivedItem = new AtomicReference<>();

        var registration = calendar.addCalendarItemClickedListener(event -> {
            listenerCalled.set(true);
            receivedItem.set(event.getItem());
        });

        assertNotNull(registration);

        // Fire the event programmatically
        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        CalendarItemClickedEvent<TestMeeting> event = new CalendarItemClickedEvent<>(calendar, true, itemJson);
        ComponentUtil.fireEvent(calendar, event);

        assertTrue(listenerCalled.get());
        assertSame(meeting, receivedItem.get());
    }

    @Test
    void addCalendarItemMouseEnterListener_registersSuccessfully() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createReadOnlyMapper());

        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        var registration = calendar.addCalendarItemMouseEnterListener(event -> listenerCalled.set(true));
        assertNotNull(registration);

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        ComponentUtil.fireEvent(calendar, new CalendarItemMouseEnterEvent<>(calendar, true, itemJson));

        assertTrue(listenerCalled.get());
    }

    @Test
    void addCalendarItemMouseLeaveListener_registersSuccessfully() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createReadOnlyMapper());

        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        var registration = calendar.addCalendarItemMouseLeaveListener(event -> listenerCalled.set(true));
        assertNotNull(registration);

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        ComponentUtil.fireEvent(calendar, new CalendarItemMouseLeaveEvent<>(calendar, true, itemJson));

        assertTrue(listenerCalled.get());
    }

    @Test
    void addCalendarItemDroppedListener_registersSuccessfully() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createBidirectionalMapper());

        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        var registration = calendar.addCalendarItemDroppedListener(event -> listenerCalled.set(true));
        assertNotNull(registration);

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        ObjectNode deltaJson = buildDeltaJson(0, 0, 1, 0L);
        ComponentUtil.fireEvent(calendar, new CalendarItemDroppedEvent<>(calendar, true, itemJson, deltaJson));

        assertTrue(listenerCalled.get());
    }

    @Test
    void addCalendarItemResizedListener_registersSuccessfully() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createBidirectionalMapper());

        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        var registration = calendar.addCalendarItemResizedListener(event -> listenerCalled.set(true));
        assertNotNull(registration);

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        ObjectNode deltaJson = buildDeltaJson(0, 0, 0, 1_800_000L);
        ComponentUtil.fireEvent(calendar, new CalendarItemResizedEvent<>(calendar, true, itemJson, deltaJson));

        assertTrue(listenerCalled.get());
    }

    @Test
    void calendarItemEvent_getSource_returnsCalendar() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createReadOnlyMapper());

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        CalendarItemClickedEvent<TestMeeting> event = new CalendarItemClickedEvent<>(calendar, true, itemJson);

        assertSame(calendar, event.getSource());
    }

    @Test
    void calendarItemDroppedEvent_getItem_returnsCorrectItem() {
        LocalDateTime begin = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2025, 6, 1, 9, 30);
        TestMeeting meeting = new TestMeeting("m1", "Standup", begin, finish, false);

        FullCalendar<TestMeeting> calendar = createCIPCalendarWithCachedItem(meeting, createBidirectionalMapper());

        ObjectNode itemJson = buildItemJson("m1", begin, finish, false);
        ObjectNode deltaJson = buildDeltaJson(0, 0, 0, 3_600_000L);

        CalendarItemDroppedEvent<TestMeeting> event = new CalendarItemDroppedEvent<>(calendar, true, itemJson, deltaJson);

        assertSame(meeting, event.getItem());
        assertNotNull(event.getDelta());
        assertEquals(1, event.getDelta().getHours());
    }
}
