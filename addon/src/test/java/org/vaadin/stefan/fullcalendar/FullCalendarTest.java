package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventBusUtil;
import com.vaadin.flow.dom.Element;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.TestUtils.*;

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

    private FullCalendar createTestCalendar(JsonObject options) {
        return setupTestCalendar(new FullCalendar(options));
    }

    @Test
    void testNonArgsConstructor() {
        FullCalendar calendar = new FullCalendar();

        // this shall assure that all init options are handled
        assertExistingOptionCount(calendar, 1);
        assertSame(CalendarLocale.getDefaultLocale(), calendar.getLocale());
    }

    @Test
    void testArgsConstructor_dayMaxEvents() {
        int entryLimit = 5;

        FullCalendar calendar = new FullCalendar();
        calendar.setMaxEntriesPerDay(entryLimit);

        // this shall assure that all init options are handled
        assertExistingOptionCount(calendar, 1);
        assertSame(CalendarLocale.getDefaultLocale(), calendar.getLocale());

        assertEquals(entryLimit, calendar.getOption(Option.MAX_ENTRIES_PER_DAY).orElse(-1));
    }

    @Test
    void testArgsConstructor_initialOptions() throws ExecutionException, InterruptedException, TimeoutException {
        JsonObject options = Json.createObject();

        FullCalendar calendar = new FullCalendar(options);
        Element element = calendar.getElement();

        // this shall assure that all init options are handled
        // only the default language should be set
        assertExistingOptionCount(calendar, 1);
        Serializable returnedOptions = element.getPropertyRaw("initialOptions");

        assertTrue(returnedOptions instanceof JsonObject, "Returned initial options not instanceof JsonObject");

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
        BusinessHours hours = new BusinessHours(LocalTime.of(5, 0), LocalTime.of(10, 0), BusinessHours.ALL_DAYS);
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
        return calendar.getEntryProvider();
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

        var entryProvider = calendar.getEntryProvider().asInMemory();
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


        var entryProvider = calendar.getEntryProvider().asInMemory();
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

        var entryProvider = calendar.getEntryProvider().asInMemory();
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

        var entryProvider = calendar.getEntryProvider().asInMemory();
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
//        var entryProvider = calendar.getEntryProvider().asInMemory();
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

}