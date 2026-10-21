package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.BusinessHoursValue;
import org.vaadin.stefan.fullcalendar.option.EntryLimitValue;
import org.vaadin.stefan.fullcalendar.option.LocaleValue;
import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.CalendarLocale;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import com.vaadin.flow.component.ComponentEventBusUtil;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
        calendar.options().setTimeZone(Timezone.getSystem());
        return calendar;
    }

//    private FullCalendar createTestCalendar(int entries) {
//        return setupTestCalendar(new FullCalendar(entries));
//    }

    private FullCalendar createTestCalendar(ObjectNode options) {
        return setupTestCalendar(new FullCalendar(options));
    }

    @Test
    void noArgConstructor_leavesEditableToTheLibrary() {
        FullCalendar calendar = new FullCalendar();

        // no add-on default, the library's default applies
        assertNull(calendar.options().getEditable());
    }

    @Test
    void noArgConstructor_setsDefaultLocale() {
        FullCalendar calendar = new FullCalendar();

        assertEquals(LocaleValue.of(CalendarLocale.getDefaultLocale()), calendar.options().getLocale());
    }

    @Test
    void initialOptionsConstructor_setsDefaultLocaleWhenNoneGiven() {
        FullCalendar calendar = new FullCalendar(JsonFactory.createObject());

        assertEquals(LocaleValue.of(CalendarLocale.getDefaultLocale()), calendar.options().getLocale());
    }

    @Test
    void dayMaxEntries_setAndGet() {
        int entryLimit = 5;

        FullCalendar calendar = new FullCalendar();
        calendar.options().setDayMaxEntries(entryLimit);

        assertEquals(EntryLimitValue.of(entryLimit), calendar.options().getDayMaxEntries());
    }

    @Test
    void testArgsConstructor_initialOptions() throws ExecutionException, InterruptedException, TimeoutException {
        ObjectNode options = JsonFactory.createObject();

        FullCalendar calendar = new FullCalendar(options);
        Element element = calendar.getElement();

        Serializable returnedOptions = element.getPropertyRaw("initialOptions");

        assertTrue(returnedOptions instanceof ObjectNode, "Returned initial options not instanceof JsonObject");

        // TODO integrate Testbench test

    }

    @Test
    void currentViewNameIsTheClientNameBeforeTheFirstRender() {
        assertEquals("dayGridMonth", createTestCalendar().getCurrentViewName());
    }

    @Test
    void changeViewToAnUnregisteredViewNamesTheRegistrationMethod() {
        CustomCalendarView unregistered = new CustomCalendarView() {
            @Override
            public String getClientSideValue() {
                return "unregistered";
            }

            @Override
            public ObjectNode getViewSettings() {
                return JsonFactory.createObject();
            }
        };

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> createTestCalendar().changeView(unregistered));
        assertTrue(e.getMessage().contains("setCustomCalendarViews"), e.getMessage());
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
    void firstDayAndLocale_setAndGet() {
        FullCalendar calendar = createTestCalendar();

        calendar.options().setFirstDay(DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.MONDAY, calendar.options().getFirstDay());

        calendar.options().setFirstDay(DayOfWeek.SUNDAY);
        assertEquals(DayOfWeek.SUNDAY, calendar.options().getFirstDay());

        Locale locale = CalendarLocale.GREEK.getLocale();

        // we want to be sure to not use the default to test.
        assertNotEquals(CalendarLocale.getDefaultLocale(), locale);

        calendar.options().setLocale(locale);
        assertEquals(LocaleValue.of(locale), calendar.options().getLocale());

        BusinessHours hours = BusinessHours.allDays().start(5).end(10);
        calendar.options().setBusinessHours(hours);
        assertEquals(BusinessHoursValue.of(hours), calendar.options().getBusinessHours());

        calendar.options().setBusinessHours((BusinessHoursValue) null);
        assertNull(calendar.options().getBusinessHours());
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
        entry.setExtendedProp("description", description);

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
    void typedOptions_setAndRemove() {
        FullCalendar calendar = createTestCalendar();

        calendar.options().setAllDayText("someValue");
        assertEquals("someValue", calendar.options().getAllDayText());

        calendar.options().setAllDayText(null);
        assertNull(calendar.options().getAllDayText(), "Option must be absent after setting null");

        calendar.options().setWeekends(true);
        assertEquals(Boolean.TRUE, calendar.options().getWeekends());
        calendar.options().setWeekends(null);
        assertNull(calendar.options().getWeekends(), "weekends must be absent after setting null");
    }

    @Test
    void testGetAndSetRawOption() {
        FullCalendar calendar = createTestCalendar();

        assertNPE(calendar, c -> c.options().getRawOption(null));
        assertNPE(calendar, c -> c.options().setRawOption(null, null));
        assertNPE(calendar, c -> c.options().setRawOption(null, "someValue"));

        calendar.options().setRawOption("allDayText", "someValue");
        assertTrue(calendar.options().getRawOption("allDayText").isPresent());

        calendar.options().setRawOption("allDayText", null);
        assertFalse(calendar.options().getRawOption("allDayText").isPresent());
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

    // ---- Auto-Revert Tests ----

    @Test
    void autoRevertDefaultIsTrue() {
        FullCalendar calendar = createTestCalendar();
        assertTrue(calendar.isAutoRevertUnappliedEntryChanges());
    }

    @Test
    void autoRevertCanBeDisabled() {
        FullCalendar calendar = createTestCalendar();
        calendar.setAutoRevertUnappliedEntryChanges(false);
        assertFalse(calendar.isAutoRevertUnappliedEntryChanges());
    }

    @Test
    void autoRevertCanBeReEnabled() {
        FullCalendar calendar = createTestCalendar();
        calendar.setAutoRevertUnappliedEntryChanges(false);
        calendar.setAutoRevertUnappliedEntryChanges(true);
        assertTrue(calendar.isAutoRevertUnappliedEntryChanges());
    }

    // The browser reverts a gesture itself when auto-revert is on and no server-side listener exists for it (#440).

    private static boolean dropRevertsOnClient(FullCalendar calendar) {
        return calendar.getElement().getProperty("eventDropRevertsOnClient", false);
    }

    private static boolean resizeRevertsOnClient(FullCalendar calendar) {
        return calendar.getElement().getProperty("eventResizeRevertsOnClient", false);
    }

    @Test
    void withoutListenersBothGesturesRevertOnClient() {
        FullCalendar calendar = createTestCalendar();
        assertTrue(dropRevertsOnClient(calendar));
        assertTrue(resizeRevertsOnClient(calendar));
    }

    @Test
    void dropListenerStopsClientRevertOfDropsOnly() {
        FullCalendar calendar = createTestCalendar();
        calendar.addEntryDroppedListener(event -> {});
        assertFalse(dropRevertsOnClient(calendar));
        assertTrue(resizeRevertsOnClient(calendar));
    }

    @Test
    void resizeListenerStopsClientRevertOfResizesOnly() {
        FullCalendar calendar = createTestCalendar();
        calendar.addEntryResizedListener(event -> {});
        assertTrue(dropRevertsOnClient(calendar));
        assertFalse(resizeRevertsOnClient(calendar));
    }

    @Test
    void removingTheLastListenerOfAGestureRestoresClientRevert() {
        FullCalendar calendar = createTestCalendar();
        var first = calendar.addEntryDroppedListener(event -> {});
        var second = calendar.addEntryDroppedListener(event -> {});

        first.remove();
        assertFalse(dropRevertsOnClient(calendar));

        first.remove(); // a second remove of the same registration does not count again
        assertFalse(dropRevertsOnClient(calendar));

        second.remove();
        assertTrue(dropRevertsOnClient(calendar));
    }

    @Test
    void autoRevertOffStopsClientRevert() {
        FullCalendar calendar = createTestCalendar();
        calendar.setAutoRevertUnappliedEntryChanges(false);
        assertFalse(dropRevertsOnClient(calendar));
        assertFalse(resizeRevertsOnClient(calendar));

        calendar.setAutoRevertUnappliedEntryChanges(true);
        assertTrue(dropRevertsOnClient(calendar));
        assertTrue(resizeRevertsOnClient(calendar));
    }

}