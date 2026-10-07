package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.shared.Registration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.TestUtils.assertOptionalEquals;

/**
 * Tests for remote entry sources.
 * Covers RemoteEntrySource subclasses, toJson() output, Option enum keys, FullCalendar API,
 * and the new server-side event classes.
 */
public class EventSourcesTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    // -------------------------------------------------------------------------
    // Option enum keys
    // -------------------------------------------------------------------------

    @Test
    void option_startParam_key() {
        assertEquals("startParam", Option.ENTRY_SOURCE_START_PARAM.getOptionKey());
    }

    @Test
    void option_endParam_key() {
        assertEquals("endParam", Option.ENTRY_SOURCE_END_PARAM.getOptionKey());
    }

    @Test
    void option_timeZoneParam_key() {
        assertEquals("timeZoneParam", Option.ENTRY_SOURCE_TIME_ZONE_PARAM.getOptionKey());
    }

    @Test
    void option_googleCalendarApiKey_key() {
        assertEquals("googleCalendarApiKey", Option.ENTRY_SOURCE_GOOGLE_CALENDAR_API_KEY.getOptionKey());
    }

    // -------------------------------------------------------------------------
    // Option setters
    // -------------------------------------------------------------------------

    @Test
    void setStartParam_storesOption() {
        calendar.setOption(Option.ENTRY_SOURCE_START_PARAM, "from");
        assertOptionalEquals("from", calendar.getOption(Option.ENTRY_SOURCE_START_PARAM));
    }

    @Test
    void setEndParam_storesOption() {
        calendar.setOption(Option.ENTRY_SOURCE_END_PARAM, "to");
        assertOptionalEquals("to", calendar.getOption(Option.ENTRY_SOURCE_END_PARAM));
    }

    @Test
    void setTimeZoneParam_storesOption() {
        calendar.setOption(Option.ENTRY_SOURCE_TIME_ZONE_PARAM, "tz");
        assertOptionalEquals("tz", calendar.getOption(Option.ENTRY_SOURCE_TIME_ZONE_PARAM));
    }

    @Test
    void setGoogleCalendarApiKey_storesOption() {
        calendar.setOption(Option.ENTRY_SOURCE_GOOGLE_CALENDAR_API_KEY, "AIzaSy-test");
        assertOptionalEquals("AIzaSy-test", calendar.getOption(Option.ENTRY_SOURCE_GOOGLE_CALENDAR_API_KEY));
    }

    // -------------------------------------------------------------------------
    // JsonFeedEntrySource — toJson()
    // -------------------------------------------------------------------------

    @Test
    void jsonFeedEntrySource_toJson_hasUrl() {
        JsonFeedEntrySource source = new JsonFeedEntrySource("/api/events");
        ObjectNode json = source.toJson();
        assertEquals("/api/events", json.get("url").asString());
    }

    @Test
    void jsonFeedEntrySource_toJson_hasDefaultMethod() {
        ObjectNode json = new JsonFeedEntrySource("/api/events").toJson();
        assertEquals("GET", json.get("method").asString());
    }

    @Test
    void jsonFeedEntrySource_toJson_hasId() {
        JsonFeedEntrySource source = new JsonFeedEntrySource("/api/events").withId("feed-1");
        assertEquals("feed-1", source.toJson().get("id").asString());
    }

    @Test
    void jsonFeedEntrySource_toJson_hasColor() {
        ObjectNode json = new JsonFeedEntrySource("/api/events").withColor("steelblue").toJson();
        assertEquals("steelblue", json.get("color").asString());
    }

    @Test
    void jsonFeedEntrySource_toJson_editableDefaultAbsent() {
        // editable is not set by default — null means not serialized
        ObjectNode json = new JsonFeedEntrySource("/api/events").toJson();
        assertFalse(json.has("editable"), "editable should not appear when not set");
    }

    @Test
    void jsonFeedEntrySource_toJson_editableWhenSet() {
        ObjectNode json = new JsonFeedEntrySource("/api/events").withEditable(true).toJson();
        assertTrue(json.get("editable").asBoolean());
    }

    @Test
    void jsonFeedEntrySource_toJson_extraParams() {
        ObjectNode json = new JsonFeedEntrySource("/api/events")
                .withExtraParams(Map.of("roomId", "101"))
                .toJson();
        assertTrue(json.has("extraParams"));
        assertEquals("101", json.get("extraParams").get("roomId").asString());
    }

    @Test
    void jsonFeedEntrySource_toJson_method_post() {
        ObjectNode json = new JsonFeedEntrySource("/api/events").withMethod("POST").toJson();
        assertEquals("POST", json.get("method").asString());
    }

    @Test
    void jsonFeedEntrySource_constructor_nullUrl_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new JsonFeedEntrySource(null));
    }

    @Test
    void jsonFeedEntrySource_toJson_classNames() {
        ObjectNode json = new JsonFeedEntrySource("/api/events")
                .withClassNames(List.of("foo", "bar"))
                .toJson();
        assertEquals("foo bar", json.get("className").asString());
        assertFalse(json.has("classNames"));
    }

    @Test
    void jsonFeedEntrySource_toJson_colors() {
        ObjectNode json = new JsonFeedEntrySource("/api/events")
                .withColor("red")
                .withContrastColor("white")
                .toJson();
        assertEquals("red", json.get("color").asString());
        assertEquals("white", json.get("contrastColor").asString());
        assertFalse(json.has("textColor"));
    }

    @Test
    @SuppressWarnings("removal")
    void jsonFeedEntrySource_withTextColor_setsContrastColor() {
        ObjectNode json = new JsonFeedEntrySource("/api/events").withTextColor("white").toJson();
        assertEquals("white", json.get("contrastColor").asString());
    }

    // -------------------------------------------------------------------------
    // GoogleCalendarEntrySource — toJson()
    // -------------------------------------------------------------------------

    @Test
    void googleCalendarEntrySource_toJson_hasGoogleCalendarId() {
        ObjectNode json = new GoogleCalendarEntrySource("abc@group.calendar.google.com").toJson();
        assertEquals("abc@group.calendar.google.com", json.get("googleCalendarId").asString());
    }

    @Test
    void googleCalendarEntrySource_toJson_hasId() {
        ObjectNode json = new GoogleCalendarEntrySource("abc@group.calendar.google.com")
                .withId("holidays")
                .toJson();
        assertEquals("holidays", json.get("id").asString());
    }

    @Test
    void googleCalendarEntrySource_toJson_apiKeyAbsentByDefault() {
        ObjectNode json = new GoogleCalendarEntrySource("abc@group.calendar.google.com").toJson();
        assertFalse(json.has("googleCalendarApiKey"));
    }

    @Test
    void googleCalendarEntrySource_toJson_apiKeyWhenSet() {
        ObjectNode json = new GoogleCalendarEntrySource("abc@group.calendar.google.com")
                .withApiKey("AIzaSy-override")
                .toJson();
        assertEquals("AIzaSy-override", json.get("googleCalendarApiKey").asString());
    }

    @Test
    void googleCalendarEntrySource_constructor_nullId_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new GoogleCalendarEntrySource(null));
    }

    // -------------------------------------------------------------------------
    // ICalendarEntrySource — toJson()
    // -------------------------------------------------------------------------

    @Test
    void iCalendarEntrySource_toJson_hasUrl() {
        ObjectNode json = new ICalendarEntrySource("https://example.com/cal.ics").toJson();
        assertEquals("https://example.com/cal.ics", json.get("url").asString());
    }

    @Test
    void iCalendarEntrySource_toJson_hasFormatIcs() {
        ObjectNode json = new ICalendarEntrySource("https://example.com/cal.ics").toJson();
        assertEquals("ics", json.get("format").asString());
    }

    @Test
    void iCalendarEntrySource_toJson_hasId() {
        ObjectNode json = new ICalendarEntrySource("https://example.com/cal.ics")
                .withId("holiday-ics")
                .toJson();
        assertEquals("holiday-ics", json.get("id").asString());
    }

    @Test
    void iCalendarEntrySource_constructor_nullUrl_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new ICalendarEntrySource(null));
    }

    // -------------------------------------------------------------------------
    // addEventSource / removeEventSource / getEventSources
    // -------------------------------------------------------------------------

    @Test
    void addRemoteEntrySource_registersSource() {
        JsonFeedEntrySource source = new JsonFeedEntrySource("/api").withId("src-1");
        calendar.addRemoteEntrySource(source);
        assertTrue(calendar.getRemoteEntrySources().contains(source));
    }

    @Test
    void addRemoteEntrySource_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.addRemoteEntrySource(null));
    }

    @Test
    void removeRemoteEntrySource_removesFromRegistry() {
        JsonFeedEntrySource source = new JsonFeedEntrySource("/api").withId("src-1");
        calendar.addRemoteEntrySource(source);
        calendar.removeRemoteEntrySource("src-1");
        assertFalse(calendar.getRemoteEntrySources().contains(source));
    }

    @Test
    void removeRemoteEntrySource_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.removeRemoteEntrySource(null));
    }

    @Test
    void setRemoteEntrySources_replacesAll() {
        calendar.addRemoteEntrySource(new JsonFeedEntrySource("/old").withId("old"));
        JsonFeedEntrySource newSource = new JsonFeedEntrySource("/new").withId("new");
        calendar.setRemoteEntrySources(List.of(newSource));
        Collection<RemoteEntrySource<?>> sources = calendar.getRemoteEntrySources();
        assertEquals(1, sources.size());
        assertTrue(sources.contains(newSource));
    }

    @Test
    void setRemoteEntrySources_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.setRemoteEntrySources(null));
    }

    @Test
    void getRemoteEntrySources_emptyInitially() {
        assertTrue(calendar.getRemoteEntrySources().isEmpty());
    }

    @Test
    void refetchEvents_doesNotThrow() {
        assertDoesNotThrow(() -> calendar.refetchEvents());
    }

    // -------------------------------------------------------------------------
    // Listener registration
    // -------------------------------------------------------------------------

    @Test
    void addRemoteEntrySourceFailureListener_returnsRegistration() {
        Registration reg = calendar.addRemoteEntrySourceFailureListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addRemoteEntrySourceFailureListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.addRemoteEntrySourceFailureListener(null));
    }

    @Test
    void addRemoteEntryDroppedListener_returnsRegistration() {
        Registration reg = calendar.addRemoteEntryDroppedListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addRemoteEntryDroppedListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.addRemoteEntryDroppedListener(null));
    }

    @Test
    void addRemoteEntryResizedListener_returnsRegistration() {
        Registration reg = calendar.addRemoteEntryResizedListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addRemoteEntryResizedListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.addRemoteEntryResizedListener(null));
    }

    @Test
    void entrySourceEntryDrop_reachesListener() {
        List<RemoteEntryDroppedEvent> received = new ArrayList<>();
        calendar.addRemoteEntryDroppedListener(received::add);

        fireEntrySourceDomEvent("externalEntryDrop");

        assertEquals(1, received.size());
        assertEquals("ext-1", received.get(0).getEntry().getId());
        assertEquals("my-feed", received.get(0).getSourceId());
        // the delta of one day is applied in reverse for the old start
        assertEquals(received.get(0).getEntry().getStart().minusDays(1), received.get(0).getOldStart());
    }

    @Test
    void entrySourceEntryResize_reachesListener() {
        List<RemoteEntryResizedEvent> received = new ArrayList<>();
        calendar.addRemoteEntryResizedListener(received::add);

        fireEntrySourceDomEvent("externalEntryResize");

        assertEquals(1, received.size());
        assertEquals("ext-1", received.get(0).getEntry().getId());
        assertEquals("my-feed", received.get(0).getSourceId());
        // the delta of one day is applied in reverse for the old end
        assertEquals(received.get(0).getEntry().getEnd().minusDays(1), received.get(0).getOldEnd());
    }

    /**
     * Fires the given client event on the calendar element, as the client does for an entry source entry. The event
     * name and the detail keys mirror {@code full-calendar.ts}. A browser test would have to drag an entry of an
     * editable entry source, which is not worth its cost here.
     */
    private void fireEntrySourceDomEvent(String eventName) {
        ObjectNode entryData = JsonFactory.createObject();
        entryData.put("id", "ext-1");
        entryData.put("start", "2025-03-10T10:00:00Z");
        entryData.put("end", "2025-03-10T11:00:00Z");
        entryData.put("allDay", false);

        ObjectNode delta = JsonFactory.createObject();
        delta.put("years", 0);
        delta.put("months", 0);
        delta.put("days", 1);
        delta.put("milliseconds", 0L);

        ObjectNode eventData = JsonFactory.createObject();
        eventData.set("event.detail.data", entryData);
        eventData.set("event.detail.delta", delta);
        eventData.put("event.detail.sourceId", "my-feed");

        calendar.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(new DomEvent(calendar.getElement(), eventName, eventData));
    }

    // -------------------------------------------------------------------------
    // RemoteEntryDroppedEvent — construction
    // -------------------------------------------------------------------------

    @Test
    void entrySourceEntryDroppedEvent_populatesEntry() {
        FullCalendar cal = new FullCalendar();
        ObjectNode entryData = JsonFactory.createObject();
        entryData.put("id", "ext-1");
        entryData.put("start", "2025-03-10T10:00:00Z");
        entryData.put("end", "2025-03-10T11:00:00Z");
        entryData.put("allDay", false);

        ObjectNode delta = JsonFactory.createObject();
        delta.put("years", 0);
        delta.put("months", 0);
        delta.put("days", 1);
        delta.put("milliseconds", 0L);

        RemoteEntryDroppedEvent event = new RemoteEntryDroppedEvent(cal, true, entryData, delta, "my-feed");

        assertNotNull(event.getEntry());
        assertEquals("my-feed", event.getSourceId());
        assertNotNull(event.getOldStart());
        assertNotNull(event.getOldEnd());
        // oldStart should be 1 day before newStart (2025-03-10 -> 2025-03-09)
        assertEquals(event.getEntry().getStart().minusDays(1), event.getOldStart());
    }

    // -------------------------------------------------------------------------
    // RemoteEntryResizedEvent — construction
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // getEventSourceById
    // -------------------------------------------------------------------------

    @Test
    void getRemoteEntrySourceById_found() {
        JsonFeedEntrySource source = new JsonFeedEntrySource("/api").withId("src-1");
        calendar.addRemoteEntrySource(source);
        assertTrue(calendar.getRemoteEntrySourceById("src-1").isPresent());
        assertSame(source, calendar.getRemoteEntrySourceById("src-1").get());
    }

    @Test
    void getRemoteEntrySourceById_notFound() {
        assertTrue(calendar.getRemoteEntrySourceById("nonexistent").isEmpty());
    }

    @Test
    void getRemoteEntrySourceById_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.getRemoteEntrySourceById(null));
    }

    // -------------------------------------------------------------------------
    // RemoteEntrySource — new properties
    // -------------------------------------------------------------------------

    @Test
    void eventSource_resourceEditable_defaultAbsent() {
        ObjectNode json = new JsonFeedEntrySource("/api").toJson();
        assertFalse(json.has("resourceEditable"));
    }

    @Test
    void eventSource_resourceEditable_whenSet() {
        ObjectNode json = new JsonFeedEntrySource("/api").withResourceEditable(true).toJson();
        assertTrue(json.get("resourceEditable").asBoolean());
    }

    @Test
    void eventSource_defaultAllDay_defaultAbsent() {
        ObjectNode json = new JsonFeedEntrySource("/api").toJson();
        assertFalse(json.has("defaultAllDay"));
    }

    @Test
    void eventSource_defaultAllDay_whenSet() {
        ObjectNode json = new JsonFeedEntrySource("/api").withDefaultAllDay(true).toJson();
        assertTrue(json.get("defaultAllDay").asBoolean());
    }

    @Test
    void eventSource_allow_defaultAbsent() {
        ObjectNode json = new JsonFeedEntrySource("/api").toJson();
        assertFalse(json.has("allow"));
    }

    @Test
    void eventSource_allow_whenSet() {
        ObjectNode json = new JsonFeedEntrySource("/api").withAllow("function() { return true; }").toJson();
        // FullCalendar reads the per-source callback as "allow"; "eventAllow" is the calendar-wide option only
        assertEquals("function() { return true; }", json.get("allow").get("__jsCallback").asString());
        assertFalse(json.has("eventAllow"));
    }

    @Test
    void eventSource_success_defaultAbsent() {
        ObjectNode json = new JsonFeedEntrySource("/api").toJson();
        assertFalse(json.has("success"));
    }

    @Test
    void eventSource_success_whenSet() {
        ObjectNode json = new JsonFeedEntrySource("/api").withSuccess("function(content) {}").toJson();
        assertTrue(json.get("success").isObject());
        assertEquals("function(content) {}", json.get("success").get("__jsCallback").asString());
    }

    @Test
    void eventSource_failure_defaultAbsent() {
        ObjectNode json = new JsonFeedEntrySource("/api").toJson();
        assertFalse(json.has("failure"));
    }

    @Test
    void eventSource_failure_whenSet() {
        ObjectNode json = new JsonFeedEntrySource("/api").withFailure("function(err) {}").toJson();
        assertTrue(json.get("failure").isObject());
        assertEquals("function(err) {}", json.get("failure").get("__jsCallback").asString());
    }

    @Test
    void eventSource_eventDataTransform_defaultAbsent() {
        ObjectNode json = new JsonFeedEntrySource("/api").toJson();
        assertFalse(json.has("eventDataTransform"));
    }

    @Test
    void eventSource_eventDataTransform_whenSet() {
        ObjectNode json = new JsonFeedEntrySource("/api").withEventDataTransform("function(e) { return e; }").toJson();
        assertTrue(json.get("eventDataTransform").isObject());
        assertEquals("function(e) { return e; }", json.get("eventDataTransform").get("__jsCallback").asString());
    }

    // -------------------------------------------------------------------------
    // RemoteEntryResizedEvent — construction
    // -------------------------------------------------------------------------

    @Test
    void entrySourceEntryResizedEvent_populatesEntry() {
        FullCalendar cal = new FullCalendar();
        ObjectNode entryData = JsonFactory.createObject();
        entryData.put("id", "ext-2");
        entryData.put("start", "2025-03-10T10:00:00Z");
        entryData.put("end", "2025-03-10T12:00:00Z");
        entryData.put("allDay", false);

        ObjectNode delta = JsonFactory.createObject();
        delta.put("years", 0);
        delta.put("months", 0);
        delta.put("days", 0);
        delta.put("milliseconds", 3600000L); // +1 hour

        RemoteEntryResizedEvent event = new RemoteEntryResizedEvent(cal, true, entryData, delta, "my-feed");

        assertNotNull(event.getEntry());
        assertEquals("my-feed", event.getSourceId());
        assertNotNull(event.getOldEnd());
        // oldEnd should be 1 hour before newEnd
        assertEquals(event.getEntry().getEnd().minusHours(1), event.getOldEnd());
    }
}
