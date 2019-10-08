/*
 * Copyright 2018, Stefan Uebe
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Represents a timezone, that is usable by the calendar. The timezone is identified by a zone id and a client side
 * representation. The client side representation may differ from the zone id.
 */
public class Timezone implements ClientSideValue {

    private static final ZoneId ZONE_ID_UTC = ZoneId.of("UTC");

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
     * @return timezones
     */
    public static Timezone[] getAvailableZones() {
        return AVAILABLE_ZONES;
    }

    /**
     * Creates a new instance based on the given zone id. The zone id is also used as client side representation.
     * @param zoneId zone id
     * @throws NullPointerException when zoneId is null
     */
    public Timezone(@NotNull ZoneId zoneId) {
        this(zoneId, zoneId.getId());
    }

    /**
     * Creates a new instance based on the given zone id and client side value.
     * @param zoneId zone id
     * @param clientSideValue client side value
     * @throws NullPointerException when zoneId is null
     */
    public Timezone(@NotNull ZoneId zoneId, String clientSideValue) {
        Objects.requireNonNull(zoneId);
        this.clientSideValue = clientSideValue;
        this.zoneId = zoneId;
    }

    /**
     * Returns the client side value of this instance.
     * @return client side value
     */
    @Override
    public String getClientSideValue() {
        return this.clientSideValue;
    }

    /**
     * Returns the zone id of this instance. Never null.
     * @return zone id
     */
    public ZoneId getZoneId() {
        return zoneId;
    }

    /**
     * Formats the given instant based on the zone id to be sent to the client side.
     * For UTC based timezones the string will end on a Z, all other zone ids are parsed as local date versions.
     *
     * @param instant instant
     * @return formatted date time
     * @throws NullPointerException when null is passed
     */
    public String formatWithZoneId(@NotNull Instant instant) {
        Objects.requireNonNull(instant);
        if (this == UTC || this.zoneId.equals(ZONE_ID_UTC)) {
            return instant.toString();
        }

        ZoneId zoneId = getZoneId();
        LocalDateTime temporal = LocalDateTime.ofInstant(instant, zoneId);

        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(temporal);
    }

    /**
     * Applies the rules of this timezone on the given local date and creates an instant at the start
     * of the given day. Please check the additional documentation on
     * {@link java.time.zone.ZoneRules#getOffset(LocalDateTime)}
     * @param date local date
     * @return instant
     */
    public Instant convertToUTC(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return convertToUTC(dateTime);
    }

    /**
     * Applies the rules of this timezone on the given local date time and creates an instant. Please check
     * the additional documentation on {@link java.time.zone.ZoneRules#getOffset(LocalDateTime)}
     * @param dateTime local date time
     * @return instant
     */
    public Instant convertToUTC(LocalDateTime dateTime) {
        return dateTime.toInstant(getZoneId().getRules().getOffset(dateTime));
    }

    /**
     * Applies the rules of this timezone on the given instant and creates a local date time.
     * @param instant instant
     * @return local date time
     */
    public LocalDateTime convertToLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, getZoneId());
    }

    /**
     * Applies the rules of this timezone on the given instant and creates a local date.
     * @param instant instant
     * @return local date
     */
    public LocalDate convertToLocalDate(Instant instant) {
        return convertToLocalDateTime(instant).toLocalDate();
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