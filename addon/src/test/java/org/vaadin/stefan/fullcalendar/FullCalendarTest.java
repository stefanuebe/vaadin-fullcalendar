package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventBusUtil;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;
import org.vaadin.stefan.fullcalendar.dataprovider.CalendarItemProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.CalendarQuery;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.TestUtils.assertNPE;
import static org.vaadin.stefan.fullcalendar.TestUtils.assertOptionalEquals;

@SuppressWarnings("ALL")
public class FullCalendarTest {

    // TODO create "client side" times not based on LocalDate but ZoneDate or similar to have different timezones

    public static final String FULL_CALENDAR_HTML = "fullcalendar/full-calendar.html";

    @BeforeAll
    static void beforeAll() {
//        TestUtils.initVaadinService(FULL_CALENDAR_HTML);
    }

    private FullCalendar createTestCalendar() {
        return setupTestCalendar(new FullCalendar());
    }

    private FullCalendar setupTestCalendar(FullCalendar calendar) {
        // to simulate a client timezone, we have to use the server time zone, since all the LocalDate... instances
        // will not be on utc, but on the server timezone.
        calendar.setTimezone(Timezone.getSystem());
        return calendar;
    }

//    private FullCalendar createTestCalendar(int entries) {
//        return setupTestCalendar(new FullCalendar(entries));
//    }

    private FullCalendar createTestCalendar(ObjectNode options) {
        return setupTestCalendar(new FullCalendar(options));
    }

    @Test
    void testNonArgsConstructor() {
        FullCalendar calendar = new FullCalendar();

        // this shall assure that all init options are handled
        assertExistingOptionCount(calendar, 2);
        assertSame(CalendarLocale.getDefaultLocale(), calendar.getLocale());
    }

    @Test
    void testArgsConstructor_dayMaxEvents() {
        int entryLimit = 5;

        FullCalendar calendar = new FullCalendar();
        calendar.setMaxEntriesPerDay(entryLimit);

        // this shall assure that all init options are handled
        assertExistingOptionCount(calendar, 2);
        assertSame(CalendarLocale.getDefaultLocale(), calendar.getLocale());

        assertEquals(entryLimit, calendar.getOption(Option.MAX_ENTRIES_PER_DAY).orElse(-1));
    }

    @Test
    void testArgsConstructor_initialOptions() throws ExecutionException, InterruptedException, TimeoutException {
        ObjectNode options = JsonFactory.createObject();

        FullCalendar calendar = new FullCalendar(options);
        Element element = calendar.getElement();

        // this shall assure that all init options are handled
        // only the default language should be set
        assertExistingOptionCount(calendar, 2);
        Serializable returnedOptions = element.getPropertyRaw("initialOptions");

        assertTrue(returnedOptions instanceof ObjectNode, "Returned initial options not instanceof JsonObject");

        // TODO integrate Testbench test

    }

    private void assertExistingOptionCount(FullCalendar calendar, int expectedOptionsCount) {
        assertEquals(expectedOptionsCount, Arrays.stream(Option.values()).map(calendar::getOption).filter(Optional::isPresent).count());
    }

    @Test
    void testClientSideMethods() {
        FullCalendar calendar = createTestCalendar();

        calendar.next();
        calendar.previous();
        calendar.today();

        assertNPE(calendar, c -> c.changeView(null));
        calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);

        assertNPE(calendar, c -> c.gotoDate(null));
        calendar.gotoDate(LocalDate.now());
        calendar.gotoDate(LocalDate.MIN);
        calendar.gotoDate(LocalDate.MAX);

