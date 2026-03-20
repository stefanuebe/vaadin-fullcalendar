package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link JsCallback} and the callback-related paths in {@link FullCalendar}.
 */
class JsCallbackTest {

    // --- setOption with JsCallback ---

    @Test
    void setOption_withJsCallback_doesNotThrow() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        assertDoesNotThrow(() ->
            calendar.setOption(FullCalendar.Option.DAY_CELL_CLASS_NAMES,
                JsCallback.of("function(arg) { return []; }")));
    }

    @Test
    void setOption_withJsCallback_null_doesNotThrow() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        assertDoesNotThrow(() ->
            calendar.setOption(FullCalendar.Option.DROP_ACCEPT, (Object) null));
    }

    @Test
    void setOption_stringKey_withJsCallback_doesNotThrow() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        assertDoesNotThrow(() ->
            calendar.setOption("dayCellClassNames",
                JsCallback.of("function(arg) { return []; }")));
    }

    // --- ENTRY_DID_MOUNT merge / userEntryDidMountCallback field ---

    @Test
    void setOption_entryDidMount_storesCallback() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT,
            JsCallback.of("function(info) { info.el.title = 'test'; }"));
        JsCallback cb = getUserEntryDidMountCallback(calendar);
        assertNotNull(cb);
        assertEquals("function(info) { info.el.title = 'test'; }", cb.getJsFunction());
    }

    @Test
    void setOption_entryDidMount_null_clearsCallback() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT,
            JsCallback.of("function(info) {}"));
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT, null);
        assertNull(getUserEntryDidMountCallback(calendar));
    }

    @Test
    void setEntryDidMountCallback_delegatesToMergePath() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setEntryDidMountCallback("function(info) {}");
        JsCallback cb = getUserEntryDidMountCallback(calendar);
        assertNotNull(cb);
        assertEquals("function(info) {}", cb.getJsFunction());
    }

    @Test
    void setEntryDidMountCallback_null_clearsCallback() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setEntryDidMountCallback("function(info) {}");
        calendar.setEntryDidMountCallback(null);
        assertNull(getUserEntryDidMountCallback(calendar));
    }

    // --- JsCallback unit tests ---

    @Test
    void jsCallback_of_null_returnsNull() {
        assertNull(JsCallback.of(null));
    }

    @Test
    void jsCallback_clearCallback_returnsNull() {
        assertNull(JsCallback.clearCallback());
    }

    @Test
    void jsCallback_of_blank_returnsNull() {
        assertNull(JsCallback.of(""));
        assertNull(JsCallback.of("   "));
    }

    @Test
    void jsCallback_of_returnsInstance() {
        JsCallback cb = JsCallback.of("function() {}");
        assertNotNull(cb);
        assertEquals("function() {}", cb.getJsFunction());
    }

    @Test
    void jsCallback_strips_whitespace() {
        JsCallback cb = JsCallback.of("  function() {}  ");
        assertEquals("function() {}", cb.getJsFunction());
    }

    @Test
    void jsCallback_toMarkerJson() {
        JsCallback cb = JsCallback.of("function() { return true; }");
        var marker = cb.toMarkerJson();
        assertEquals("function() { return true; }", marker.get("__jsCallback").asString());
    }

    @Test
    void jsCallback_equals_and_hashCode() {
        JsCallback a = JsCallback.of("function() {}");
        JsCallback b = JsCallback.of("function() {}");
        JsCallback c = JsCallback.of("function() { return 1; }");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    private static JsCallback getUserEntryDidMountCallback(FullCalendar calendar) {
        try {
            Field field = FullCalendar.class.getDeclaredField("userEntryDidMountCallback");
            field.setAccessible(true);
            return (JsCallback) field.get(calendar);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
