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

/**
 * This class can be used to create FullCalendar instances via a fluent builder api. Every method returns
 * an immutable builder instance.
 */
public class FullCalendarBuilder {

    private final boolean autoBrowserTimezone;
    private final boolean scheduler;
    private final int entryLimit;
    private final String schedulerLicenseKey;

    private FullCalendarBuilder(boolean scheduler, int entryLimit, boolean autoBrowserTimezone, String schedulerLicenseKey) {
        this.scheduler = scheduler;
        this.entryLimit = entryLimit;
        this.autoBrowserTimezone = autoBrowserTimezone;
        this.schedulerLicenseKey = schedulerLicenseKey;
    }

    /**
     * Creates a new builder instance with default settings.
     *
     * @return builder instance
     */
    public static FullCalendarBuilder create() {
        return new FullCalendarBuilder(false, -1, false, null);
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
        return new FullCalendarBuilder(true, entryLimit, autoBrowserTimezone, schedulerLicenseKey);
    }

    /**
     * Activates Scheduler support. The given string will be used as scheduler license key.
     * <br><br>
     * <b>Note: </b> You need to add the FullCalender Scheduler extension addon to the class path, otherwise
     * the build will fail.
     *
     * @param licenseKey scheduler license key to be used
     *
     * @return new immutable instance with updated settings
     */
    public FullCalendarBuilder withScheduler(String licenseKey) {
        return new FullCalendarBuilder(true, entryLimit, autoBrowserTimezone, licenseKey);
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
        return new FullCalendarBuilder(scheduler, entryLimit, autoBrowserTimezone, schedulerLicenseKey);
    }

    /**
     * Sets automatically the browser timezone as timezone for this browser.
     *
     * @return new immutable instance with updated settings
     */
    public FullCalendarBuilder withAutoBrowserTimezone() {
        return new FullCalendarBuilder(scheduler, entryLimit, true, schedulerLicenseKey);
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

        if (autoBrowserTimezone) {
            calendar.addBrowserTimezoneObtainedListener(event -> calendar.setTimezone(event.getTimezone()));
        }

        return calendar;
    }

    /**
     * Creates a basic instance.
     * @return instance
     */
    protected FullCalendar createFullCalendarBasicInstance() {
        return new FullCalendar(entryLimit);
    }

    /**
     * Creates a basic scheduler instance. Needs the scheduler addon.
     * @return scheduler instance
     * @throws ExtensionNotFoundException when the scheduler extension has not been found on the class path.
     * @throws RuntimeException on any other exception internally catched except for ClassNotFoundException
     * @param schedulerLicenseKey scheduler license key to be used
     */
    protected FullCalendar createFullCalendarSchedulerInstance(String schedulerLicenseKey) {
        try {
            Class<?> loadClass = getClass().getClassLoader().loadClass("org.vaadin.stefan.fullcalendar.FullCalendarScheduler");
            Object scheduler = loadClass.getDeclaredConstructor(int.class).newInstance(this.entryLimit);

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