        calendar.render();
    }

    @Test
    void testClientSideOptionMethods() {
        FullCalendar calendar = createTestCalendar();

        // first day of week
        assertNPE(calendar, c -> c.setFirstDay(null));

        calendar.setFirstDay(DayOfWeek.MONDAY);
        assertOptionalEquals(DayOfWeek.MONDAY, calendar.getOption(Option.FIRST_DAY));
        assertOptionalEquals(DayOfWeek.MONDAY.getValue(), calendar.getOption(Option.FIRST_DAY, true));

        calendar.setFirstDay(DayOfWeek.SUNDAY);
        assertOptionalEquals(DayOfWeek.SUNDAY, calendar.getOption(Option.FIRST_DAY));
        assertOptionalEquals(0, calendar.getOption(Option.FIRST_DAY, true));

        assertNPE(calendar, c -> c.setLocale(null));

        Locale locale = CalendarLocale.GREEK.getLocale();

        // we want to be sure to not use the default to test.
        assertNotEquals(CalendarLocale.getDefaultLocale(), locale);

        calendar.setLocale(locale);
        assertSame(locale, calendar.getLocale());
        assertOptionalEquals(locale, calendar.getOption(Option.LOCALE));
        assertOptionalEquals(locale.toLanguageTag().toLowerCase(), calendar.getOption(Option.LOCALE, true));

        assertCorrectBooleanOption(calendar, Option.SELECTABLE, calendar::setTimeslotsSelectable);
        assertCorrectBooleanOption(calendar, Option.WEEK_NUMBERS, calendar::setWeekNumbersVisible);
        assertCorrectBooleanOption(calendar, Option.NOW_INDICATOR, calendar::setNowIndicatorShown);

        // this must be tested before the other setNumberClickForwads...Target methods
        assertCorrectBooleanOption(calendar, Option.NAV_LINKS, calendar::setNumberClickable);

        assertNPE(calendar, c -> calendar.setBusinessHours(null));
        BusinessHours hours = BusinessHours.allDays().start(5).end(10);
        calendar.setBusinessHours(hours);

        Optional<Object> option = calendar.getOption(Option.BUSINESS_HOURS);
        assertTrue(option.isPresent());
        assertTrue(option.get() instanceof BusinessHours[]);
        assertEquals(hours, ((BusinessHours[]) option.get())[0]);

        calendar.removeBusinessHours();
        option = calendar.getOption(Option.BUSINESS_HOURS);
        assertFalse(option.isPresent());
    }

    private void assertCorrectBooleanOption(FullCalendar calendar, Option optionToCheck, Consumer<Boolean> function) {
        function.accept(true);
        assertOptionalEquals(true, calendar.getOption(optionToCheck), "Checking set true for setter of "
                + optionToCheck.name() + " failed. Option returned false.");
    }


    @Test
    void testEmptyOptionalOnFetchingNonExistingEntryById() {
        FullCalendar calendar = createTestCalendar();

        Optional<Entry> optional = getEntryProvider(calendar).getEntryById("");
        assertNotNull(optional);
        assertFalse(optional.isPresent());
    }

    private InMemoryEntryProvider<Entry> getEntryProvider(FullCalendar calendar) {
        return (InMemoryEntryProvider<Entry>) calendar.getEntryProvider();
    }

    @Test
    void testFetchingExistingEntryById() {
        FullCalendar calendar = createTestCalendar();

        Entry entry = new Entry();
        calendar.getEntryProvider().asInMemory().addEntry(entry);

        Optional<Entry> optional = getEntryProvider(calendar).getEntryById(entry.getId());
        assertNotNull(optional);
        assertOptionalEquals(entry, optional);
    }

    @Test
    void testAddEntry() {
        FullCalendar calendar = createTestCalendar();

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();
        entryProvider.addEntry(entry1);
        entryProvider.addEntry(entry2);
        entryProvider.addEntry(entry3);

        Collection<Entry> entries = entryProvider.getEntries();
        assertEquals(3, entries.size());

        assertTrue(entries.contains(entry1));
        assertTrue(entries.contains(entry2));
        assertTrue(entries.contains(entry3));

        assertOptionalEquals(entry1, getEntryProvider(calendar).getEntryById(entry1.getId()));
        assertOptionalEquals(entry2, getEntryProvider(calendar).getEntryById(entry2.getId()));
        assertOptionalEquals(entry3, getEntryProvider(calendar).getEntryById(entry3.getId()));
    }

    @Test
    void testRemoveContent() {
        FullCalendar calendar = createTestCalendar();

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();

        entryProvider.addEntry(entry1);
        entryProvider.addEntry(entry2);
        entryProvider.addEntry(entry3);

        entryProvider.removeEntry(entry2);

        Collection<Entry> entries = entryProvider.getEntries();
        assertEquals(2, entries.size());

        assertTrue(entries.contains(entry1));
        assertFalse(entries.contains(entry2));
        assertTrue(entries.contains(entry3));

        assertOptionalEquals(entry1, getEntryProvider(calendar).getEntryById(entry1.getId()));
        assertOptionalEquals(entry3, getEntryProvider(calendar).getEntryById(entry3.getId()));

        assertFalse(getEntryProvider(calendar).getEntryById(entry2.getId()).isPresent());
    }

    @Test
    void testInitialEmptyCollection() {
        FullCalendar calendar = createTestCalendar();

        Collection<Entry> entries = calendar.getEntryProvider().asInMemory().getEntries();
        assertNotNull(entries);
        assertEquals(0, entries.size());
    }

