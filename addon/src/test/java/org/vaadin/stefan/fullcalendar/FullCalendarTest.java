package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventBusUtil;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

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
        // locale + dayMaxEvents set in postConstruct
        assertExistingOptionCount(calendar, 2);
        assertSame(CalendarLocale.getDefaultLocale(), calendar.getLocale());
    }

    @Test
    void testArgsConstructor_dayMaxEvents() {
        int entryLimit = 5;

        FullCalendar calendar = new FullCalendar();
        calendar.setMaxEntriesPerDay(entryLimit);

        // this shall assure that all init options are handled
        // locale + dayMaxEvents set in postConstruct
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
        // locale + dayMaxEvents set in postConstruct
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
        // Client-side value is the locale tag string, possibly wrapped in a JsonNode
        Optional<Object> clientSideLocale = calendar.getOption(Option.LOCALE, true);
        assertTrue(clientSideLocale.isPresent());
        String clientValue = clientSideLocale.get().toString().replace("\"", "");
        assertEquals(locale.toLanguageTag().toLowerCase(), clientValue);

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

    // Removed: testEntryClickedEvent — body was entirely commented out.
    // EntryClickedEvent is covered by E2E tests (listener-data.spec.js, roundtrip.spec.js).

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

    // Removed: testTimeChangedEventSubClass — called subTestEntryTimeChangedEventSubClass
    // which was entirely commented out. EntryDroppedEvent and EntryResizedEvent are covered
    // by E2E tests (interaction-callbacks.spec.js, roundtrip.spec.js).

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

    // Removed: subTestEntryTimeChangedEventSubClass + test_fetchFromServer — entirely commented out.
    // Covered by E2E tests.

}