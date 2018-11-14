package org.vaadin.stefan.fullcalendar;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Timezone implements ClientSideValue {
    private static final ZoneId ZONE_ID_UTC = ZoneId.of("UTC");
    public static final Timezone LOCAL = new Timezone("local", ZONE_ID_UTC);
    public static final Timezone UTC = new Timezone("UTC", ZONE_ID_UTC);
    private static final Timezone[] AVAILABLE_ZONES;

    static {
        Timezone[] timezones = ZoneId.getAvailableZoneIds().stream()
                .sorted()
                .filter(s -> !s.equalsIgnoreCase("utc"))
                .map(ZoneId::of)
                .map(Timezone::new)
                .toArray(Timezone[]::new);

        List<Timezone> timezonesList = new ArrayList<>(Arrays.asList(LOCAL, UTC));
        timezonesList.addAll(Arrays.asList(timezones));

        AVAILABLE_ZONES = timezonesList.toArray(new Timezone[0]);
    }

    private final String clientSideValue;
    private ZoneId zoneId;

    public Timezone(ZoneId zoneId) {
        this(zoneId.getId(), zoneId);
    }

    private Timezone(String clientSideValue, ZoneId zoneId) {
        this.clientSideValue = clientSideValue;
        this.zoneId = zoneId;
    }

    @Nullable
    @Override
    public String getClientSideValue() {
        return this.clientSideValue;
    }

    public static Timezone[] getAvailableZones() {
        return AVAILABLE_ZONES;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public String formatWithZoneId(Instant instant) {
        if (this == UTC || this == LOCAL) {
            return instant.toString();
        }

        ZoneId zoneId = getZoneId();
        LocalDateTime temporal = LocalDateTime.ofInstant(instant, zoneId);

        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(temporal);
    }

    @Override
    public String toString() {
        return getClientSideValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timezone timezone = (Timezone) o;
        return Objects.equals(clientSideValue, timezone.clientSideValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientSideValue);
    }

    /**
     * Thrown when there is no timezone defined or could not be obtained from the client.
     */
    public static class TimezoneNotFoundException extends RuntimeException {

        public TimezoneNotFoundException(String message) {
            super(message);
        }

        public TimezoneNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public TimezoneNotFoundException(Throwable cause) {
            super(cause);
        }
    }
}