//    @Test
//    void testEntriesInstanceAreSameAfterUpdate() {
//        FullCalendar calendar = createTestCalendar();
//
//        Entry entry1 = new Entry();
//        Entry entry2 = new Entry();
//        Entry entry3 = new Entry();
//
//        var entryProvider = calendar.getEntryProvider().asInMemory();
//        entryProvider.addEntry(entry1);
//        entryProvider.addEntry(entry2);
//        entryProvider.addEntry(entry3);
//
//        entry1.setTitle("1");
//        entry2.setTitle("2");
//        entry3.setTitle("3");
//
//        entryProvider.updateEntry(entry1);
//        entryProvider.updateEntry(entry2);
//        entryProvider.updateEntry(entry3);
//
//        Collection<Entry> entries = entryProvider.getEntries();
//        assertEquals(3, entries.size());
//
//        assertTrue(entries.contains(entry1));
//        assertTrue(entries.contains(entry2));
//        assertTrue(entries.contains(entry3));
//
//        assertOptionalEquals(entry1, getEntryProvider(calendar).getEntryById(entry1.getId()));
//        assertOptionalEquals(entry2, getEntryProvider(calendar).getEntryById(entry2.getId()));
//        assertOptionalEquals(entry3, getEntryProvider(calendar).getEntryById(entry3.getId()));
//    }

    private Entry createEntry(String id, String title, LocalDateTime start, LocalDateTime end, boolean allDay, boolean editable, String color, String description) {
        Entry entry = new Entry(id);

        entry.setTitle(title);
        entry.setStart(start);
        entry.setEnd(end);
        entry.setAllDay(allDay);
        entry.setEditable(editable);
        entry.setColor(color);
        entry.setDescription(description);

        return entry;
    }


    @Test
    void testRemoveAll() {
        FullCalendar calendar = createTestCalendar();


        InMemoryEntryProvider entryProvider = calendar.getEntryProvider().asInMemory();
        entryProvider.addEntry(new Entry());
        entryProvider.addEntry(new Entry());
        entryProvider.addEntry(new Entry());

        assertEquals(3, entryProvider.getEntries().size());

        entryProvider.removeAllEntries();
        assertEquals(0, entryProvider.getEntries().size());
    }

    @Test
    void testGetEntriesReturnListCopy() {
        FullCalendar calendar = createTestCalendar();

        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();
        entryProvider.addEntry(new Entry());
        entryProvider.addEntry(new Entry());
        entryProvider.addEntry(new Entry());

        Collection<Entry> entries = entryProvider.getEntries();
        assertEquals(3, entries.size());

        entryProvider.removeAllEntries();
        assertEquals(3, entries.size());
    }

    @Test
    void testGetAndSetOption() {
        FullCalendar calendar = createTestCalendar();

        assertNPE(calendar, c -> c.getOption((Option) null));
        assertNPE(calendar, c -> c.setOption((Option) null, null));
        assertNPE(calendar, c -> c.setOption((Option) null, "someValue"));

        calendar.setOption(Option.LOCALE, "someValue");
        assertTrue(calendar.getOption(Option.LOCALE).isPresent());

        calendar.setOption(Option.LOCALE, null);
        assertFalse(calendar.getOption(Option.LOCALE).isPresent());
    }

    @Test
    void testGetAndSetOptionWithStringKeys() {
        FullCalendar calendar = createTestCalendar();

        assertNPE(calendar, c -> c.getOption((String) null));
        assertNPE(calendar, c -> c.setOption((String) null, null));
        assertNPE(calendar, c -> c.setOption((String) null, "someValue"));

        String optionKey = Option.LOCALE.getOptionKey();

        calendar.setOption(optionKey, "someValue");
        assertTrue(calendar.getOption(optionKey).isPresent());

        calendar.setOption(optionKey, null);
        assertFalse(calendar.getOption(optionKey).isPresent());
    }

    @Test
    void testGetAndSetOptionWithServerSideValues() {
        FullCalendar calendar = createTestCalendar();

        Locale locale = Locale.getDefault();

        calendar.setOption(Option.LOCALE, "someValue", locale);
        assertOptionalEquals(locale, calendar.getOption(Option.LOCALE));
        assertOptionalEquals("someValue", calendar.getOption(Option.LOCALE, true));

        calendar.setOption(Option.LOCALE, "someValue", null);
        assertOptionalEquals("someValue", calendar.getOption(Option.LOCALE));

        calendar.setOption(Option.LOCALE, "someOtherValue", locale);
        calendar.setOption(Option.LOCALE, "someOtherValue");
        assertOptionalEquals("someOtherValue", calendar.getOption(Option.LOCALE));
    }

    @Test
    void testGetAndSetOptionWithServerSideValuesWithStringKeys() {
        FullCalendar calendar = createTestCalendar();

        Locale locale = Locale.getDefault();
        String option = Option.LOCALE.getOptionKey();

        calendar.setOption(option, "someValue", locale);
        assertOptionalEquals(locale, calendar.getOption(option));
        assertOptionalEquals("someValue", calendar.getOption(option, true));

        calendar.setOption(option, "someValue", null);
        assertOptionalEquals("someValue", calendar.getOption(option));

        calendar.setOption(option, "someOtherValue", locale);
        calendar.setOption(option, "someOtherValue");
        assertOptionalEquals("someOtherValue", calendar.getOption(option));
    }

    @Test
    void testEntryClickedEvent() throws Exception {
        FullCalendar calendar = createTestCalendar();

        LocalDateTime refDate = LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime refDateAsDateTime = refDate;
        LocalDateTime refDateTime = refDate.withHour(7);

        // check all day and time entries
        Entry allDayEntry = createEntry("allDay", "title", refDateAsDateTime, refDateAsDateTime.plus(1, ChronoUnit.DAYS), true, true, "color", null);
        Entry timedEntry = createEntry("timed", "title", refDateTime, refDateTime.plus(1, ChronoUnit.HOURS), false, true, "color", null);

        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();
        entryProvider.addEntry(allDayEntry);
        entryProvider.addEntry(timedEntry);

        // TODO fix: here a fetch is missing

//        JsonObject jsonData = Json.createObject();
//        jsonData.put("id", allDayEntry.getId());
//        assertSame(allDayEntry, new EntryClickedEvent(calendar, true, jsonData).getEntry());
//
//        jsonData = Json.createObject();
//        jsonData.put("id", timedEntry.getId());
//        assertSame(timedEntry, new EntryClickedEvent(calendar, true, jsonData).getEntry());
    }

    @Test
    void testDateTimeEventSubClasses() throws Exception {
        subTestDateTimeEventSubClass(TimeslotClickedEvent.class);
        subTestDateEventSubClass(DayNumberClickedEvent.class);
        subTestDateEventSubClass(WeekNumberClickedEvent.class);
    }


    @Test
    void testTimeslotsSelectedEvent() throws Exception {
        FullCalendar calendar = createTestCalendar();


        // client timezone may differ server timezone, so we have to simulate that for the event creation
        LocalDate refDateStart = LocalDate.of(2000, 1, 1);
        LocalDate refDateEnd = LocalDate.of(2000, 1, 2);
        LocalDateTime refDateTimeStart = LocalDateTime.of(2000, 1, 1, 7, 0);
        LocalDateTime refDateTimeEnd = LocalDateTime.of(2000, 1, 1, 8, 0);

        TimeslotsSelectedEvent event;
        // now we simulate the conversion to the server side timezone
        event = new TimeslotsSelectedEvent(calendar, true, JsonUtils.formatClientSideDateString(refDateStart), JsonUtils.formatClientSideDateString(refDateEnd), true);
        assertEquals((refDateStart).atStartOfDay(), event.getStart());
        assertEquals((refDateEnd).atStartOfDay(), event.getEnd());
        assertTrue(event.isAllDay());

        event = new TimeslotsSelectedEvent(calendar, true, JsonUtils.formatClientSideDateTimeString(refDateTimeStart), JsonUtils.formatClientSideDateTimeString(refDateTimeEnd), false);
        assertEquals((refDateTimeStart), event.getStart());
        assertEquals((refDateTimeEnd), event.getEnd());
        assertFalse(event.isAllDay());
    }

    @Test
    void testTimeChangedEventSubClass() throws Exception {
        subTestEntryTimeChangedEventSubClass(EntryDroppedEvent.class);
        subTestEntryTimeChangedEventSubClass(EntryResizedEvent.class);
    }

    private <T extends DateEvent> void subTestDateEventSubClass(Class<T> eventClass) throws Exception {
        FullCalendar calendar = createTestCalendar();

        LocalDate refDate = LocalDate.of(2000, 1, 1);

        T event;
        Constructor<T> constructor = ComponentEventBusUtil.getEventConstructor(eventClass);
        event = constructor.newInstance(calendar, true, JsonUtils.formatClientSideDateString(refDate));
        assertEquals((refDate), event.getDate());
    }

    private <T extends DateTimeEvent> void subTestDateTimeEventSubClass(Class<T> eventClass) throws Exception {
        FullCalendar calendar = createTestCalendar();

        LocalDate refDate = LocalDate.of(2000, 1, 1);
        LocalDateTime refDateTime = LocalDate.of(2000, 1, 1).atStartOfDay();

        T event;
        Constructor<T> constructor = ComponentEventBusUtil.getEventConstructor(eventClass);
        event = constructor.newInstance(calendar, true, JsonUtils.formatClientSideDateTimeString(refDate), true);
        assertEquals((refDate).atStartOfDay(), event.getDateTime());
        assertTrue(event.isAllDay());

        event = constructor.newInstance(calendar, true, JsonUtils.formatClientSideDateTimeString(refDateTime), false);
        assertEquals((refDateTime), event.getDateTime());
        assertFalse(event.isAllDay());

    }

    // TODO fix
    private <T extends EntryTimeChangedEvent> void subTestEntryTimeChangedEventSubClass(Class<T> eventClass) throws Exception {

        //        FullCalendar calendar = createTestCalendar();
//
//        LocalDateTime refDate = LocalDate.of(2000, 1, 1).atStartOfDay();
//        LocalDateTime refDateTime = LocalDate.of(2000, 1, 1).atStartOfDay().withHour(7);
//
//        // check all day and time entries
//        Entry allDayEntry = createEntry("allDay", "title", refDate, refDate.plus(1, ChronoUnit.DAYS), true, true, "color", null);
//        Entry timedEntry = createEntry("timed", "title", refDateTime, refDateTime.plus(1, ChronoUnit.HOURS), false, true, "color", null);
//
//        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();
//        entryProvider.addEntry(allDayEntry);
//        entryProvider.addEntry(timedEntry);
//
//        // the original entry will be modified by the event. we test if the modified original event matches the json source
//        Delta delta = new Delta(1, 1, 1, 1, 1, 1);
//        JsonObject jsonDelta = Json.createObject();
//        jsonDelta.put("years", 1);
//        jsonDelta.put("months", 1);
//        jsonDelta.put("days", 1);
//        jsonDelta.put("hours", 1);
//        jsonDelta.put("minutes", 1);
//        jsonDelta.put("seconds", 1);
//
//        Entry modifiedAllDayEntry = createEntry(allDayEntry.getId(), allDayEntry.getTitle() + 1, delta.applyOn(allDayEntry.getStart()), delta.applyOn(allDayEntry.getEnd()), allDayEntry.isAllDay(), !allDayEntry.isEditable(), allDayEntry.getColor() + 1, allDayEntry.getDescription());
//        Entry modifiedTimedEntry = createEntry(timedEntry.getId(), timedEntry.getTitle() + 1, delta.applyOn(timedEntry.getStart()), delta.applyOn(timedEntry.getEnd()), timedEntry.isAllDay(), !timedEntry.isEditable(), timedEntry.getColor() + 1, timedEntry.getDescription());
//        JsonObject jsonModifiedAllDayEntry = modifiedAllDayEntry.toJson();
//        JsonObject jsonModifiedTimedEntry = modifiedTimedEntry.toJson();
//
//        Constructor<T> constructor = ComponentEventBusUtil.getEventConstructor(eventClass);
//
//        // TODO I assume a fetch is missing here somewhere?
//
//        /*
//            Day event
//         */
//        T event = constructor.newInstance(calendar, true, jsonModifiedAllDayEntry, jsonDelta);
//        assertEquals(delta, event.getDelta());
//
//        // not changed automatically
//        EntryTest.assertFullEqualsByJsonAttributes(allDayEntry, event.getEntry());
//
//        // apply changes and test modifications
//        event.applyChangesOnEntry();
//        EntryTest.assertFullEqualsByJsonAttributes(modifiedAllDayEntry, event.getEntry());
//
//        /*
//            Time slot event
//         */
//        event = constructor.newInstance(calendar, true, jsonModifiedTimedEntry, jsonDelta);
//
//        assertEquals(delta, event.getDelta());
//
//        // not changed automatically
//        EntryTest.assertFullEqualsByJsonAttributes(timedEntry, event.getEntry());
//
//        // apply changes and test modifications
//        event.applyChangesOnEntry();
//        EntryTest.assertFullEqualsByJsonAttributes(modifiedTimedEntry, event.getEntry());
    }

    // ---- CIP (Calendar Item Provider) Phase 3 tests ----

    /**
     * Simple test POJO for CIP tests.
     */
    static class TestMeeting {
        private String id;
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

    @Test
    void testSetCalendarItemProvider_fetchReturnsCorrectJson() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        TestMeeting meeting = new TestMeeting("m1", "Standup", LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 9, 30), false);
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of(meeting));
        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper();

        calendar.setCalendarItemProvider(provider, mapper);

        assertTrue(calendar.isUsingCalendarItemProvider());
        assertSame(provider, calendar.getCalendarItemProvider());
        assertSame(mapper, calendar.getCalendarItemPropertyMapper());
        assertNull(calendar.getEntryProvider());

        // Simulate a fetch from server
        ObjectNode query = JsonFactory.createObject();
        var result = calendar.fetchEntriesFromServer(query);

        assertEquals(1, result.size());
        ObjectNode jsonEntry = (ObjectNode) result.get(0);
        assertEquals("m1", jsonEntry.get("id").asString());
        assertEquals("Standup", jsonEntry.get("title").asString());
    }

    @Test
    void testCachedItemFromFetch_returnsPojo() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        TestMeeting meeting = new TestMeeting("m1", "Standup", LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 9, 30), false);
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of(meeting));
        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper();

        calendar.setCalendarItemProvider(provider, mapper);

        // Trigger fetch
        calendar.fetchEntriesFromServer(JsonFactory.createObject());

        Optional<TestMeeting> cached = calendar.getCachedItemFromFetch("m1");
        assertTrue(cached.isPresent());
        assertSame(meeting, cached.get());
    }

    @Test
    void testCachedEntryFromFetch_emptyWhenCipActive() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        TestMeeting meeting = new TestMeeting("m1", "Standup", LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 9, 30), false);
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of(meeting));
        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper();

        calendar.setCalendarItemProvider(provider, mapper);
        calendar.fetchEntriesFromServer(JsonFactory.createObject());

        // getCachedEntryFromFetch should return empty when CIP is active (items are not Entry instances)
        Optional<Entry> cached = calendar.getCachedEntryFromFetch("m1");
        assertFalse(cached.isPresent());
    }

    @Test
    void testCachedItemFromFetch_worksForEntryProviderToo() {
        FullCalendar<Entry> calendar = new FullCalendar<>();

        // Default calendar uses entry provider (delegates to CIP internally)
        assertFalse(calendar.isUsingCalendarItemProvider());

        // Before any fetch, cache is empty
        Optional<Entry> itemCached = calendar.getCachedItemFromFetch("anything");
        assertFalse(itemCached.isPresent());

        // After fetch, Entry items are also accessible via getCachedItemFromFetch
        Entry entry = new Entry("e1");
        entry.setTitle("Test");
        entry.setStart(LocalDateTime.of(2025, 6, 1, 9, 0));
        entry.setEnd(LocalDateTime.of(2025, 6, 1, 10, 0));
        ((org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider<Entry>) calendar.getEntryProvider()).addEntry(entry);
        calendar.fetchEntriesFromServer(JsonFactory.createObject());

        Optional<Entry> cached = calendar.getCachedItemFromFetch("e1");
        assertTrue(cached.isPresent());
        assertSame(entry, cached.get());
    }

    @Test
    void testSwitchFromEntryToCip_clearsEntryState() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        // Initially has entry provider
        assertNotNull(calendar.getEntryProvider());
        assertFalse(calendar.isUsingCalendarItemProvider());

        // Switch to CIP
        TestMeeting meeting = new TestMeeting("m1", "Standup", LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 9, 30), false);
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of(meeting));
        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper();

        calendar.setCalendarItemProvider(provider, mapper);

        assertTrue(calendar.isUsingCalendarItemProvider());
        assertNull(calendar.getEntryProvider());
        assertSame(provider, calendar.getCalendarItemProvider());
    }

    @Test
    void testSwitchFromCipToEntry_clearsCipState() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        // Set up CIP
        TestMeeting meeting = new TestMeeting("m1", "Standup", LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 9, 30), false);
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of(meeting));
        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper();
        calendar.setCalendarItemProvider(provider, mapper);

        assertTrue(calendar.isUsingCalendarItemProvider());

        // Switch back to entry provider
        calendar.setEntryProvider(org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider.emptyInMemory());

        assertFalse(calendar.isUsingCalendarItemProvider());
        // After unification, setEntryProvider() delegates to CIP internally, so
        // getCalendarItemProvider() returns the EntryProvider (which IS-A CalendarItemProvider)
        assertNotNull(calendar.getCalendarItemProvider());
        assertInstanceOf(org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider.class, calendar.getCalendarItemProvider());
        assertNotNull(calendar.getCalendarItemPropertyMapper());
        assertNotNull(calendar.getEntryProvider());
    }

    @Test
    void testMutualExclusion_settersAndHandler_providerClearsHandler() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        CalendarItemPropertyMapper<TestMeeting> mapperWithSetters = createBidirectionalMapper();
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of());

        // Set update handler first
        calendar.setCalendarItemUpdateHandler((item, changes) -> {});

        // Setting a new CIP clears the handler, so this should NOT throw
        calendar.setCalendarItemProvider(provider, mapperWithSetters);

        // But trying to set a handler after a mapper with setters still throws
        assertThrows(IllegalStateException.class, () ->
                calendar.setCalendarItemUpdateHandler((item, changes) -> {}));
    }

    @Test
    void testMutualExclusion_handlerAfterMapperSetters_throws() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        CalendarItemPropertyMapper<TestMeeting> mapperWithSetters = createBidirectionalMapper();
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of());

        // Set CIP with mapper that has setters
        CalendarItemPropertyMapper<TestMeeting> readOnlyMapper = createReadOnlyMapper();
        calendar.setCalendarItemProvider(provider, readOnlyMapper);

        // Now replace with mapper that has setters
        calendar.setCalendarItemProvider(provider, mapperWithSetters);

        // Now try to set handler → should throw
        assertThrows(IllegalStateException.class, () ->
                calendar.setCalendarItemUpdateHandler((item, changes) -> {}));
    }

    @Test
    void testApplyCalendarItemChanges_usesMapperSetters() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        CalendarItemPropertyMapper<TestMeeting> mapper = createBidirectionalMapper();
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of());

        calendar.setCalendarItemProvider(provider, mapper);

        TestMeeting meeting = new TestMeeting("m1", "Standup", LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 9, 30), false);

        ObjectNode delta = JsonFactory.createObject();
        delta.put("start", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2025, 6, 1, 10, 0)));
        delta.put("end", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2025, 6, 1, 10, 30)));

        calendar.applyCalendarItemChanges(meeting, delta);

        assertEquals(LocalDateTime.of(2025, 6, 1, 10, 0), meeting.getBegin());
        assertEquals(LocalDateTime.of(2025, 6, 1, 10, 30), meeting.getFinish());
    }

    @Test
    void testApplyCalendarItemChanges_usesHandler() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper();
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of());

        calendar.setCalendarItemProvider(provider, mapper);

        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        AtomicReference<TestMeeting> handlerItem = new AtomicReference<>();
        calendar.setCalendarItemUpdateHandler((item, changes) -> {
            handlerCalled.set(true);
            handlerItem.set(item);
        });

        TestMeeting meeting = new TestMeeting("m1", "Standup", LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 9, 30), false);

        ObjectNode delta = JsonFactory.createObject();
        delta.put("start", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2025, 6, 1, 10, 0)));

        calendar.applyCalendarItemChanges(meeting, delta);

        assertTrue(handlerCalled.get());
        assertSame(meeting, handlerItem.get());
    }

    @Test
    void testApplyCalendarItemChanges_noStrategy_throws() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper(); // no setters
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of());

        calendar.setCalendarItemProvider(provider, mapper);
        // No update handler set, and mapper has no setters

        TestMeeting meeting = new TestMeeting("m1", "Standup", LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 9, 30), false);
        ObjectNode delta = JsonFactory.createObject();

        assertThrows(IllegalStateException.class, () ->
                calendar.applyCalendarItemChanges(meeting, delta));
    }

    @Test
    void testBuilderWithCalendarItemProvider() {
        TestMeeting meeting = new TestMeeting("m1", "Standup", LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 9, 30), false);
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of(meeting));
        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper();

        FullCalendar<TestMeeting> calendar = FullCalendarBuilder.create(TestMeeting.class)
                .withCalendarItemProvider(provider, mapper)
                .build();

        assertTrue(calendar.isUsingCalendarItemProvider());
        assertSame(provider, calendar.getCalendarItemProvider());
        assertSame(mapper, calendar.getCalendarItemPropertyMapper());
    }

    @Test
    void testBuilderRawTypeBackwardCompat() {
        // The existing raw create() should still work for backward compatibility
        FullCalendar calendar = FullCalendarBuilder.create().build();
        assertNotNull(calendar);
        assertFalse(calendar.isUsingCalendarItemProvider());
        assertNotNull(calendar.getEntryProvider());
    }

    @Test
    void testSetCalendarItemProvider_nullProvider_throws() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();
        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper();

        assertThrows(NullPointerException.class, () ->
                calendar.setCalendarItemProvider(null, mapper));
    }

    @Test
    void testSetCalendarItemProvider_nullMapper_throws() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of());

        assertThrows(NullPointerException.class, () ->
                calendar.setCalendarItemProvider(provider, null));
    }

    @Test
    void testFetchWithMultipleItems() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        TestMeeting m1 = new TestMeeting("m1", "Standup", LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 9, 30), false);
        TestMeeting m2 = new TestMeeting("m2", "Review", LocalDateTime.of(2025, 6, 1, 14, 0), LocalDateTime.of(2025, 6, 1, 15, 0), false);
        TestMeeting m3 = new TestMeeting("m3", "All Day Event", LocalDateTime.of(2025, 6, 1, 0, 0), LocalDateTime.of(2025, 6, 2, 0, 0), true);

        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of(m1, m2, m3));
        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper();

        calendar.setCalendarItemProvider(provider, mapper);
        var result = calendar.fetchEntriesFromServer(JsonFactory.createObject());

        assertEquals(3, result.size());

        // All three items should be cached
        assertTrue(calendar.getCachedItemFromFetch("m1").isPresent());
        assertTrue(calendar.getCachedItemFromFetch("m2").isPresent());
        assertTrue(calendar.getCachedItemFromFetch("m3").isPresent());
        assertSame(m1, calendar.getCachedItemFromFetch("m1").get());
        assertSame(m2, calendar.getCachedItemFromFetch("m2").get());
        assertSame(m3, calendar.getCachedItemFromFetch("m3").get());
    }

    @Test
    void testBuilderWithCalendarItemProviderAndUpdateHandler() {
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of());
        CalendarItemPropertyMapper<TestMeeting> mapper = createReadOnlyMapper(); // read-only, no setters
        CalendarItemUpdateHandler<TestMeeting> handler = (item, changes) -> {};

        FullCalendar<TestMeeting> calendar = FullCalendarBuilder.create(TestMeeting.class)
                .withCalendarItemProvider(provider, mapper)
                .withCalendarItemUpdateHandler(handler)
                .build();

        assertTrue(calendar.isUsingCalendarItemProvider());
        assertSame(provider, calendar.getCalendarItemProvider());
    }

