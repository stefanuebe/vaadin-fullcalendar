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
import org.vaadin.stefan.fullcalendar.dataprovider.EagerInMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;

import javax.validation.constraints.NotNull;
import java.util.Collection;
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
    private final EntryProvider<Entry> entryProvider;
    private final Class<? extends FullCalendar> customType;
    private final Collection<Entry> initialEntries;
    private final String entryContent;

    @SuppressWarnings("unchecked")
    private FullCalendarBuilder(boolean scheduler, int entryLimit, boolean autoBrowserTimezone, String schedulerLicenseKey, JsonObject initialOptions, EntryProvider<? extends Entry> entryProvider, Class<? extends FullCalendar> customType, Collection<Entry> initialEntries, String entryContent) {
        this.scheduler = scheduler;
        this.entryLimit = entryLimit;
        this.autoBrowserTimezone = autoBrowserTimezone;
        this.schedulerLicenseKey = schedulerLicenseKey;
        this.initialOptions = initialOptions;
        this.entryProvider = (EntryProvider<Entry>) entryProvider;
        this.customType = customType;
        this.initialEntries = initialEntries;
        this.entryContent = entryContent;
    }

    /**
     * Creates a new builder instance with default settings.
     *
     * @return builder instance
     */
    public static FullCalendarBuilder create() {
        return new FullCalendarBuilder(false, -1, false, null, null, null, null, null, null);
    }

    /**
     * Allows to define an initial collection of entries to be added to the default entry provider of the new calendar instance. Will be ignored, when
     * a custom entry provider is set.
     *
     * @param initialEntries initial entries
     * @return new immutable instance with updated settings
     */
    public FullCalendarBuilder withInitialEntries(@NotNull Collection<Entry> initialEntries) {
        return new FullCalendarBuilder(scheduler, entryLimit, autoBrowserTimezone, schedulerLicenseKey, initialOptions, entryProvider, customType, Objects.requireNonNull(initialEntries), entryContent);
    }

    /**
     * Allows to define a subclass of the FullCalendar to be used as the new instance's type. Please make sure, that this
     * class inherits all necessary constructors of the FullCalendar or Scheduler to prevent any exceptions at build time.
     * Also the constructors needs to be public.
     *
     * @param customType custom type to be used
     * @return new immutable instance with updated settings
     */
    public FullCalendarBuilder withCustomType(@NotNull Class<? extends FullCalendar> customType) {
        return new FullCalendarBuilder(scheduler, entryLimit, autoBrowserTimezone, schedulerLicenseKey, initialOptions, entryProvider, Objects.requireNonNull(customType), initialEntries, entryContent);
    }

    /**
     * Initializes the calendar with the given entry provider. By default it will be created with an instance of
     * {@link org.vaadin.stefan.fullcalendar.dataprovider.EagerInMemoryEntryProvider}.
     *
     * @param entryProvider entry provider to be used
     * @return new immutable instance with updated settings
     */
    public FullCalendarBuilder withEntryProvider(@NotNull EntryProvider<? extends Entry> entryProvider) {
        return new FullCalendarBuilder(scheduler, entryLimit, autoBrowserTimezone, schedulerLicenseKey, initialOptions, Objects.requireNonNull(entryProvider), customType, initialEntries, entryContent);
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
        return new FullCalendarBuilder(true, entryLimit, autoBrowserTimezone, schedulerLicenseKey, initialOptions, entryProvider, customType, initialEntries, entryContent);
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
        return new FullCalendarBuilder(true, entryLimit, autoBrowserTimezone, licenseKey, initialOptions, entryProvider, customType, initialEntries, entryContent);
    }

    /**
     * Expects the default limit of entries shown per day. This does not affect basic or
     * list views.
     * <br><br>
     * Passing a negative number disabled the entry limit (same as not using this method at all).
     *
     * @param entryLimit limit
     * @return new immutable instance with updated settings
     */
    public FullCalendarBuilder withEntryLimit(int entryLimit) {
        return new FullCalendarBuilder(scheduler, entryLimit, autoBrowserTimezone, schedulerLicenseKey, initialOptions, entryProvider, customType, initialEntries, entryContent);
    }

    /**
     * Sets automatically the browser timezone as timezone for this browser.
     *
     * @return new immutable instance with updated settings
     */
    public FullCalendarBuilder withAutoBrowserTimezone() {
        return new FullCalendarBuilder(scheduler, entryLimit, true, schedulerLicenseKey, initialOptions, entryProvider, customType, initialEntries, entryContent);
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
        return new FullCalendarBuilder(scheduler, entryLimit, autoBrowserTimezone, schedulerLicenseKey, Objects.requireNonNull(initialOptions), entryProvider, customType, initialEntries, entryContent);
    }

    /**
     * The given string will be interpreted as JS on the client side
     * and attached to the calendar as the "eventContent" callback. It must be a valid JavaScript expression or
     * function as described in <a href="https://fullcalendar.io/docs/content-injection">the official FC docs</a>.
     * <br><br>
     * Internally this method will modify the initial options. If you set the "eventContent" at some custom
     * initial options, calling this method will override the respective custom initial option value (in short:
     * calling this method will always win).
     * <br><br>
     * A Content Injection Input. Generated content is inserted inside the inner-most wrapper of the event element.
     * If supplied as a callback function, it is called every time the associated event data changes.
     * <br><br>
     * <b>Note: </b> Please be aware, that there is <b>NO</b> content parsing, escaping, quoting or
     * other security mechanism applied on this string, so check it yourself before passing it to the client.
     * <br><br>
     * <b>Example</b>
     * <pre>
     * calendar.setEntryContentCallback("" +
     * "function(arg) { " +
     * "  let italicEl = document.createElement('i');" +
     * "  if (arg.event.getCustomProperty('isUrgent', false)) {" +
     * "    italicEl.innerHTML = 'urgent event';" +
     * "  } else {" +
     * "    italicEl.innerHTML = 'normal event';" +
     * "  }" +
     * "  let arrayOfDomNodes = [ italicEl ];" +
     * "  return { domNodes: arrayOfDomNodes }" +
     * "}");
     * </pre>
     *
     * @param entryContent function to be attached
     *
     */
    public FullCalendarBuilder withEntryContent(@NotNull String entryContent) {
        return new FullCalendarBuilder(scheduler, entryLimit, autoBrowserTimezone, schedulerLicenseKey, initialOptions, entryProvider, customType, initialEntries, entryContent);
    }

    /**
     * Builds the FullCalendar with the settings of this instance. Depending on some settings the returned
     * instance might be a subclass of {@link FullCalendar}.
     *
     * @return FullCalendar instance
     */
    @SuppressWarnings("unchecked")
    public <T extends FullCalendar> T build() {
        FullCalendar calendar;
        if (scheduler) {
            calendar = createFullCalendarSchedulerInstance(schedulerLicenseKey);
        } else {
            calendar = createFullCalendarBasicInstance();
        }

        if ((initialOptions == null || !initialOptions.hasKey(FullCalendar.Option.TIMEZONE.getOptionKey())) && autoBrowserTimezone) {
            calendar.addBrowserTimezoneObtainedListener(event -> calendar.setTimezone(event.getTimezone()));
        }

        if (entryProvider != null) {
            calendar.setEntryProvider(entryProvider);
        } else if (initialEntries != null) {
            ((EagerInMemoryEntryProvider<Entry>) calendar.getEntryProvider()).addEntries(initialEntries);
        }

        return (T) calendar;
    }

    /**
     * Creates a basic instance.
     *
     * @return instance
     */
    protected FullCalendar createFullCalendarBasicInstance() {
        if (initialOptions != null) {
            extendInitialOptions();

            FullCalendar calendar = null;
            try {
                calendar = customType != null ? customType.getDeclaredConstructor(JsonObject.class).newInstance(initialOptions) : new FullCalendar(initialOptions);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            extendFcWithInitialOptions(calendar);
            return calendar;
        }

        try {
            return customType != null ? customType.getDeclaredConstructor(int.class).newInstance(entryLimit) : new FullCalendar(entryLimit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void extendInitialOptions() {
        if (initialOptions != null) {
            if (entryLimit > 0 && !initialOptions.hasKey("dayMaxEvents")) {
                initialOptions.put("dayMaxEvents", entryLimit);
            }

            if (entryContent != null) {
                initialOptions.put("eventContent", entryContent);
            }
        }
    }

    private void extendFcWithInitialOptions(FullCalendar calendar) {
        if (initialOptions != null) {
            if (!initialOptions.hasKey(FullCalendar.Option.LOCALE.getOptionKey())) {
                calendar.setLocale(CalendarLocale.getDefault());
            }
        }
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
            Class<?> loadClass = customType != null ? customType : getClass().getClassLoader().loadClass("org.vaadin.stefan.fullcalendar.FullCalendarScheduler");

            FullCalendar scheduler;

            if (initialOptions != null) {
                extendInitialOptions();
                scheduler = (FullCalendar) loadClass.getDeclaredConstructor(JsonObject.class).newInstance(this.initialOptions);
            } else {
                scheduler = (FullCalendar) loadClass.getDeclaredConstructor(int.class).newInstance(this.entryLimit);
            }

            if (schedulerLicenseKey != null) { // set the license key, if provided
                scheduler.getClass().getMethod("setSchedulerLicenseKey", String.class).invoke(scheduler, schedulerLicenseKey);
            }

            extendFcWithInitialOptions(scheduler);

            return scheduler;
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
