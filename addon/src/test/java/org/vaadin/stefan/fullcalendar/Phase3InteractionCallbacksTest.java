package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.TestUtils.assertOptionalEquals;

/**
 * Tests for Phase 3: Missing interaction callbacks and server-side events.
 * Covers Option enum keys, listener registration, and JS callback/option setters.
 */
public class Phase3InteractionCallbacksTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    // -------------------------------------------------------------------------
    // Option enum keys — Phase 3 additions
    // -------------------------------------------------------------------------

    @Test
    void option_selectAllow_key() {
        assertEquals("selectAllow", Option.SELECT_ALLOW.getOptionKey());
    }

    @Test
    void option_eventAllow_key() {
        assertEquals("eventAllow", Option.EVENT_ALLOW.getOptionKey());
    }

    @Test
    void option_eventOverlap_key() {
        assertEquals("eventOverlap", Option.EVENT_OVERLAP.getOptionKey());
    }

    @Test
    void option_droppable_key() {
        assertEquals("droppable", Option.DROPPABLE.getOptionKey());
    }

    @Test
    void option_dropAccept_key() {
        assertEquals("dropAccept", Option.DROP_ACCEPT.getOptionKey());
    }

    // -------------------------------------------------------------------------
    // setDroppable — uses setOption
    // -------------------------------------------------------------------------

    @Test
    void setDroppable_true_storesOption() {
        calendar.setDroppable(true);
        assertOptionalEquals(true, calendar.getOption(Option.DROPPABLE));
    }

    @Test
    void setDroppable_false_storesOption() {
        calendar.setDroppable(false);
        assertOptionalEquals(false, calendar.getOption(Option.DROPPABLE));
    }

    // -------------------------------------------------------------------------
    // setDropAccept — uses setOption
    // -------------------------------------------------------------------------

    @Test
    void setDropAccept_cssSelector_storesOption() {
        calendar.setDropAccept(".external-event");
        assertOptionalEquals(".external-event", calendar.getOption(Option.DROP_ACCEPT));
    }

    @Test
    void setDropAccept_jsFunction_storesOption() {
        String fn = "function(dragEl) { return dragEl.classList.contains('droppable'); }";
        calendar.setDropAccept(fn);
        assertOptionalEquals(fn, calendar.getOption(Option.DROP_ACCEPT));
    }

    // -------------------------------------------------------------------------
    // JS callback setters (callJsFunction-based, not stored in options map)
    // -------------------------------------------------------------------------

    @Test
    void setSelectAllowCallback_doesNotThrow() {
        // These methods delegate to callJsFunction(), which queues silently before attachment.
        // Verify the method is callable without error.
        assertDoesNotThrow(() -> calendar.setSelectAllowCallback(
                "function(selectInfo) { return true; }"));
    }

    @Test
    void setEventAllowCallback_doesNotThrow() {
        assertDoesNotThrow(() -> calendar.setEventAllowCallback(
                "function(dropInfo, draggedEvent) { return true; }"));
    }

    @Test
    void setEventOverlapCallback_doesNotThrow() {
        assertDoesNotThrow(() -> calendar.setEventOverlapCallback(
                "function(stillEvent, movingEvent) { return stillEvent.display === 'background'; }"));
    }

    // -------------------------------------------------------------------------
    // Listener registration — returns non-null Registration, rejects null
    // -------------------------------------------------------------------------

    @Test
    void addEntryDragStartListener_returnsRegistration() {
        Registration reg = calendar.addEntryDragStartListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addEntryDragStartListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
                calendar.addEntryDragStartListener(null));
    }

    @Test
    void addEntryDragStopListener_returnsRegistration() {
        Registration reg = calendar.addEntryDragStopListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addEntryDragStopListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
                calendar.addEntryDragStopListener(null));
    }

    @Test
    void addEntryResizeStartListener_returnsRegistration() {
        Registration reg = calendar.addEntryResizeStartListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addEntryResizeStartListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
                calendar.addEntryResizeStartListener(null));
    }

    @Test
    void addEntryResizeStopListener_returnsRegistration() {
        Registration reg = calendar.addEntryResizeStopListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addEntryResizeStopListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
                calendar.addEntryResizeStopListener(null));
    }

    @Test
    void addTimeslotsUnselectListener_returnsRegistration() {
        Registration reg = calendar.addTimeslotsUnselectListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addTimeslotsUnselectListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
                calendar.addTimeslotsUnselectListener(null));
    }

    @Test
    void addWindowResizeListener_returnsRegistration() {
        Registration reg = calendar.addWindowResizeListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addWindowResizeListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
                calendar.addWindowResizeListener(null));
    }

    @Test
    void addDropListener_returnsRegistration() {
        Registration reg = calendar.addDropListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addDropListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
                calendar.addDropListener(null));
    }

    @Test
    void addEntryReceiveListener_returnsRegistration() {
        Registration reg = calendar.addEntryReceiveListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addEntryReceiveListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
                calendar.addEntryReceiveListener(null));
    }

    @Test
    void addEntryLeaveListener_returnsRegistration() {
        Registration reg = calendar.addEntryLeaveListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addEntryLeaveListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
                calendar.addEntryLeaveListener(null));
    }

    // -------------------------------------------------------------------------
    // WindowResizeEvent — construction
    // -------------------------------------------------------------------------

    @Test
    void windowResizeEvent_knownView_populatesCalendarView() {
        WindowResizeEvent event = new WindowResizeEvent(calendar, true, CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());
        assertEquals(CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue(), event.getViewName());
        assertTrue(event.getCalendarViewOptional().isPresent());
        assertEquals(CalendarViewImpl.DAY_GRID_MONTH, event.getCalendarViewOptional().get());
    }

    @Test
    void windowResizeEvent_unknownView_calendarViewIsEmpty() {
        WindowResizeEvent event = new WindowResizeEvent(calendar, true, "customView");
        assertEquals("customView", event.getViewName());
        assertFalse(event.getCalendarViewOptional().isPresent());
    }

    // -------------------------------------------------------------------------
    // EntryReceiveEvent — creates new Entry from JSON (not from cache)
    // -------------------------------------------------------------------------

    @Test
    void entryReceiveEvent_createsEntryFromJson() {
        ObjectNode json = JsonFactory.createObject();
        json.put("id", "ext-1");
        json.put("start", "2025-03-10T09:00:00Z");
        json.put("end", "2025-03-10T10:00:00Z");
        json.put("allDay", false);

        EntryReceiveEvent event = new EntryReceiveEvent(calendar, true, json);
        assertNotNull(event.getEntry());
        // Entry.id is final (set by Entry constructor), so it won't match the JSON id.
        // Verify the mutable fields are populated.
        assertFalse(event.getEntry().isAllDay());
        assertNotNull(event.getEntry().getStart());
    }

    // -------------------------------------------------------------------------
    // TimeslotsUnselectEvent — no-payload constructor
    // -------------------------------------------------------------------------

    @Test
    void timeslotsUnselectEvent_constructsWithoutError() {
        TimeslotsUnselectEvent event = new TimeslotsUnselectEvent(calendar, true);
        assertNotNull(event);
        assertSame(calendar, event.getSource());
    }
}