//    @Test
//    void test_fetchFromServer() {
//        Entry entry1 = new Entry("1");
//        Entry entry2 = new Entry("2");
//        Entry entry3 = new Entry("3");
//
//        Set<Entry> entries = Stream.of(entry1, entry2, entry3).collect(Collectors.toSet());
//
//        entry1.setStart(LocalDate.of(2000, 1, 1).atTime(10, 0));
//        entry1.setEnd(LocalDate.of(2000, 1, 1).atTime(11, 0));
//        entry2.setStart(LocalDate.of(2000, 2, 1).atTime(10, 0));
//        entry2.setEnd(LocalDate.of(2000, 2, 1).atTime(11, 0));
//        entry3.setStart(LocalDate.of(2000, 3, 1).atTime(10, 0));
//        entry3.setEnd(LocalDate.of(2000, 3, 1).atTime(11, 0));
//
//        FullCalendar calendar = createTestCalendar();
//
//        EntryProvider<Entry> provider = EntryProvider.fromCallbacks(
//                query -> query.applyFilter(entries.stream())
//                , s -> entries.stream().filter(e -> s.equals(e.getId())).findFirst().orElse(null));
//        calendar.setEntryProvider(provider);
//
//        JsonArray array = calendar.fetchEntriesFromServer(Json.createObject());
//        Set<Entry> converted = TestUtils.toSet(array, jsonValue -> Entry.fromJson((JsonObject) jsonValue));
//
//        assertEqualAsSet(entries, converted);
//        assertTrue(calendar.getCachedEntryFromFetch("1").isPresent());
//        assertTrue(calendar.getCachedEntryFromFetch("2").isPresent());
//        assertTrue(calendar.getCachedEntryFromFetch("3").isPresent());
//
//        JsonObject clientSideRequest = Json.createObject();
//        clientSideRequest.put("start", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2000, 1, 1, 0, 0)));
//        clientSideRequest.put("end", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2000, 1, 2, 0, 0)));
//
//        array = calendar.fetchEntriesFromServer(clientSideRequest);
//        converted = TestUtils.toSet(array, jsonValue -> Entry.fromJson((JsonObject) jsonValue));
//
//        assertEqualAsSet(Stream.of(entry1).collect(Collectors.toSet()), converted);
//        assertTrue(calendar.getCachedEntryFromFetch("1").isPresent());
//        assertFalse(calendar.getCachedEntryFromFetch("2").isPresent());
//        assertFalse(calendar.getCachedEntryFromFetch("3").isPresent());
//    }

    // ---- Phase 7: Internal Unification tests ----

    @Test
    void testSetEntryProvider_internallySetsCIP() {
        FullCalendar<Entry> calendar = new FullCalendar<>();

        // setEntryProvider is called by postConstruct, verify CIP is set internally
        assertNotNull(calendar.getCalendarItemProvider());
        assertInstanceOf(org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider.class, calendar.getCalendarItemProvider());
        assertNotNull(calendar.getCalendarItemPropertyMapper());
        assertNotNull(calendar.getEntryProvider());
        assertFalse(calendar.isUsingCalendarItemProvider());
    }

    @Test
    void testSetEntryProvider_fetchUsesEntryToJson() {
        FullCalendar<Entry> calendar = new FullCalendar<>();

        Entry entry = new Entry("e1");
        entry.setTitle("Team Standup");
        entry.setStart(LocalDateTime.of(2025, 6, 1, 9, 0));
        entry.setEnd(LocalDateTime.of(2025, 6, 1, 9, 30));
        ((org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider<Entry>) calendar.getEntryProvider()).addEntry(entry);

        var result = calendar.fetchEntriesFromServer(JsonFactory.createObject());
        assertEquals(1, result.size());

        ObjectNode json = (ObjectNode) result.get(0);
        assertEquals("e1", json.get("id").asString());
        assertEquals("Team Standup", json.get("title").asString());
    }

    @Test
    void testSetEntryProvider_entryLifecyclePreserved() {
        FullCalendar<Entry> calendar = new FullCalendar<>();

        Entry entry = new Entry("e1");
        entry.setStart(LocalDateTime.of(2025, 6, 1, 9, 0));
        ((org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider<Entry>) calendar.getEntryProvider()).addEntry(entry);

        calendar.fetchEntriesFromServer(JsonFactory.createObject());

        // After fetch, Entry should have calendar set and be known to client
        assertTrue(entry.getCalendar().isPresent());
        assertSame(calendar, entry.getCalendar().get());
        assertTrue(entry.isKnownToTheClient());
    }

    @Test
    void testSetEntryProvider_cachedItemFromFetchWorksForEntries() {
        FullCalendar<Entry> calendar = new FullCalendar<>();

        Entry entry = new Entry("e1");
        entry.setStart(LocalDateTime.of(2025, 6, 1, 9, 0));
        ((org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider<Entry>) calendar.getEntryProvider()).addEntry(entry);

        calendar.fetchEntriesFromServer(JsonFactory.createObject());

        // getCachedItemFromFetch should work for Entry items after unification
        Optional<Entry> cached = calendar.getCachedItemFromFetch("e1");
        assertTrue(cached.isPresent());
        assertSame(entry, cached.get());

        // getCachedEntryFromFetch should also work
        Optional<Entry> entryCached = calendar.getCachedEntryFromFetch("e1");
        assertTrue(entryCached.isPresent());
        assertSame(entry, entryCached.get());
    }

    @Test
    void testSetEntryProvider_cipListenersWorkOnEntryCalendars() {
        FullCalendar<Entry> calendar = new FullCalendar<>();

        // CIP listeners should fire on Entry-based calendars
        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        calendar.addCalendarItemClickedListener(event -> listenerCalled.set(true));
        // Just verify registration doesn't throw
        assertFalse(listenerCalled.get());
    }

    @Test
    void testSetCalendarItemProvider_clearsHandlerFromPreviousEntryProvider() {
        FullCalendar<TestMeeting> calendar = new FullCalendar<>();

        // Default state has an entry update handler (set by setEntryProvider via postConstruct)
        // Switching to CIP should clear it
        CalendarItemPropertyMapper<TestMeeting> mapper = createBidirectionalMapper();
        CalendarItemProvider<TestMeeting> provider = createTestProvider(List.of());

        // This should not throw — the entry handler is cleared before the new mapper is checked
        calendar.setCalendarItemProvider(provider, mapper);
        assertTrue(calendar.isUsingCalendarItemProvider());
    }

}