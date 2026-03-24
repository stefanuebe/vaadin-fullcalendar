package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonValue;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.converters.*;
import org.vaadin.stefan.fullcalendar.model.Header;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterPart;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterPartPosition;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    // --- DayOfWeekConverter ---

    @Test
    void dayOfWeekConverter_monday() {
        DayOfWeekConverter<Object> c = new DayOfWeekConverter<>();
        assertTrue(c.supports(DayOfWeek.MONDAY));
        JsonValue result = c.toClientModel(DayOfWeek.MONDAY, null);
        assertEquals(1.0, result.asNumber());
    }

    @Test
    void dayOfWeekConverter_sunday() {
        DayOfWeekConverter<Object> c = new DayOfWeekConverter<>();
        JsonValue result = c.toClientModel(DayOfWeek.SUNDAY, null);
        assertEquals(0.0, result.asNumber());
    }

    @Test
    void dayOfWeekConverter_doesNotSupportString() {
        DayOfWeekConverter<Object> c = new DayOfWeekConverter<>();
        assertFalse(c.supports("MONDAY"));
    }

    // --- DayOfWeekArrayConverter ---

    @Test
    void dayOfWeekArrayConverter_collection() {
        DayOfWeekArrayConverter<Object> c = new DayOfWeekArrayConverter<>();
        assertTrue(c.supports(Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)));
        JsonValue result = c.toClientModel(Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), null);
        assertNotNull(result);
        assertEquals("array", result.getType().name().toLowerCase());
    }

    @Test
    void dayOfWeekArrayConverter_array() {
        DayOfWeekArrayConverter<Object> c = new DayOfWeekArrayConverter<>();
        DayOfWeek[] arr = {DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY};
        assertTrue(c.supports(arr));
        JsonValue result = c.toClientModel(arr, null);
        assertNotNull(result);
    }

    // --- DurationConverter ---

    @Test
    void durationConverter_duration() {
        DurationConverter<Object> c = new DurationConverter<>();
        assertTrue(c.supports(Duration.ofMinutes(30)));
        JsonValue result = c.toClientModel(Duration.ofMinutes(30), null);
        assertEquals("00:30:00", result.asString());
    }

    @Test
    void durationConverter_localTime() {
        DurationConverter<Object> c = new DurationConverter<>();
        assertTrue(c.supports(LocalTime.of(6, 0)));
        JsonValue result = c.toClientModel(LocalTime.of(6, 0), null);
        assertEquals("06:00:00", result.asString());
    }

    @Test
    void durationConverter_null() {
        DurationConverter<Object> c = new DurationConverter<>();
        assertTrue(c.supports(null));
        JsonValue result = c.toClientModel(null, null);
        assertEquals("NULL", result.getType().name());
    }

    // --- LocaleConverter ---

    @Test
    void localeConverter() {
        LocaleConverter<Object> c = new LocaleConverter<>();
        assertTrue(c.supports(Locale.GERMANY));
        JsonValue result = c.toClientModel(Locale.GERMANY, null);
        assertEquals("de-de", result.asString());
    }

    @Test
    void localeConverter_english() {
        LocaleConverter<Object> c = new LocaleConverter<>();
        JsonValue result = c.toClientModel(Locale.US, null);
        assertEquals("en-us", result.asString());
    }

    // --- StringArrayConverter ---

    @Test
    void stringArrayConverter_array() {
        StringArrayConverter<Object> c = new StringArrayConverter<>();
        assertTrue(c.supports(new String[]{"a", "b"}));
        JsonValue result = c.toClientModel(new String[]{"a", "b"}, null);
        assertEquals("a,b", result.asString());
    }

    @Test
    void stringArrayConverter_collection() {
        StringArrayConverter<Object> c = new StringArrayConverter<>();
        assertTrue(c.supports(List.of("x", "y", "z")));
        JsonValue result = c.toClientModel(List.of("x", "y", "z"), null);
        assertEquals("x,y,z", result.asString());
    }

    // --- BusinessHoursConverter ---

    @Test
    void businessHoursConverter_single() {
        BusinessHoursConverter<Object> c = new BusinessHoursConverter<>();
        BusinessHours bh = new BusinessHours(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        assertTrue(c.supports(bh));
        JsonValue result = c.toClientModel(bh, null);
        assertEquals("OBJECT", result.getType().name());
    }

    @Test
    void businessHoursConverter_array() {
        BusinessHoursConverter<Object> c = new BusinessHoursConverter<>();
        BusinessHours[] arr = {
                new BusinessHours(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                new BusinessHours(DayOfWeek.SATURDAY)
        };
        assertTrue(c.supports(arr));
        JsonValue result = c.toClientModel(arr, null);
        assertEquals("ARRAY", result.getType().name());
    }

    // --- ToolbarConverter ---

    @Test
    void toolbarConverter() {
        ToolbarConverter<Object> c = new ToolbarConverter<>();
        Header header = new Header(List.of(new HeaderFooterPart(HeaderFooterPartPosition.START)));
        assertTrue(c.supports(header));
        JsonValue result = c.toClientModel(header, null);
        assertEquals("OBJECT", result.getType().name());
    }

    // --- ClientSideValueConverter ---

    @Test
    void clientSideValueConverter() {
        ClientSideValueConverter<Object> c = new ClientSideValueConverter<>();
        ClientSideValue csv = () -> "test-value";
        assertTrue(c.supports(csv));
        JsonValue result = c.toClientModel(csv, null);
        assertEquals("test-value", result.asString());
    }

    @Test
    void clientSideValueConverter_doesNotSupportString() {
        ClientSideValueConverter<Object> c = new ClientSideValueConverter<>();
        assertFalse(c.supports("plain string"));
    }

    // --- BeanProperties Converter Caching ---

    @Test
    void beanPropertiesCachesConverterInstances() {
        Set<BeanProperties<Entry>> props = BeanProperties.read(Entry.class);
        // Find a field that has a @JsonConverter annotation (e.g., recurringDaysOfWeek uses DayOfWeekItemConverter)
        Optional<BeanProperties<Entry>> withConverter = props.stream()
                .filter(p -> p.getConverter() != null)
                .findFirst();

        assertTrue(withConverter.isPresent(), "At least one field should have a cached converter");
        assertNotNull(withConverter.get().getConverter());
    }

    @Test
    void beanPropertiesCachesJsonName() {
        Set<BeanProperties<Entry>> props = BeanProperties.read(Entry.class);
        // All properties should have a non-null jsonName
        for (BeanProperties<Entry> prop : props) {
            assertNotNull(prop.getJsonName());
            assertFalse(prop.getJsonName().isEmpty());
        }
    }
}
