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
 * Tests for Phase 4: Event source improvements.
 * Covers ClientSideEventSource subclasses, toJson() output, Option enum keys, FullCalendar API,
 * and the new server-side event classes.
 */
public class Phase4EventSourcesTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    // -------------------------------------------------------------------------
    // Option enum keys — Phase 4 additions
    // -------------------------------------------------------------------------

    @Test
    void option_startParam_key() {
        assertEquals("startParam", Option.START_PARAM.getOptionKey());
    }

    @Test
    void option_endParam_key() {
        assertEquals("endParam", Option.END_PARAM.getOptionKey());
    }

    @Test
    void option_timeZoneParam_key() {
        assertEquals("timeZoneParam", Option.TIME_ZONE_PARAM.getOptionKey());
    }

    @Test
    void option_googleCalendarApiKey_key() {
        assertEquals("googleCalendarApiKey", Option.GOOGLE_CALENDAR_API_KEY.getOptionKey());
    }

    // -------------------------------------------------------------------------
    // Option setters
    // -------------------------------------------------------------------------

    @Test
    void setStartParam_storesOption() {
        calendar.setStartParam("from");
        assertOptionalEquals("from", calendar.getOption(Option.START_PARAM));
    }

    @Test
    void setEndParam_storesOption() {
        calendar.setEndParam("to");
        assertOptionalEquals("to", calendar.getOption(Option.END_PARAM));
    }

    @Test
    void setTimeZoneParam_storesOption() {
        calendar.setTimeZoneParam("tz");
        assertOptionalEquals("tz", calendar.getOption(Option.TIME_ZONE_PARAM));
    }

    @Test
    void setGoogleCalendarApiKey_storesOption() {
        calendar.setGoogleCalendarApiKey("AIzaSy-test");
        assertOptionalEquals("AIzaSy-test", calendar.getOption(Option.GOOGLE_CALENDAR_API_KEY));
    }

    // -------------------------------------------------------------------------
    // JS callback setters — assertDoesNotThrow
    // -------------------------------------------------------------------------

    @Test
    void setLoadingCallback_doesNotThrow() {
        assertDoesNotThrow(() -> calendar.setLoadingCallback("function(isLoading) {}"));
    }

    @Test
    void setEventDataTransformCallback_doesNotThrow() {
        assertDoesNotThrow(() -> calendar.setEventDataTransformCallback("function(event) { return event; }"));
    }

    @Test
    void setEventSourceSuccessCallback_doesNotThrow() {
        assertDoesNotThrow(() -> calendar.setEventSourceSuccessCallback("function(content) { return content; }"));
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
    void addEventSource_registersSource() {
        JsonFeedEventSource source = new JsonFeedEventSource("/api").withId("src-1");
        calendar.addEventSource(source);
        assertTrue(calendar.getEventSources().contains(source));
    }

    @Test
    void addEventSource_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.addEventSource(null));
    }

    @Test
    void removeEventSource_removesFromRegistry() {
        JsonFeedEventSource source = new JsonFeedEventSource("/api").withId("src-1");
        calendar.addEventSource(source);
        calendar.removeEventSource("src-1");
        assertFalse(calendar.getEventSources().contains(source));
    }

    @Test
    void removeEventSource_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.removeEventSource(null));
    }

    @Test
    void setEventSources_replacesAll() {
        calendar.addEventSource(new JsonFeedEventSource("/old").withId("old"));
        JsonFeedEventSource newSource = new JsonFeedEventSource("/new").withId("new");
        calendar.setEventSources(List.of(newSource));
        Collection<ClientSideEventSource<?>> sources = calendar.getEventSources();
        assertEquals(1, sources.size());
        assertTrue(sources.contains(newSource));
    }

    @Test
    void setEventSources_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> calendar.setEventSources(null));
    }

    @Test
    void getEventSources_emptyInitially() {
        assertTrue(calendar.getEventSources().isEmpty());
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
