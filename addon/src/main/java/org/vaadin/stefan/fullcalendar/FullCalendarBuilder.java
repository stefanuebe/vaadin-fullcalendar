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

import elemental.json.JsonObject;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * This class can be used to create FullCalendar instances via a fluent builder api. Every method returns
 * an immutable builder instance.
 */
public class FullCalendarBuilder {

    // TODO convert to lombok based builder

    private final boolean autoBrowserTimezone;
    private final boolean scheduler;
    private final int entryLimit;
    private final String schedulerLicenseKey;
    private final JsonObject initialOptions;

    private FullCalendarBuilder(boolean scheduler, int entryLimit, boolean autoBrowserTimezone, String schedulerLicenseKey, JsonObject initialOptions) {
        this.scheduler = scheduler;
        this.entryLimit = entryLimit;
        this.autoBrowserTimezone = autoBrowserTimezone;
        this.schedulerLicenseKey = schedulerLicenseKey;
        this.initialOptions = initialOptions;
    }

    /**
     * Creates a new builder instance with default settings.
     *
     * @return builder instance
     */
    public static FullCalendarBuilder create() {
        return new FullCalendarBuilder(false, -1, false, null, null);
    }

    /**
     * Activates Scheduler support. Sets no license key. When {@link #withScheduler(String)} has been used before,
     * the license key passed there, will be kept internally.
     * <br><br>
     * <b>Note: </b> You need to add the FullCalender Scheduler extension addon to the class path, otherwise
     * the build will fail.
     *
     * @return new immutable instance with updated settings
     */
    public FullCalendarBuilder withScheduler() {
        return new FullCalendarBuilder(true, entryLimit, autoBrowserTimezone, schedulerLicenseKey, initialOptions);
    }

    /**
     * Activates Scheduler support. The given string will be used as scheduler license key.
     * <br><br>
     * <b>Note: </b> You need to add the FullCalender Scheduler extension addon to the class path, otherwise
     * the build will fail.
     *
     * @param licenseKey scheduler license key to be used
     * @return new immutable instance with updated settings
     */
    public FullCalendarBuilder withScheduler(String licenseKey) {
        return new FullCalendarBuilder(true, entryLimit, autoBrowserTimezone, licenseKey, initialOptions);
    }

    /**
     * Expects the default limit of entries shown per day. This does not affect basic or
     * list views.
     * <br><br>
     * Passing a negative number or 0 disabled the entry limit (same as not using this method at all).
     *
     * @param entryLimit limit
     * @return new immutable instance with updated settings
     */
    public FullCalendarBuilder withEntryLimit(int entryLimit) {
        return new FullCalendarBuilder(scheduler, entryLimit, autoBrowserTimezone, schedulerLicenseKey, initialOptions);
    }

    /**
     * Sets automatically the browser timezone as timezone for this browser.
     *
     * @return new immutable instance with updated settings
     */
    public FullCalendarBuilder withAutoBrowserTimezone() {
        return new FullCalendarBuilder(scheduler, entryLimit, true, schedulerLicenseKey, initialOptions);
    }

    /**
     * Sets the given json object as initial options for the calendar. This allows a full override of the default
     * initial options, that the calendar would normally receive. It also overrides settings from
     * other builder methods except for the {@code withScheduler}, regardless of the method call order.
     * Theoretically you can set all options, as long as they are not based on a client side variable
     * (as for instance "plugins" or "locales"). Complex objects are possible, too, for instance for view-specific
     * settings. Please refer to the official FC documentation regarding potential options.
     *
     * <br><br>
     * Client side event handlers, that are technically also a part of the options are still applied to
     * the options object. However you may set your own event handlers with the correct name. In that case
     * they will be taken into account instead of the default ones.
     * <br><br>
     * Plugins (key "plugins") will always be set on the client side (and thus override any key passed with this
     * object), since they are needed for a functional calendar. This may change in future. Same for locales
     * (key "locales").
     * <br><br>
     * Please be aware, that incorrect options or event handler overriding can lead to unpredictable errors,
     * which will NOT be supported in any case.
     * <br><br>
     * Also, options set this way are not cached in the server side state. Calling any of the
     * {@code getOption(...)} methods will result in {@code null} (or the respective native default).
     *
     * @param initialOptions json object
     * @return new immutable instance with updated settings
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs">FullCalendar documentation</a>
     */
    public FullCalendarBuilder withInitialOptions(@NotNull JsonObject initialOptions) {
        return new FullCalendarBuilder(scheduler, entryLimit, autoBrowserTimezone, schedulerLicenseKey, Objects.requireNonNull(initialOptions));
    }

    /**
     * Builds the FullCalendar with the settings of this instance. Depending on some settings the returned
     * instance might be a subclass of {@link FullCalendar}.
     *
     * @return FullCalendar instance
     */
    public FullCalendar build() {
        FullCalendar calendar;
        if (scheduler) {
            calendar = createFullCalendarSchedulerInstance(schedulerLicenseKey);
        } else {
            calendar = createFullCalendarBasicInstance();
        }

        if (initialOptions == null && autoBrowserTimezone) {
            calendar.addBrowserTimezoneObtainedListener(event -> calendar.setTimezone(event.getTimezone()));
        }

        return calendar;
    }

    /**
     * Creates a basic instance.
     *
     * @return instance
     */
    protected FullCalendar createFullCalendarBasicInstance() {
        return initialOptions != null ? new FullCalendar(initialOptions) : new FullCalendar(entryLimit);
    }

    /**
     * Creates a basic scheduler instance. Needs the scheduler addon.
     *
     * @param schedulerLicenseKey scheduler license key to be used
     * @return scheduler instance
     * @throws ExtensionNotFoundException when the scheduler extension has not been found on the class path.
     * @throws RuntimeException           on any other exception internally catched except for ClassNotFoundException
     */
    protected FullCalendar createFullCalendarSchedulerInstance(String schedulerLicenseKey) {
        try {
            Class<?> loadClass = getClass().getClassLoader().loadClass("org.vaadin.stefan.fullcalendar.FullCalendarScheduler");

            Object scheduler;

            if (initialOptions != null) {
                scheduler = loadClass.getDeclaredConstructor(JsonObject.class).newInstance(this.initialOptions);
            } else {
                scheduler = loadClass.getDeclaredConstructor(int.class).newInstance(this.entryLimit);
            }

            if (schedulerLicenseKey != null) { // set the license key, if provided
                scheduler.getClass().getMethod("setSchedulerLicenseKey", String.class).invoke(scheduler, schedulerLicenseKey);
            }

            return (FullCalendar) scheduler;
        } catch (ClassNotFoundException ce) {
            throw new ExtensionNotFoundException("Could not find scheduler extension for FullCalendar on class path. Please check you libraries / dependencies.", ce);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Thrown when an extension should be used that is not installed properly.
     */
    public static class ExtensionNotFoundException extends RuntimeException {
        public ExtensionNotFoundException() {
        }

        public ExtensionNotFoundException(String message) {
            super(message);
        }

        public ExtensionNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public ExtensionNotFoundException(Throwable cause) {
            super(cause);
        }

        public ExtensionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

}
