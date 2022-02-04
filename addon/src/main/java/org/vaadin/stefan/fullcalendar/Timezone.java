/*
 * Copyright 2020, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar;

import javax.validation.constraints.NotNull;
import java.time.*;
import java.util.*;

/**
 * Represents a timezone, that is usable by the calendar. The timezone is identified by a zone id and a client side
 * representation. The client side representation may differ from the zone id.
 */
public class Timezone implements ClientSideValue {

    public static final ZoneId ZONE_ID_UTC = ZoneId.of("UTC");

    /**
     * Constant for the timezone UTC. Default for conversions, if no custom timezone is set.
     */
    public static final Timezone UTC = new Timezone(ZONE_ID_UTC, "UTC");

    private static final Timezone[] AVAILABLE_ZONES;

    static {
        Timezone[] timezones = ZoneId.getAvailableZoneIds().stream()
                .sorted()
                .filter(s -> !s.equalsIgnoreCase("utc"))
                .map(ZoneId::of)
                .map(Timezone::new)
                .toArray(Timezone[]::new);

        List<Timezone> timezonesList = new ArrayList<>(Collections.singletonList(UTC));
        timezonesList.addAll(Arrays.asList(timezones));

        AVAILABLE_ZONES = timezonesList.toArray(new Timezone[0]);
    }

    private final String clientSideValue;
    private ZoneId zoneId;

    /**
     * Returns all available timezones. This arrayy bases on all constants of this class plus all available zone ids
     * returned by {@link ZoneId#getAvailableZoneIds()}.
     *
     * @return timezones
     */
    public static Timezone[] getAvailableZones() {
        return AVAILABLE_ZONES;
    }

    /**
     * Creates a new instance based on the given zone id. The zone id is also used as client side representation.
     *
     * @param zoneId zone id
     * @throws NullPointerException when zoneId is null
     */
    public Timezone(@NotNull ZoneId zoneId) {
        this(zoneId, zoneId.getId());
    }

    /**
     * Creates a new instance based on the given zone id and client side value.
     *
     * @param zoneId          zone id
     * @param clientSideValue client side value
     * @throws NullPointerException when zoneId is null
     */
    public Timezone(@NotNull ZoneId zoneId, String clientSideValue) {
        Objects.requireNonNull(zoneId);
        this.clientSideValue = clientSideValue;
        this.zoneId = zoneId;
    }

    /**
     * Returns a new timezone instance representing the system's current timezone.
     *
     * @return system's timezone
     */
    public static Timezone getSystem() {
        return new Timezone(ZoneId.systemDefault());
    }

    /**
     * Returns the client side value of this instance.
     *
     * @return client side value
     */
    @Override
    public String getClientSideValue() {
        return this.clientSideValue;
    }

    /**
     * Returns the zone id of this instance. Never null.
     *
     * @return zone id
     */
    public ZoneId getZoneId() {
        return zoneId;
    }

    /**
     * Creates a zoned date time based on this timezone interpreting the given local date time as UTC time.
     * Passing ...T00:00 to a GMT+1 instance will result in local date time ...T01:00+01:00
     * <p/>
     * For the UTC instance this method will not modify anything. Passing null will return null.
     * @param localDateTime local date time to convert to a zoned date time
     * @return zoned date time representing the given local date time at this timezone
     */
    public ZonedDateTime applyTimezone(LocalDateTime localDateTime) {
        return localDateTime != null ? ZonedDateTime.of(localDateTime, ZONE_ID_UTC).withZoneSameInstant(getZoneId()) : null;
    }

    /**
     * Creates a UTC zoned date time by interpreting the given local date time as a timestamp of this time zone.
     * Passing ...T01:00 to a GMT+1 instance will result in local date time ...T00:00Z
     * <p/>
     * For the UTC instance this method will not modify anything.. Passing null will return null.
     * @param localDateTime local date time to convert to a zoned date time
     * @return zoned date time representing the given local date time at this timezone
     */
    public ZonedDateTime removeTimezone(LocalDateTime localDateTime) {
        return localDateTime != null ? ZonedDateTime.of(localDateTime, getZoneId()).withZoneSameInstant(ZONE_ID_UTC) : null;
    }



    /**
     * Creates a local date time based by apply the zone offset of this timezone onto the given local date time.
     * Any offset modifies like daylight saving will be based on the given local date time.
     * Passing ...T00:00 to a GMT+1 instance will result in local date time ...T01:00
     * <p/>
     * For the UTC instance this method will not modify anything, but return a new local date time instance.
     * Passing null will return null.
     * @param localDateTime local date time to convert to a zoned date time
     * @return zoned date time representing the given local date time at this timezone
     */
    public LocalDateTime applyTimezoneOffset(LocalDateTime localDateTime) {
        return localDateTime != null ? applyTimezone(localDateTime).toLocalDateTime() : null;
    }

    /**
     * Creates a local date time based by removing the zone offset of this timezone from the given local date time.
     * Any offset modifies like daylight saving will be based on the given local date time.
     * Passing ...T01:00 to a GMT+1 instance will result in local date time ...T00:00
     * <p/>
     * For the UTC instance this method will not modify anything, but return a new local date time instance.
     * Passing null will return null.
     * @param localDateTime local date time to convert to a zoned date time
     * @return zoned date time representing the given local date time at this timezone
     */
    public LocalDateTime removeTimezoneOffset(LocalDateTime localDateTime) {
        return localDateTime != null ? removeTimezone(localDateTime).toLocalDateTime() : null;
    }

    /**
     * Converts the given local date time to a zoned date time without changing the time itself other then.
     * Passing null will return null.
     * {@link #applyTimezone(LocalDateTime)} or {@link #removeTimezone(LocalDateTime)}.
     * @param localDateTime local date time  (nullable)
     * @return zoned date time
     */
    public ZonedDateTime asZonedDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? ZonedDateTime.of(localDateTime, getZoneId()) : null;
    }

    /**
     * Applies the rules of this timezone on the given local date and creates an instant at the start
     * of the given day. Please check the additional documentation on
     * {@link java.time.zone.ZoneRules#getOffset(LocalDateTime)}
     * @param date local date (nullable)
     * @return instant or null
     */
    public Instant convertToInstant(LocalDate date) {
        if (date == null) {
            return null;
        }
        LocalDateTime dateTime = date.atStartOfDay();
        return convertToInstant(dateTime);
    }

    /**
     * Applies the rules of this timezone on the given local date time and creates an instant. Please check
     * the additional documentation on {@link java.time.zone.ZoneRules#getOffset(LocalDateTime)}
     * @param dateTime local date time (nullable)
     * @return instant or null
     */
    public Instant convertToInstant(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toInstant(getZoneId().getRules().getOffset(dateTime)) : null;
    }

    /**
     * Applies the rules of this timezone on the given instant and creates a local date time.
     * @param instant instant (nullable)
     * @return local date time or null
     */
    public LocalDateTime convertToLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, getZoneId()) : null;
    }

    /**
     * Applies the rules of this timezone on the given instant and creates a local date.
     * @param instant instant (nullable)
     * @return local date or null
     */
    public LocalDate convertToLocalDate(Instant instant) {
        return instant != null ? convertToLocalDateTime(instant).toLocalDate() : null;
    }

    @Override
    public String toString() {
        return "Timezone{" +
                "clientSideValue='" + clientSideValue + '\'' +
                ", zoneId=" + zoneId +
                '}';
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