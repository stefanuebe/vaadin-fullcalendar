package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

public class TimezoneTests {

    public static final ZoneId UTC_ID = ZoneId.of("UTC");
    public static final Timezone CUSTOM_TIMEZONE = new Timezone(ZoneId.of("Europe/Berlin"));

    @Test
    void testStatics() {
        Assertions.assertEquals(UTC_ID, Timezone.UTC.getZoneId());

        List<Timezone> timezoneList = Arrays.asList(Timezone.getAvailableZones());
        Assertions.assertTrue(timezoneList.contains(Timezone.UTC));
    }

    @Test
    void testConstructors() {
        ZoneId zoneId = ZoneId.of("Europe/Berlin");
        Timezone timezone = new Timezone(zoneId);
        Assertions.assertEquals(zoneId, timezone.getZoneId());
        Assertions.assertEquals(zoneId.getId(), timezone.getClientSideValue());

        timezone = new Timezone(zoneId, "someOther");
        Assertions.assertEquals(zoneId, timezone.getZoneId());
        Assertions.assertEquals("someOther", timezone.getClientSideValue());
    }

    @Test
    void testFormatting() {
        Instant now = Instant.now();
        String utcString = now.toString();
        String ldtString = LocalDateTime.ofInstant(now, CUSTOM_TIMEZONE.getZoneId()).toString();

        Assertions.assertEquals(utcString, Timezone.UTC.formatWithZoneId(now));
        Assertions.assertEquals(utcString, new Timezone(UTC_ID).formatWithZoneId(now));

        Assertions.assertEquals(ldtString, CUSTOM_TIMEZONE.formatWithZoneId(now));
    }

    @Test
    void testConversion() {
        Instant instant = Instant.now();

        // TODO additional tests needed?
        Assertions.assertEquals(instant, Timezone.UTC.convertToUTC(Timezone.UTC.convertToLocalDateTime(instant)));
        Assertions.assertEquals(instant, CUSTOM_TIMEZONE.convertToUTC(CUSTOM_TIMEZONE.convertToLocalDateTime(instant)));
    }

    @Test
    void testSystemTimezone() {
        Timezone system = Timezone.getSystem();
        Assertions.assertEquals(ZoneId.systemDefault(), system.getZoneId());
    }


}
