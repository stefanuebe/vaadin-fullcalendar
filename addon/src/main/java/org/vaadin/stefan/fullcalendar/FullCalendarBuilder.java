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

import com.vaadin.flow.component.UI;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * This class can be used to create FullCalendar instances via a fluent builder api.
 *
 * @deprecated since 7.2.0 — {@link FullCalendar} and {@link FullCalendarScheduler} can be constructed
 * directly and configured through {@link FullCalendar#setOption(FullCalendar.Option, Object)}. The
 * builder no longer provides value beyond syntactic sugar. <b>No removal is scheduled for 7.x</b> —
 * existing code keeps working throughout the 7.x line; the class is only slated for removal in a
 * future major version.
 * <p>
 * Migration:
 * <table>
 *   <caption>Builder call → direct replacement</caption>
 *   <tr><th>Builder call</th><th>Replacement</th></tr>
 *   <tr><td>{@code FullCalendarBuilder.create().build()}</td>
 *       <td>{@code new FullCalendar()}</td></tr>
 *   <tr><td>{@code .withScheduler().build()}</td>
 *       <td>{@code new FullCalendarScheduler()}</td></tr>
 *   <tr><td>{@code .withScheduler(key).build()}</td>
 *       <td>{@code new FullCalendarScheduler()} + {@code setOption(Option.SCHEDULER_LICENSE_KEY, key)}</td></tr>
 *   <tr><td>{@code .withInitialOptions(obj).build()}</td>
 *       <td>{@code new FullCalendar(obj)} / {@code new FullCalendarScheduler(obj)}</td></tr>
 *   <tr><td>{@code .withEntryLimit(n).build()}</td>
 *       <td>{@code new FullCalendar()} + {@code setOption(Option.MAX_ENTRIES_PER_DAY, n)}</td></tr>
 *   <tr><td>{@code .withEntryProvider(p).build()}</td>
 *       <td>{@code new FullCalendar()} + {@code setEntryProvider(p)}</td></tr>
 *   <tr><td>{@code .withInitialEntries(entries).build()}</td>
 *       <td>{@code new FullCalendar()} + {@code ((InMemoryEntryProvider<Entry>) calendar.getEntryProvider()).addEntries(entries)}</td></tr>
 *   <tr><td>{@code .withInitialView(view).build()}</td>
 *       <td>{@code new FullCalendar()} + {@code addAttachListener(e -> { calendar.changeView(view); e.unregisterListener(); })}</td></tr>
 *   <tr><td>{@code .withLocale(l)} / {@code .withTimezone(t)} / {@code .withBusinessHours(h)}</td>
 *       <td>{@code setLocale(l)} / {@code setTimezone(t)} / {@code setBusinessHours(h)} on the calendar</td></tr>
 *   <tr><td>{@code .withDirection(d).build()}</td>
 *       <td>{@code setOption(Option.DIRECTION, d.getClientSideValue())}</td></tr>
 *   <tr><td>{@code .withAutoBrowserTimezone().build()}</td>
 *       <td>{@code addBrowserTimezoneObtainedListener(e -> calendar.setTimezone(e.getTimezone()))}</td></tr>
 *   <tr><td>{@code .withAutoBrowserLocale().build()}</td>
 *       <td>{@code setLocale(UI.getCurrent().getLocale())} (after UI is available)</td></tr>
 *   <tr><td>{@code .withCustomCalendarViews(views).build()}</td>
 *       <td>{@code setCustomCalendarViews(views)}</td></tr>
 *   <tr><td>{@code .withCustomType(Cls.class).build()}</td>
 *       <td>{@code new Cls()} directly</td></tr>
 * </table>
 */
@Deprecated(since = "7.2.0")
public class FullCalendarBuilder {

    private boolean autoBrowserTimezone;
    private boolean autoBrowserLocale;
    private boolean scheduler;
    private int entryLimit;
    private String schedulerLicenseKey;
    private ObjectNode initialOptions;
    private EntryProvider<Entry> entryProvider;
    private Class<? extends FullCalendar> customType;
    private Collection<Entry> initialEntries;
    private String entryContent;
    private CustomCalendarView[] customCalendarViews;

    // New fields for options best set before first render
    private CalendarView initialView;
    private Locale locale;
    private Timezone timezone;
    private BusinessHours[] businessHours;
    private Direction direction;

    private FullCalendarBuilder() {
        this.entryLimit = -1;
    }

    /**
     * Creates a new builder instance with default settings.
     *
     * @return builder instance
     * @deprecated since 7.2.0 — see class-level deprecation note for direct replacements.
     */
    @Deprecated(since = "7.2.0")
    public static FullCalendarBuilder create() {
        return new FullCalendarBuilder();
    }

    /**
     * Allows to define an initial collection of entries to be added to the default entry provider of the new calendar instance. Will be ignored, when
     * a custom entry provider is set.
     *
     * @param initialEntries initial entries
     * @return this instance
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withInitialEntries(Collection<Entry> initialEntries) {
        this.initialEntries = Objects.requireNonNull(initialEntries);
        return this;
    }

    /**
     * Allows to define a subclass of the FullCalendar to be used as the new instance's type. Please make sure, that this
     * class inherits all necessary constructors of the FullCalendar or Scheduler to prevent any exceptions at build time.
     * Also the constructors needs to be public.
     *
     * @param customType custom type to be used
     * @return this instance
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withCustomType(Class<? extends FullCalendar> customType) {
        this.customType = Objects.requireNonNull(customType);
        return this;
    }

    /**
     * Initializes the calendar with the given entry provider. By default it will be created with an instance of
     * {@link org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider}.
     *
     * @param entryProvider entry provider to be used
     * @return this instance
     */
    @SuppressWarnings("unchecked")
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withEntryProvider(EntryProvider<? extends Entry> entryProvider) {
        this.entryProvider = (EntryProvider<Entry>) Objects.requireNonNull(entryProvider);
        return this;
    }

    /**
     * Activates Scheduler support. Sets no license key. When {@link #withScheduler(String)} has been used before,
     * the license key passed there, will be kept internally.
     * <br><br>
     * <b>Note: </b> You need to add the FullCalender Scheduler extension addon to the class path, otherwise
     * the build will fail.
     *
     * @return this instance
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withScheduler() {
        this.scheduler = true;
        return this;
    }

    /**
     * Activates Scheduler support. The given string will be used as scheduler license key.
     * <br><br>
     * <b>Note: </b> You need to add the FullCalender Scheduler extension addon to the class path, otherwise
     * the build will fail.
     *
     * @param licenseKey scheduler license key to be used
     * @return this instance
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withScheduler(String licenseKey) {
        this.scheduler = true;
        this.schedulerLicenseKey = licenseKey;
        return this;
    }

    /**
     * Expects the default limit of entries shown per day. This does not affect basic or
     * list views.
     * <br><br>
     * Passing a negative number disabled the entry limit (same as not using this method at all).
     *
     * @param entryLimit limit
     * @return this instance
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withEntryLimit(int entryLimit) {
        this.entryLimit = entryLimit;
        return this;
    }

    /**
     * Sets automatically the browser timezone as timezone for this instance.
     *
     * @return this instance
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withAutoBrowserTimezone() {
        this.autoBrowserTimezone = true;
        return this;
    }

    /**
     * Sets automatically the browser locale as timezone for this instance. Internally it will use
     * {@link UI#getLocale()} to obtain the value. This might change in future, if necessary.
     *
     * @return this instance
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withAutoBrowserLocale() {
        this.autoBrowserLocale = true;
        return this;
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
     * @return this instance
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs">FullCalendar documentation</a>
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withInitialOptions(ObjectNode initialOptions) {
        this.initialOptions = initialOptions;
        return this;
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
     * calendar.setEntryContentCallback("""
     *     function(arg) {
     *         let italicEl = document.createElement('i');
     *         if (arg.event.getCustomProperty('isUrgent', false)) {
     *             italicEl.innerHTML = 'urgent event';
     *         } else {
     *             italicEl.innerHTML = 'normal event';
     *         }
     *         let arrayOfDomNodes = [ italicEl ];
     *         return { domNodes: arrayOfDomNodes }
     *     }""");
     * </pre>
     *
     * @param entryContent function to be attached
     * @return this instance
     * @deprecated since 7.2.0 — use {@link FullCalendar#setOption(FullCalendar.Option, Object)} with {@link FullCalendar.Option#ENTRY_CONTENT} and {@link JsCallback} after building instead.
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withEntryContent(String entryContent) {
        this.entryContent = entryContent;
        return this;
    }

    /**
     * Registers the given custom calendar views to be available to the server and client side. This is the recommended
     * way to register custom views. You may also use initial options, but in that case, the server side will only
     * store "anonymous" instances.
     * <br><br>
     * If you register a custom view via this method, an anonymous custom view with the same name in the initial options will be ignored.
     * @param customCalendarViews custom calendar views
     * @return this instance
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withCustomCalendarViews(CustomCalendarView... customCalendarViews) {
        this.customCalendarViews = customCalendarViews;
        return this;
    }

    /**
     * Sets the initial view to be shown when the calendar is first rendered.
     *
     * @param view initial calendar view
     * @return this instance
     * @throws NullPointerException when null is passed
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withInitialView(CalendarView view) {
        this.initialView = Objects.requireNonNull(view);
        return this;
    }

    /**
     * Sets the locale to be used by the calendar.
     *
     * @param locale locale
     * @return this instance
     * @throws NullPointerException when null is passed
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withLocale(Locale locale) {
        this.locale = Objects.requireNonNull(locale);
        return this;
    }

    /**
     * Sets the timezone to be used by the calendar.
     *
     * @param timezone timezone
     * @return this instance
     * @throws NullPointerException when null is passed
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withTimezone(Timezone timezone) {
        this.timezone = Objects.requireNonNull(timezone);
        return this;
    }

    /**
     * Sets the business hours for the calendar.
     *
     * @param hours business hours
     * @return this instance
     * @throws NullPointerException when null is passed
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withBusinessHours(BusinessHours... hours) {
        this.businessHours = Objects.requireNonNull(hours);
        return this;
    }

    /**
     * Sets the text direction for the calendar.
     *
     * @param direction text direction
     * @return this instance
     * @throws NullPointerException when null is passed
     */
    @Deprecated(since = "7.2.0")
    public FullCalendarBuilder withDirection(Direction direction) {
        this.direction = Objects.requireNonNull(direction);
        return this;
    }

    /**
     * Builds the FullCalendar with the settings of this instance. Depending on some settings the returned
     * instance might be a subclass of {@link FullCalendar}.
     *
     * @return FullCalendar instance
     */
    @SuppressWarnings("unchecked")
    @Deprecated(since = "7.2.0")
    public <T extends FullCalendar> T build() {
        FullCalendar calendar;
        if (scheduler) {
            calendar = createFullCalendarSchedulerInstance(schedulerLicenseKey);
        } else {
            calendar = createFullCalendarBasicInstance();
        }

        if(customCalendarViews != null && customCalendarViews.length > 0) {
            calendar.setCustomCalendarViews(customCalendarViews);
        }

        if (entryLimit > 0) {
            calendar.setMaxEntriesPerDay(entryLimit);
        }

        if (autoBrowserTimezone) {
            calendar.addBrowserTimezoneObtainedListener(event -> calendar.setTimezone(event.getTimezone()));
        }

        if (autoBrowserLocale) {
            calendar.setLocale(UI.getCurrent().getLocale());
        }

        if (StringUtils.isNotBlank(entryContent)) {
            calendar.setEntryContentCallback(entryContent);
        }

        if (entryProvider != null) {
            calendar.setEntryProvider(entryProvider);
        } else if (initialEntries != null) {
            ((InMemoryEntryProvider<Entry>) calendar.getEntryProvider()).addEntries(initialEntries);
        }

        if (initialView != null) {
            // initial view is not working currently, no idea why - the initial rendering is broken
            // therefore we workaround this with a one time attachlistener for initial view.
            // this way we also know on the server side which view we have

//            calendar.setOption("initialView", initialView.getClientSideValue());
            calendar.addAttachListener(event -> {
                calendar.changeView(initialView);
                event.unregisterListener();
            });
        }
        if (locale != null) {
            calendar.setLocale(locale);
        }
        if (timezone != null) {
            calendar.setTimezone(timezone);
        }
        if (businessHours != null) {
            calendar.setBusinessHours(businessHours);
        }
        if (direction != null) {
            calendar.setOption(FullCalendar.Option.DIRECTION, direction.getClientSideValue());
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
            FullCalendar calendar;
            try {
                calendar = customType != null ? customType.getDeclaredConstructor(ObjectNode.class).newInstance(initialOptions) : new FullCalendar(initialOptions);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return calendar;
        }

        try {
            return customType != null ? customType.getDeclaredConstructor().newInstance() : new FullCalendar();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
                scheduler = (FullCalendar) loadClass.getDeclaredConstructor(ObjectNode.class).newInstance(this.initialOptions);
            } else {
                scheduler = (FullCalendar) loadClass.getDeclaredConstructor().newInstance();
            }

            if (schedulerLicenseKey != null) { // set the license key, if provided
                scheduler.getClass().getMethod("setSchedulerLicenseKey", String.class).invoke(scheduler, schedulerLicenseKey);
            }

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
