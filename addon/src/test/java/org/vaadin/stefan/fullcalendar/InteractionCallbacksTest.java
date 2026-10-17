package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.option.CssSelector;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for interaction callbacks and server-side events.
 * Covers listener registration, and JS callback/option setters.
 */
public class InteractionCallbacksTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    // -------------------------------------------------------------------------
    // setDroppable
    // -------------------------------------------------------------------------

    @Test
    void setDroppable_true_storesOption() {
        calendar.options().setDroppable(true);
        assertEquals(true, calendar.options().getDroppable());
    }

    @Test
    void setDroppable_false_storesOption() {
        calendar.options().setDroppable(false);
        assertEquals(false, calendar.options().getDroppable());
    }

    // -------------------------------------------------------------------------
    // setDropAccept
    // -------------------------------------------------------------------------

    @Test
    void setDropAccept_cssSelector_storesOption() {
        calendar.options().setDropAccept(".external-event");
        assertEquals(CssSelector.of(".external-event"), calendar.options().getDropAccept());
    }

    @Test
    void setDropAccept_jsFunction_storesOption() {
        String fn = "function(dragEl) { return dragEl.classList.contains('droppable'); }";
        calendar.options().setDropAccept(JsCallback.of(fn));
        assertEquals(JsCallback.of(fn), calendar.options().getDropAccept());
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
    // EntryReceiveEvent — creates new Entry from JSON (not from cache)
    // -------------------------------------------------------------------------

    @Test
    void entryReceiveEvent_createsEntryFromJson() {
        ObjectNode json = JsonFactory.createObject();
        json.put("id", "ext-1");
        json.put("start", "2025-03-10T09:00:00Z");
        json.put("end", "2025-03-10T10:00:00Z");
        json.put("allDay", false);

        EntryReceiveEvent event = new EntryReceiveEvent(calendar, true, json, null);
        assertNotNull(event.getEntry());
        // Entry.id is final (set by Entry constructor), so it won't match the JSON id.
        // Verify the mutable fields are populated.
        assertFalse(event.getEntry().isAllDay());
        assertNotNull(event.getEntry().getStart());
    }

    @Test
    void entryReceiveEvent_readsExtendedProps() {
        ObjectNode json = JsonFactory.createObject();
        json.put("start", "2025-03-10T09:00:00Z");
        json.putObject("extendedProps").put("priority", "high").put("weight", 3);

        EntryReceiveEvent event = new EntryReceiveEvent(calendar, true, json, null);
        assertEquals("high", event.getEntry().getExtendedProp("priority"));
        assertEquals(3, event.getEntry().<Number>getExtendedProp("weight", null).intValue());
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
