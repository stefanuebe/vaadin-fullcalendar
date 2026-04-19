package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link JsCallback} and the callback-related paths in {@link FullCalendar}.
 */
@SuppressWarnings("deprecation")
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

    // --- setOption with JsCallback round-trip via getOption ---

    @Test
    void setOption_withJsCallback_getOptionReturnsJsCallback() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        JsCallback cb = JsCallback.of("function(arg) { return []; }");
        calendar.setOption(FullCalendar.Option.DAY_CELL_CLASS_NAMES, cb);

        // getOption should return the original JsCallback, not the marker JSON
        var result = calendar.getOption(FullCalendar.Option.DAY_CELL_CLASS_NAMES);
        assertTrue(result.isPresent());
        assertInstanceOf(JsCallback.class, result.get());
        assertEquals(cb, result.get());
    }

    @Test
    void setOption_withJsCallback_stringKey_getOptionReturnsJsCallback() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        JsCallback cb = JsCallback.of("function() { return true; }");
        calendar.setOption("selectAllow", cb);

        var result = calendar.getOption("selectAllow");
        assertTrue(result.isPresent());
        assertInstanceOf(JsCallback.class, result.get());
        assertEquals(cb, result.get());
    }

    // --- Deprecated wrapper delegation tests ---

    @Test
    void setEntryClassNamesCallback_delegatesToSetOption() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setEntryClassNamesCallback("function(arg) { return ['cls']; }");

        var result = calendar.getOption(FullCalendar.Option.ENTRY_CLASS_NAMES);
        assertTrue(result.isPresent());
        assertInstanceOf(JsCallback.class, result.get());
        assertEquals("function(arg) { return ['cls']; }", ((JsCallback) result.get()).getJsFunction());
    }

    @Test
    void setEntryContentCallback_delegatesToSetOption() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setEntryContentCallback("function(arg) { return arg.el; }");

        var result = calendar.getOption(FullCalendar.Option.ENTRY_CONTENT);
        assertTrue(result.isPresent());
        assertInstanceOf(JsCallback.class, result.get());
    }

    @Test
    void setEntryWillUnmountCallback_delegatesToSetOption() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setEntryWillUnmountCallback("function(arg) { }");

        var result = calendar.getOption(FullCalendar.Option.ENTRY_WILL_UNMOUNT);
        assertTrue(result.isPresent());
        assertInstanceOf(JsCallback.class, result.get());
    }

    // --- Native event listener field tests ---

    @Test
    void addEntryNativeEventListener_storesInMap() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addEntryNativeEventListener("click", "e => alert('hi')");

        // Verify via reflection that the map has the entry
        var map = getNativeEventsMap(calendar);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("click"));
        assertEquals("e => alert('hi')", map.get("click"));
    }

    @Test
    void addEntryNativeEventListener_multipleListeners() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addEntryNativeEventListener("click", "e => {}");
        calendar.addEntryNativeEventListener("mouseover", "e => {}");

        var map = getNativeEventsMap(calendar);
        assertEquals(2, map.size());
    }

    // --- buildEntryDidMountMerged — merge logic tests ---

    @Test
    void buildMerged_userCallbackOnly_returnsCallbackAsIs() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setAutoProvideEntryIdOnClient(false);
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT,
                JsCallback.of("function(info) { info.el.title = 'x'; }"));

        String merged = calendar.buildEntryDidMountMerged();
        assertEquals("function(info) { info.el.title = 'x'; }", merged);
    }

    @Test
    void buildMerged_nativeListenerOnly_generatesWrapper() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setAutoProvideEntryIdOnClient(false);
        calendar.addEntryNativeEventListener("click", "e => alert(1)");

        String merged = calendar.buildEntryDidMountMerged();
        assertNotNull(merged);
        assertTrue(merged.startsWith("function(info) {"));
        assertTrue(merged.contains("addEventListener('click', e => alert(1))"));
        assertTrue(merged.endsWith("}"));
    }

    @Test
    void buildMerged_userCallbackAndNativeListeners_mergesCorrectly() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT,
                JsCallback.of("function(info) { info.el.title = 'x'; }"));
        calendar.addEntryNativeEventListener("click", "e => {}");

        String merged = calendar.buildEntryDidMountMerged();
        assertNotNull(merged);
        // User callback content preserved
        assertTrue(merged.contains("info.el.title = 'x'"));
        // Native listener spliced in before closing brace
        assertTrue(merged.contains("addEventListener('click', e => {})"));
        // Starts with function and ends with closing brace
        assertTrue(merged.startsWith("function(info)"));
        assertTrue(merged.endsWith("}"));
    }

    @Test
    void buildMerged_nothingSet_returnsNull() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setAutoProvideEntryIdOnClient(false);
        assertNull(calendar.buildEntryDidMountMerged());
    }

    @Test
    void buildMerged_userCallbackCleared_nativeListenerRemains() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setAutoProvideEntryIdOnClient(false);
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT,
                JsCallback.of("function(info) { }"));
        calendar.addEntryNativeEventListener("mouseover", "e => {}");

        // Clear user callback
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT, null);

        String merged = calendar.buildEntryDidMountMerged();
        assertNotNull(merged, "native listener should still produce a merged function");
        assertTrue(merged.contains("addEventListener('mouseover'"));
        // User callback content should NOT be in the merged string
        assertFalse(merged.contains("info.el"));
    }

    // --- autoProvideEntryIdOnClient — default snippet injection ---

    @Test
    void autoProvideEntryIdOnClient_defaultsToTrue() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        assertTrue(calendar.isAutoProvideEntryIdOnClient());
    }

    @Test
    void setAutoProvideEntryIdOnClient_roundTrip() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setAutoProvideEntryIdOnClient(false);
        assertFalse(calendar.isAutoProvideEntryIdOnClient());
        calendar.setAutoProvideEntryIdOnClient(true);
        assertTrue(calendar.isAutoProvideEntryIdOnClient());
    }

    @Test
    void buildMerged_autoProvideOnly_wrapsSnippetInFunction() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        String merged = calendar.buildEntryDidMountMerged();
        assertNotNull(merged);
        assertTrue(merged.startsWith("function(info) {"));
        assertTrue(merged.contains(FullCalendar.DEFAULT_ENTRY_ID_ASSIGNMENT_SNIPPET));
        assertTrue(merged.endsWith("}"));
    }

    @Test
    void buildMerged_autoProvideAndUserCallback_defaultPrefixesUser() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT,
                JsCallback.of("function(info) { info.el.title = 'x'; }"));

        String merged = calendar.buildEntryDidMountMerged();
        assertNotNull(merged);
        int snippetIdx = merged.indexOf("arguments[0].el.id = ");
        int userIdx = merged.indexOf("info.el.title = 'x'");
        assertTrue(snippetIdx > 0, "default snippet must be present");
        assertTrue(userIdx > 0, "user callback body must be present");
        assertTrue(snippetIdx < userIdx, "default snippet must run before user callback so user can override the id");
    }

    @Test
    void buildMerged_autoProvideUserAndNative_allThreePresentInOrder() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT,
                JsCallback.of("function(info) { info.el.title = 'x'; }"));
        calendar.addEntryNativeEventListener("click", "e => {}");

        String merged = calendar.buildEntryDidMountMerged();
        assertNotNull(merged);
        int defaultIdx = merged.indexOf("arguments[0].el.id = ");
        int userIdx = merged.indexOf("info.el.title = 'x'");
        int nativeIdx = merged.indexOf("addEventListener('click'");
        assertTrue(defaultIdx > 0 && userIdx > defaultIdx && nativeIdx > userIdx,
                "order must be: default -> user -> native, got default=" + defaultIdx + " user=" + userIdx + " native=" + nativeIdx);
    }

    @Test
    void buildMerged_autoProvideAndNativeOnly_wrapsBoth() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addEntryNativeEventListener("mouseover", "e => {}");

        String merged = calendar.buildEntryDidMountMerged();
        assertNotNull(merged);
        assertTrue(merged.startsWith("function(info) {"));
        assertTrue(merged.contains(FullCalendar.DEFAULT_ENTRY_ID_ASSIGNMENT_SNIPPET));
        assertTrue(merged.contains("addEventListener('mouseover'"));
        assertTrue(merged.endsWith("}"));
    }

    @Test
    void defaultSnippet_contains_mirrorGuard_and_isStartGuard() {
        String s = FullCalendar.DEFAULT_ENTRY_ID_ASSIGNMENT_SNIPPET;
        assertTrue(s.contains("fc-event-mirror"), "must skip mirror elements via CSS class");
        assertTrue(s.contains("isStart"), "must skip non-start segments");
        assertTrue(s.contains("data-resource-id"), "must look up resource context for multi-resource disambiguation");
        assertTrue(s.contains("'entry-' + arguments[0].event.id"), "must use entry-<id> schema");
    }

    @Test
    void buildMerged_expressionBodyArrowCallback_returnedAsIsNoCorruption() {
        FullCalendar calendar = FullCalendarBuilder.create().build();
        // auto-provide default: true. User provides an expression-body arrow (no braces).
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT,
                JsCallback.of("info => info.el.title = 'x'"));

        String merged = calendar.buildEntryDidMountMerged();
        // Output must be syntactically valid JS — returned verbatim when there is no
        // brace-delimited body to splice into. The default snippet is intentionally dropped
        // rather than prepended raw outside the arrow expression.
        assertEquals("info => info.el.title = 'x'", merged);
    }

    @Test
    void buildMerged_expressionBodyArrowWithNativeListeners_droppedByContract() {
        // Documents the current contract: addEntryNativeEventListener's Javadoc requires
        // a braced body; if the user callback is an expression-body arrow, native listeners
        // are silently skipped because there is nowhere syntactically valid to splice them.
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setOption(FullCalendar.Option.ENTRY_DID_MOUNT,
                JsCallback.of("info => info.el.title = 'x'"));
        calendar.addEntryNativeEventListener("click", "e => {}");

        String merged = calendar.buildEntryDidMountMerged();
        assertEquals("info => info.el.title = 'x'", merged, "native listeners must not corrupt an expression-body arrow");
    }

    // --- Helpers ---

    private static JsCallback getUserEntryDidMountCallback(FullCalendar calendar) {
        try {
            Field field = FullCalendar.class.getDeclaredField("userEntryDidMountCallback");
            field.setAccessible(true);
            return (JsCallback) field.get(calendar);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static java.util.Map<String, String> getNativeEventsMap(FullCalendar calendar) {
        try {
            Field field = FullCalendar.class.getDeclaredField("customNativeEventsMap");
            field.setAccessible(true);
            return (java.util.Map<String, String>) field.get(calendar);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
