package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.shared.Registration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;
import tools.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.TestUtils.assertOptionalEquals;

/**
 * Tests for event source improvements.
 * Covers ClientSideEventSource subclasses, toJson() output, Option enum keys, FullCalendar API,
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
        assertEquals("startParam", Option.EXTERNAL_EVENT_SOURCE_START_PARAM.getOptionKey());
    }

    @Test
    void option_endParam_key() {
        assertEquals("endParam", Option.EXTERNAL_EVENT_SOURCE_END_PARAM.getOptionKey());
    }

    @Test
    void option_timeZoneParam_key() {
        assertEquals("timeZoneParam", Option.EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM.getOptionKey());
    }

    @Test
    void option_googleCalendarApiKey_key() {
        assertEquals("googleCalendarApiKey", Option.EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY.getOptionKey());
    }

    // -------------------------------------------------------------------------
    // Option setters
    // -------------------------------------------------------------------------

    @Test
    void setStartParam_storesOption() {
        calendar.setOption(Option.EXTERNAL_EVENT_SOURCE_START_PARAM, "from");
        assertOptionalEquals("from", calendar.getOption(Option.EXTERNAL_EVENT_SOURCE_START_PARAM));
    }

    @Test
    void setEndParam_storesOption() {
        calendar.setOption(Option.EXTERNAL_EVENT_SOURCE_END_PARAM, "to");
        assertOptionalEquals("to", calendar.getOption(Option.EXTERNAL_EVENT_SOURCE_END_PARAM));
    }

    @Test
    void setTimeZoneParam_storesOption() {
        calendar.setOption(Option.EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM, "tz");
        assertOptionalEquals("tz", calendar.getOption(Option.EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM));
    }

    @Test
    void setGoogleCalendarApiKey_storesOption() {
        calendar.setOption(Option.EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY, "AIzaSy-test");
        assertOptionalEquals("AIzaSy-test", calendar.getOption(Option.EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY));
    }

    // -------------------------------------------------------------------------
    // JsonFeedEventSource — toJson()
    // -------------------------------------------------------------------------

    @Test
    void jsonFeedEventSource_toJson_hasUrl() {
        JsonFeedEventSource source = new JsonFeedEventSource("/api/events");
        ObjectNode json = source.toJson();
        assertEquals("/api/events", json.get("url").asString());
    }

    @Test
    void jsonFeedEventSource_toJson_hasDefaultMethod() {
        ObjectNode json = new JsonFeedEventSource("/api/events").toJson();
        assertEquals("GET", json.get("method").asString());
    }

    @Test
    void jsonFeedEventSource_toJson_hasId() {
        JsonFeedEventSource source = new JsonFeedEventSource("/api/events").withId("feed-1");
        assertEquals("feed-1", source.toJson().get("id").asString());
    }

    @Test
    void jsonFeedEventSource_toJson_hasColor() {
        ObjectNode json = new JsonFeedEventSource("/api/events").withColor("steelblue").toJson();
        assertEquals("steelblue", json.get("color").asString());
    }

    @Test
    void jsonFeedEventSource_toJson_editableDefaultAbsent() {
        // editable is not set by default — null means not serialized
        ObjectNode json = new JsonFeedEventSource("/api/events").toJson();
        assertFalse(json.has("editable"), "editable should not appear when not set");
    }

    @Test
    void jsonFeedEventSource_toJson_editableWhenSet() {
        ObjectNode json = new JsonFeedEventSource("/api/events").withEditable(true).toJson();
        assertTrue(json.get("editable").asBoolean());
    }

    @Test
    void jsonFeedEventSource_toJson_extraParams() {
        ObjectNode json = new JsonFeedEventSource("/api/events")
                .withExtraParams(Map.of("roomId", "101"))
                .toJson();
        assertTrue(json.has("extraParams"));
        assertEquals("101", json.get("extraParams").get("roomId").asString());
    }

    @Test
    void jsonFeedEventSource_toJson_method_post() {
        ObjectNode json = new JsonFeedEventSource("/api/events").withMethod("POST").toJson();
        assertEquals("POST", json.get("method").asString());
    }

    @Test
    void jsonFeedEventSource_constructor_nullUrl_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new JsonFeedEventSource(null));
    }

    @Test
    void jsonFeedEventSource_toJson_classNames() {
        ObjectNode json = new JsonFeedEventSource("/api/events")
                .withClassNames(List.of("foo", "bar"))
                .toJson();
        assertTrue(json.has("classNames"));
        assertEquals("foo", json.get("classNames").get(0).asString());
        assertEquals("bar", json.get("classNames").get(1).asString());
    }

    // -------------------------------------------------------------------------
    // GoogleCalendarEventSource — toJson()
    // -------------------------------------------------------------------------

    @Test
    void googleCalendarEventSource_toJson_hasGoogleCalendarId() {
        ObjectNode json = new GoogleCalendarEventSource("abc@group.calendar.google.com").toJson();
        assertEquals("abc@group.calendar.google.com", json.get("googleCalendarId").asString());
    }

    @Test
    void googleCalendarEventSource_toJson_hasId() {
        ObjectNode json = new GoogleCalendarEventSource("abc@group.calendar.google.com")
                .withId("holidays")
                .toJson();
        assertEquals("holidays", json.get("id").asString());
    }

    @Test
    void googleCalendarEventSource_toJson_apiKeyAbsentByDefault() {
        ObjectNode json = new GoogleCalendarEventSource("abc@group.calendar.google.com").toJson();
        assertFalse(json.has("googleCalendarApiKey"));
    }

    @Test
    void googleCalendarEventSource_toJson_apiKeyWhenSet() {
        ObjectNode json = new GoogleCalendarEventSource("abc@group.calendar.google.com")
                .withApiKey("AIzaSy-override")
                .toJson();
        assertEquals("AIzaSy-override", json.get("googleCalendarApiKey").asString());
    }

    @Test
    void googleCalendarEventSource_constructor_nullId_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new GoogleCalendarEventSource(null));
    }

    // -------------------------------------------------------------------------
    // ICalendarEventSource — toJson()
    // -------------------------------------------------------------------------

    @Test
    void iCalendarEventSource_toJson_hasUrl() {
        ObjectNode json = new ICalendarEventSource("https://example.com/cal.ics").toJson();
        assertEquals("https://example.com/cal.ics", json.get("url").asString());
    }

    @Test
    void iCalendarEventSource_toJson_hasFormatIcs() {
        ObjectNode json = new ICalendarEventSource("https://example.com/cal.ics").toJson();
        assertEquals("ics", json.get("format").asString());
    }

    @Test
    void iCalendarEventSource_toJson_hasId() {
        ObjectNode json = new ICalendarEventSource("https://example.com/cal.ics")
                .withId("holiday-ics")
                .toJson();
        assertEquals("holiday-ics", json.get("id").asString());
    }

    @Test
    void iCalendarEventSource_constructor_nullUrl_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new ICalendarEventSource(null));
    }

    // -------------------------------------------------------------------------
    // addEventSource / removeEventSource / getEventSources
    // -------------------------------------------------------------------------

    @Test
    void addClientSideEventSource_registersSource() {
        JsonFeedEventSource source = new JsonFeedEventSource("/api").withId("src-1");
        calendar.addClientSideEventSource(source);
        assertTrue(calendar.getClientSideEventSources().contains(source));
    }

    @Test
    void addClientSideEventSource_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.addClientSideEventSource(null));
    }

    @Test
    void removeClientSideEventSource_removesFromRegistry() {
        JsonFeedEventSource source = new JsonFeedEventSource("/api").withId("src-1");
        calendar.addClientSideEventSource(source);
        calendar.removeClientSideEventSource("src-1");
        assertFalse(calendar.getClientSideEventSources().contains(source));
    }

    @Test
    void removeClientSideEventSource_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.removeClientSideEventSource(null));
    }

    @Test
    void setClientSideEventSources_replacesAll() {
        calendar.addClientSideEventSource(new JsonFeedEventSource("/old").withId("old"));
        JsonFeedEventSource newSource = new JsonFeedEventSource("/new").withId("new");
        calendar.setClientSideEventSources(List.of(newSource));
        Collection<ClientSideEventSource<?>> sources = calendar.getClientSideEventSources();
        assertEquals(1, sources.size());
        assertTrue(sources.contains(newSource));
    }

    @Test
    void setClientSideEventSources_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.setClientSideEventSources(null));
    }

    @Test
    void getClientSideEventSources_emptyInitially() {
        assertTrue(calendar.getClientSideEventSources().isEmpty());
    }

    @Test
    void refetchEvents_doesNotThrow() {
        assertDoesNotThrow(() -> calendar.refetchEvents());
    }

    // -------------------------------------------------------------------------
    // Listener registration
    // -------------------------------------------------------------------------

    @Test
    void addEventSourceFailureListener_returnsRegistration() {
        Registration reg = calendar.addEventSourceFailureListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addEventSourceFailureListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.addEventSourceFailureListener(null));
    }

    @Test
    void addExternalEntryDroppedListener_returnsRegistration() {
        Registration reg = calendar.addExternalEntryDroppedListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addExternalEntryDroppedListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.addExternalEntryDroppedListener(null));
    }

    @Test
    void addExternalEntryResizedListener_returnsRegistration() {
        Registration reg = calendar.addExternalEntryResizedListener(event -> {});
        assertNotNull(reg);
    }

    @Test
    void addExternalEntryResizedListener_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.addExternalEntryResizedListener(null));
    }

    // -------------------------------------------------------------------------
    // ExternalEntryDroppedEvent — construction
    // -------------------------------------------------------------------------

    @Test
    void externalEntryDroppedEvent_populatesEntry() {
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

        ExternalEntryDroppedEvent event = new ExternalEntryDroppedEvent(cal, true, entryData, delta, "my-feed");

        assertNotNull(event.getEntry());
        assertEquals("my-feed", event.getSourceId());
        assertNotNull(event.getOldStart());
        assertNotNull(event.getOldEnd());
        // oldStart should be 1 day before newStart (2025-03-10 -> 2025-03-09)
        assertEquals(event.getEntry().getStart().minusDays(1), event.getOldStart());
    }

    // -------------------------------------------------------------------------
    // ExternalEntryResizedEvent — construction
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // getEventSourceById
    // -------------------------------------------------------------------------

    @Test
    void getClientSideEventSourceById_found() {
        JsonFeedEventSource source = new JsonFeedEventSource("/api").withId("src-1");
        calendar.addClientSideEventSource(source);
        assertTrue(calendar.getClientSideEventSourceById("src-1").isPresent());
        assertSame(source, calendar.getClientSideEventSourceById("src-1").get());
    }

    @Test
    void getClientSideEventSourceById_notFound() {
        assertTrue(calendar.getClientSideEventSourceById("nonexistent").isEmpty());
    }

    @Test
    void getClientSideEventSourceById_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.getClientSideEventSourceById(null));
    }

    // -------------------------------------------------------------------------
    // ClientSideEventSource — new properties
    // -------------------------------------------------------------------------

    @Test
    void eventSource_resourceEditable_defaultAbsent() {
        ObjectNode json = new JsonFeedEventSource("/api").toJson();
        assertFalse(json.has("resourceEditable"));
    }

    @Test
    void eventSource_resourceEditable_whenSet() {
        ObjectNode json = new JsonFeedEventSource("/api").withResourceEditable(true).toJson();
        assertTrue(json.get("resourceEditable").asBoolean());
    }

    @Test
    void eventSource_defaultAllDay_defaultAbsent() {
        ObjectNode json = new JsonFeedEventSource("/api").toJson();
        assertFalse(json.has("defaultAllDay"));
    }

    @Test
    void eventSource_defaultAllDay_whenSet() {
        ObjectNode json = new JsonFeedEventSource("/api").withDefaultAllDay(true).toJson();
        assertTrue(json.get("defaultAllDay").asBoolean());
    }

    @Test
    void eventSource_allow_defaultAbsent() {
        ObjectNode json = new JsonFeedEventSource("/api").toJson();
        assertFalse(json.has("eventAllow"));
    }

    @Test
    void eventSource_allow_whenSet() {
        ObjectNode json = new JsonFeedEventSource("/api").withAllow("function() { return true; }").toJson();
        assertTrue(json.get("eventAllow").isObject());
        assertEquals("function() { return true; }", json.get("eventAllow").get("__jsCallback").asString());
    }

    @Test
    void eventSource_success_defaultAbsent() {
        ObjectNode json = new JsonFeedEventSource("/api").toJson();
        assertFalse(json.has("success"));
    }

    @Test
    void eventSource_success_whenSet() {
        ObjectNode json = new JsonFeedEventSource("/api").withSuccess("function(content) {}").toJson();
        assertTrue(json.get("success").isObject());
        assertEquals("function(content) {}", json.get("success").get("__jsCallback").asString());
    }

    @Test
    void eventSource_failure_defaultAbsent() {
        ObjectNode json = new JsonFeedEventSource("/api").toJson();
        assertFalse(json.has("failure"));
    }

    @Test
    void eventSource_failure_whenSet() {
        ObjectNode json = new JsonFeedEventSource("/api").withFailure("function(err) {}").toJson();
        assertTrue(json.get("failure").isObject());
        assertEquals("function(err) {}", json.get("failure").get("__jsCallback").asString());
    }

    @Test
    void eventSource_eventDataTransform_defaultAbsent() {
        ObjectNode json = new JsonFeedEventSource("/api").toJson();
        assertFalse(json.has("eventDataTransform"));
    }

    @Test
    void eventSource_eventDataTransform_whenSet() {
        ObjectNode json = new JsonFeedEventSource("/api").withEventDataTransform("function(e) { return e; }").toJson();
        assertTrue(json.get("eventDataTransform").isObject());
        assertEquals("function(e) { return e; }", json.get("eventDataTransform").get("__jsCallback").asString());
    }

    // -------------------------------------------------------------------------
    // ExternalEntryResizedEvent — construction
    // -------------------------------------------------------------------------

    @Test
    void externalEntryResizedEvent_populatesEntry() {
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

        ExternalEntryResizedEvent event = new ExternalEntryResizedEvent(cal, true, entryData, delta, "my-feed");

        assertNotNull(event.getEntry());
        assertEquals("my-feed", event.getSourceId());
        assertNotNull(event.getOldEnd());
        // oldEnd should be 1 hour before newEnd
        assertEquals(event.getEntry().getEnd().minusHours(1), event.getOldEnd());
    }
}
