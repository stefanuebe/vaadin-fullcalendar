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

import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.DisplayMode;
import org.vaadin.stefan.fullcalendar.option.ExtendedLocalTime;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.stefan.fullcalendar.converters.*;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonIgnore;
import org.vaadin.stefan.fullcalendar.json.JsonName;
import org.vaadin.stefan.fullcalendar.json.JsonReadField;
import org.vaadin.stefan.fullcalendar.json.JsonUpdateAllowed;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * A calendar item shown by {@link FullCalendar}, called "event" in the FullCalendar client library.
 * <p>
 * An entry has a title, a start and an end, and can be all-day. Start and end are stored as UTC
 * ({@link #setStart(LocalDateTime)}). The calendar's {@link Option#TIMEZONE} only changes how they are shown.
 * An entry can repeat, either with the simple recurrence properties ({@link #setRecurringDaysOfWeek(Set)},
 * {@link #setRecurringStartTime(LocalTime)}, ...) or with an {@link RRule} ({@link #setRRule(RRule)}).
 * <p>
 * Color, class names, editability and display mode can be set per entry and override the calendar's options.
 * Values of your own, such as an ID from your backend, can be attached with {@link #setExtendedProp(String, Object)}.
 * <p>
 * The calendar gets its entries from its {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider}.
 * After changing an entry that is already shown, call
 * {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider#refreshItem(Entry)} to send the change to the
 * client:
 * <pre>{@code
 * Entry entry = new Entry();
 * entry.setTitle("Meeting");
 * entry.setStart(LocalDateTime.of(2025, 3, 3, 9, 0));
 * entry.setEnd(LocalDateTime.of(2025, 3, 3, 10, 0));
 *
 * InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
 * provider.addEntry(entry);
 * calendar.setEntryProvider(provider);
 *
 * entry.setTitle("Team meeting");
 * provider.refreshItem(entry);
 * }</pre>
 * Entries are equal when their ids are equal. The id must be unique within a calendar. Without an id given to the
 * constructor, the entry generates one. Use {@code ResourceEntry} of the scheduler add-on to assign entries to
 * resources.
 */
@Getter
@lombok.Setter // prevent conflicts with Vaadin Setter
@EqualsAndHashCode(of = "id")
@FieldNameConstants
public class Entry implements Serializable {

    private static final Set<BeanProperties<Entry>> PROPERTIES = BeanProperties.read(Entry.class);

    private final String id;
    private String groupId;
    private String title;

    @JsonUpdateAllowed
    @JsonConverter(LocalDateTimeConverter.class)
    private LocalDateTime start;

    @JsonUpdateAllowed
    @JsonConverter(LocalDateTimeConverter.class)
    private LocalDateTime end;

    @JsonUpdateAllowed
    private boolean allDay;

    /**
     * Whether this entry is editable (draggable + resizable). Internal storage is
     * nullable: {@code null} means "not explicitly set — inherit the calendar-level
     * {@code editable} option". {@code true}/{@code false} are explicit overrides.
     * <p>
     * The public API ({@link #isEditable()}) keeps its pre-7.2 semantics — treating
     * null as {@code true} — so the default behaviour ("entries are editable by default
     * when the calendar is editable") is preserved. The difference is that {@code null}
     * is now skipped during JSON serialization, so a calendar-level {@code editable: false}
     * is no longer overridden by every entry's per-entry {@code editable: true} default.
     * See issue #212.
     */
    @Getter(AccessLevel.NONE)
    @lombok.Setter(AccessLevel.NONE)
    @JsonReadField
    private Boolean editable;
    private Boolean startEditable;
    private Boolean durationEditable;
    private String color;
    @Getter(AccessLevel.NONE)
    @lombok.Setter(AccessLevel.NONE)
    private Object constraint;
    private String contrastColor;
    private Boolean overlap;

    /** Whether the entry is keyboard-focusable (tabbable) independently of drag/drop. Null inherits the calendar-level {@code eventInteractive} option. */
    private Boolean interactive;

    /**
     * A URL that FullCalendar navigates to when the event is clicked. Null means no URL navigation.
     * <p>
     * If a server-side click listener is also registered, FullCalendar will navigate to the url immediately on click
     * and the listener will still fire — but the page may already be unloading. Use one mechanism or the other, not both.
     */
    private String url;

    /**
     * How the calendar renders this entry. Null means not set, then the calendar's
     * {@link FullCalendarOptions#setEntryDisplay(DisplayMode) entry display} applies, which is {@link DisplayMode#AUTO}
     * by default. An entry set to {@link DisplayMode#AUTO} keeps it, also when the calendar sets another display.
     */
    @JsonName("display")
    @JsonConverter(DisplayModeConverter.class)
    private DisplayMode displayMode;

    @JsonName("startRecur")
    @JsonConverter(LocalDateConverter.class)
    private LocalDate recurringStartDate;

    @JsonName("endRecur")
    @JsonConverter(LocalDateConverter.class)
    private LocalDate recurringEndDate;

    @JsonName("startTime")
    @JsonConverter(ExtendedLocalTimeConverter.class)
    private ExtendedLocalTime recurringStartTime; // see #139

    @JsonName("endTime")
    @JsonConverter(ExtendedLocalTimeConverter.class)
    private ExtendedLocalTime recurringEndTime; // see #139

    @JsonName("daysOfWeek")
    @JsonConverter(DayOfWeekItemConverter.class)
    private Set<DayOfWeek> recurringDaysOfWeek;

    /**
     * The duration of each occurrence of a recurring entry, e.g. {@code Duration.ofHours(48)} for an occurrence
     * spanning two days. Applies to both the built-in recurrence and an {@link RRule}, so neither of them clears it.
     * Without it, an all-day occurrence spans one day and a timed occurrence uses the recurring start and end time.
     * <p>
     * Sent to the client as {@code hh:mm:ss}, where the hours may exceed 24. The client adds it in wall-clock time, so an
     * occurrence ends at the same wall-clock time across a daylight saving change.
     *
     * @see <a href="https://fullcalendar.io/docs/recurring-events">recurring events</a>
     */
    @JsonName("duration")
    @JsonConverter(DurationConverter.class)
    private Duration recurringDuration;

    /**
     * RRule-based recurrence definition. Requires the {@code @fullcalendar/rrule} plugin.
     * <p>
     * <b>Mutually exclusive</b> with the built-in recurrence ({@code recurringDaysOfWeek}, etc.), so setting one
     * removes the other. See {@link #setRRule(RRule)}.
     * <p>
     * Use {@link #setRRule(RRule)} and {@link #getRRule()} instead of the Lombok-generated accessors.
     *
     * @see RRule
     */
    @Getter(AccessLevel.NONE)
    @lombok.Setter(AccessLevel.NONE)
    @JsonReadField
    @JsonConverter(RRuleConverter.class)
    private RRule rrule;

    /**
     * Dates to exclude from an RRule-based recurrence. Populated automatically from
     * {@link RRule#getExcludedDates()} when {@link #setRRule(RRule)} is called.
     * Not part of the public API — use {@link RRule#excludeDates(LocalDate...)} instead.
     */
    @Getter(AccessLevel.NONE)
    @lombok.Setter(AccessLevel.NONE)
    @JsonReadField
    @JsonConverter(ExdateConverter.class)
    private List<LocalDate> exdate;

    /**
     * RRules defining exclusion patterns for an RRule-based recurrence. Populated automatically from
     * {@link RRule#getExcludedRules()} when {@link #setRRule(RRule)} is called.
     * Not part of the public API — use {@link RRule#excludeRules(RRule...)} instead.
     */
    @Getter(AccessLevel.NONE)
    @lombok.Setter(AccessLevel.NONE)
    @JsonReadField
    @JsonConverter(ExruleConverter.class)
    private List<RRule> exrule;

    /**
     * Returns the RRule-based recurrence definition, or {@code null} if not set.
     *
     * @return the RRule, or {@code null}
     */
    public RRule getRRule() {
        return rrule;
    }

    /**
     * Sets the RRule-based recurrence definition. If the RRule has excluded dates
     * (set via {@link RRule#excludeDates(LocalDate...)}), they are automatically transferred
     * to the entry's {@code exdate} property and serialized separately as required by
     * FullCalendar's RRule plugin.
     * <p>
     * Pass {@code null} to remove the recurrence rule and any associated excluded dates and rules.
     * <p>
     * A non-null RRule removes the built-in recurrence (days of week, recurring start and end date, recurring
     * start and end time), because the two must not be combined. The {@link #getRecurringDuration() recurring
     * duration} is kept, as it applies to both.
     *
     * @param rrule the RRule to set, or {@code null} to clear
     */
    public void setRRule(RRule rrule) {
        if (rrule != null) {
            // fields, not setters: the built-in setters call setRRule(null)
            recurringDaysOfWeek = null;
            recurringStartDate = null;
            recurringEndDate = null;
            recurringStartTime = null;
            recurringEndTime = null;
        }
        this.rrule = rrule;
        this.exdate = rrule != null ? rrule.getExcludedDates() : null;
        this.exrule = rrule != null ? rrule.getExcludedRules() : null;
    }

    @JsonName("className")
    @JsonConverter(ClassNameConverter.class)
    private Set<String> classNames;

    // null until extended props are set, most entries have none
    @JsonConverter(ExtendedPropsConverter.class)
    private Map<String, Object> extendedProps;

    @JsonIgnore
    private boolean knownToTheClient; // not sure if still needed?

    /**
     * The referenced calendar instance. Can be null.
     */
    @JsonIgnore
    private FullCalendar calendar;

    /**
     * Creates a new editable instance with a generated id.
     */
    public Entry() {
        this(null);
    }

    /**
     * Creates a new entry with the given id. Null will lead to a generated id.
     * <br><br>
     * Please be aware, that the ID needs to be unique in the calendar instance. Otherwise it can lead to
     * unpredictable results.
     *
     * @param id id
     */
    public Entry(String id) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
    }

    /**
     * Returns the calendar instance of this entry. Is empty when not yet added to a calendar.
     *
     * @return calendar instance
     */
    public Optional<FullCalendar> getCalendar() {
        return Optional.ofNullable(calendar);
    }

    public void setCalendar(FullCalendar calendar) {
        if (this.calendar != null && calendar != null && this.calendar != calendar) {
            throw new UnsupportedOperationException("This entry is already attached to a calendar instance. Please remove it first from the old one.");
        }
        this.calendar = calendar;
    }

    /**
     * Converts the given instance to a json object representing its inner state. This json is intended to be
     * transported to and interpreted by the client and thus should not be modified manually, except for concrete
     * reasons.
     * @return json
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ObjectNode toJson() {
        // The toJson is implemented in a dynamic fashion to not need to extend it every time a
        // new property comes out.

        ObjectNode json = JsonFactory.createObject();

        streamProperties().forEach(def -> {
            // Use cached annotation data from BeanProperties for performance
            if (!def.isJsonIgnored()) {
                Object value = def.getGetter().apply(this);

                JsonNode jsonValue;

                JsonItemPropertyConverter converter = def.getConverter();

                if (converter != null && converter.supports(value)) {
                    jsonValue = converter.toClientModel(value, this);
                } else {
                    jsonValue = JsonUtils.toJsonNode(value);
                }

                if (jsonValue != null && !(jsonValue instanceof NullNode)) {
                    json.set(def.getJsonName(), jsonValue);
                }
            }
        });

        return json;
    }

    /**
     * Updates this instance with the given json object. Only fields, that are updateable will be overwritten.
     * Throws an exception, when the given json object has not the same id as this instance.
     * @param jsonObject json object
     */
    public void updateFromJson(ObjectNode jsonObject) {
        updateFromJson(jsonObject, true);
    }

    /**
     * Updates this instance with the given json object. Only fields, that are updateable will be overwritten.
     * Based on the boolean parameter, the id will be either ignored (false) or has to be the same as this instance's one
     * (true), otherwise an exception will be thrown
     *
     * @param jsonObject json object
     * @param requiresMatchingId require the ids to match
     */
    public void updateFromJson(ObjectNode jsonObject, boolean requiresMatchingId) {
        checkForMatchingId(jsonObject, requiresMatchingId);

        // Use cached annotation data from BeanProperties for performance
        streamProperties()
                .filter(def -> !def.isJsonIgnored())
                .filter(BeanProperties::isJsonUpdateAllowed)
                .forEach(def -> writeJsonValueToEntry(jsonObject, def));
    }

    /**
     * Updates this instance with the given json object. ALL fields will be overwritten, regardless whether they are allowed to
     * be updated or not. Ignored properties still will be not overwritten as they are intended to be invisible for the json parser.
     * Based on the boolean parameter, the id will be either ignored (false) or has to be the same as this instance's one
     * (true), otherwise an exception will be thrown
     *
     * @param jsonObject json object
     * @param requiresMatchingId require the ids to match
     */
    public void updateAllFromJson(ObjectNode jsonObject, boolean requiresMatchingId) {
        checkForMatchingId(jsonObject, requiresMatchingId);

        // Use cached annotation data from BeanProperties for performance
        streamProperties()
                .filter(def -> !def.isJsonIgnored())
                .filter(def -> def.getSetter().isPresent())
                .forEach(def -> writeJsonValueToEntry(jsonObject, def));
    }

    private void checkForMatchingId(ObjectNode jsonObject, boolean requiresMatchingId) {
        if (requiresMatchingId) {
            if (!jsonObject.hasNonNull(Fields.ID)) {
                throw new IllegalArgumentException("JsonObject has no id. Id matching is required.");
            }

            String idString = jsonObject.get(Fields.ID).asString();
            if (!id.equals(idString)) {
                throw new IllegalArgumentException("Id matching is required. This id is " + id + " but the json object provided " + idString);
            }
        }
    }

    private void writeJsonValueToEntry(ObjectNode jsonObject, BeanProperties<Entry> def) {
        String name = def.getName();

        Setter<Entry, Object> setter = def.getSetter()
                .orElseThrow(() -> new UnsupportedOperationException("No setter found for field " + name));

        String jsonName = def.getJsonName();

        if (jsonObject.hasNonNull(jsonName)) {
            JsonItemPropertyConverter<Object, Object> converter = def.getConverter();

            Object newValue;
            JsonNode jsonValue = jsonObject.get(jsonName);

            if (converter != null) {
                newValue = converter.toServerModel(jsonValue, this);
            } else {
                newValue = JsonUtils.ofJsonNode(jsonValue);
            }

            setter.accept(this, newValue);
        }
    }

    /**
     * Checks whether the given json object is a valid source to update this instance.
     *
     * @param jsonObject json object to check
     * @return is a valid source
     */
    protected boolean isValidJsonSource(ObjectNode jsonObject) {
        return jsonObject.hasNonNull(Fields.ID) && Objects.equals(jsonObject.get(Fields.ID).asString(), getId());
    }

    /**
     * Creates a copy of this instance. Collection, Map and Array values are copied (but their values are taken
     * as they are, so no deep copy).
     *
     * @param <T> Type of copy
     * @return copy
     */
    public <T extends Entry> T copy() {
        try {
            T copy = (T) getClass().getConstructor(String.class).newInstance(getId());
            copy(this, copy, false);

            return copy;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create copy of entry", e);
        }
    }

    /**
     * Creates a copy of this instance. Collection, Map and Array values are copied (but their values are taken
     * as they are, so no deep copy).
     * <p></p>
     * Please make sure, that both types are compatible in their properties. Any property, that exists in the source
     * but not the target type might lead to an exception. Also the target type must have a public constructor, taking
     * the id as the parameter.
     *
     * @param <T> Type of copy
     * @return copy
     */
    public <T extends Entry> T copyAsType(Class<T> targetType) {
        try {
            T copy = (T) targetType.getConstructor(String.class).newInstance(getId());
            copy(this, copy, true);

            return copy;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create copy of entry as type " + targetType.getName(), e);
        }
    }

    public void copyFrom(Entry source) {
        copyFrom(source, false);
    }

    public void copyFrom(Entry source, boolean ignoreId) {
        if (!ignoreId && !source.getId().equals(getId())) {
            throw new IllegalArgumentException("IDs must match");
        }
        copy(source, this, false);
    }

    /**
     * Copies all values from source to target (except for the id). When the boolean is set to false,
     * both objects have to be of the <b>same</b> type. Otherwise it is up to the developer to guarantee,
     * that the target can take all properties of the source.<br>
     * Properties without a setter are ignored.<br>
     * The {@link RRule} instance is taken over as it is, together with its excluded dates and rules.
     * @param source source
     * @param target target
     * @param ignoreTypeDifference difference
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void copy(Entry source, Entry target, boolean ignoreTypeDifference) {
        if (!ignoreTypeDifference && !source.getClass().equals(target.getClass())) {
            throw new IllegalArgumentException("Both parameters must be of the SAME class.");
        }

        source.streamProperties().forEach(def -> {
            ValueProvider getter = def.getGetter();
            def.getSetter().ifPresent(setter -> {
                try {
                    Object value = getter.apply(source);

                    if (value instanceof Collection) {
                        // fails on unmodifiable
                        Collection collection = newInstance((Collection) value);
                        collection.addAll((Collection) value);
                        value = collection;
                    } else if (value instanceof Map) {
                        // fails on unmodifiable
                        Map map = newInstance((Map) value);
                        map.putAll((Map) value);
                        value = map;
                    } else if (value instanceof Object[]) {
                        value = ((Object[]) value).clone();
                    }

                    setter.accept(target, value);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Property " + def.getName() + " threw an exception during copy", e);
                }
            });
        });

        // The loop finds no setter for rrule, because it is named setRRule. That setter also fills exdate and exrule.
        target.setRRule(source.getRRule());
    }

    @SuppressWarnings("unchecked")
    private static <T> T newInstance(T value) throws ReflectiveOperationException {
        try {
            return (T) value.getClass().getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            if (value instanceof Set) {
                return (T) new LinkedHashSet<>();
            }

            if (value instanceof Collection) {
                return (T) new ArrayList<>();
            }

            if (value instanceof Map) {
                return (T) new HashMap<>();
            }

            throw new IllegalArgumentException("Type " + value.getClass() + " has no public no-args constructor and no fallback.");
        }
    }

    protected Stream<BeanProperties<Entry>> streamProperties() {
        return PROPERTIES.stream();
    }

    /**
     * Streams all properties, that are updateable. A property counts as updateable, if it is not annotated with
     * {@link JsonIgnore}, it is annotated with {@link JsonUpdateAllowed} and has a setter.
     * @return updateable properties
     */
    protected Stream<BeanProperties<Entry>> streamUpdateableProperties() {
        return streamProperties().filter(def ->
                !def.isJsonIgnored()
                        && def.isJsonUpdateAllowed()
                        && def.getSetter().isPresent()
        );
    }

    /**
     * Converts this instance to a json object, that only contains the id. This still represents
     * this item but without any data.
     *
     * @return json object representing this instance
     */
    public ObjectNode toJsonWithIdOnly() {
        ObjectNode jsonObject = JsonFactory.createObject();
        jsonObject.put(Fields.ID, getId());
        return jsonObject;
    }

    /**
     * Returns the entry's start as an {@link Instant}. The contained time is the same as when calling
     * {@link #getStart()}.
     *
     * @return start as Instant or null
     */
    public Instant getStartAsInstant() {
        return convertNullable(getStart(), (LocalDateTime start) -> start.toInstant(ZoneOffset.UTC));
    }

    /**
     * Returns the entry's start date.
     * @return start date or null
     */
    public LocalDate getStartAsLocalDate() {
        return convertNullable(getStart(), LocalDateTime::toLocalDate);
    }

    /**
     * Returns the start time as a zoned date time using this entry's start time zone. By default this is
     * the calendar's timezone or, if no calendar is set yet, UTC.
     * <p></p>
     * Calling {@link ZonedDateTime#toLocalDateTime()} returns the time including the offset as LocalDateTime.
     * @return start at current timezone or null
     */
    public ZonedDateTime getStartWithTimezone() {
        return getStartTimezone().applyTimezone(getStart());
    }

    /**
     * Returns the start time as a local date time after applying the timezone's offset to
     * the utc based start date ({@link #getStart()}). By default the timezone is
     * the calendar's timezone or, if no calendar is set yet, UTC.
     * <p></p>
     * To get a {@link OffsetDateTime} please use {@link #getStartWithTimezone()} and call
     * {@link ZonedDateTime#toOffsetDateTime()}
     * @return start with offset or null
     */
    public LocalDateTime getStartWithOffset() {
        return getStartWithOffset(getStartTimezone());
    }

    /**
     * Returns the start time as a local date time after applying the timezone's offset to
     * the utc based start date ({@link #getStart()}).
     * <p></p>
     * This method is intended to be used for new entries that have not yet been added to the
     * calendar and thus have no reference to its timezone.
     * <p></p>
     * To get a {@link OffsetDateTime} please use {@link #getStartWithTimezone()} and call
     * {@link ZonedDateTime#toOffsetDateTime()}
     * @return start with offset or null
     */
    public LocalDateTime getStartWithOffset(Timezone timezone) {
        return timezone.applyTimezoneOffset(getStart());
    }

    /**
     * Sets the entry's start. The given date time will be interpreted as the UTC start time of this entry.
     *
     * @param start utc start
     */
    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    /**
     * Sets the entry's start. The given instant will be interpreted as the UTC start time of this entry.
     *
     * @param start utc start
     */
    public void setStart(Instant start) {
        setStart(start != null ? LocalDateTime.ofInstant(start, Timezone.ZONE_ID_UTC) : null);
    }

    /**
     * Sets the given local date as start using the start of the day as time (utc based).
     *
     * @param start start at 00:00 utc time
     */
    public void setStart(LocalDate start) {
        setStart(start != null ? start.atStartOfDay() : null);
    }

    /**
     * Sets the entry's start based on the zoned date time instance. The given date time will be converted to UTC.
     * <p></p>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting start time will be ...T00:00.
     * <p></p>
     * @param startWithTimezone start with time zone
     */
    public void setStartWithTimezone(ZonedDateTime startWithTimezone) {
        setStart(startWithTimezone != null ? startWithTimezone.withZoneSameInstant(Timezone.ZONE_ID_UTC).toLocalDateTime() : null);
    }

    /**
     * Sets the entry's start. The given date time will be interpreted as having the offset of the
     * start time zone applied. The time will be converted to UTC.
     * <p></p>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting start time will be ...T00:00.
     * <p></p>
     * This method is intended to be used in cases where the start time is edited in relation to
     * the current time zone (like a calendar entry editor).
     * <p></p>
     *
     * @param startWithTimezone start with time zone
     */
    public void setStartWithOffset(LocalDateTime startWithTimezone) {
        setStartWithOffset(startWithTimezone, getStartTimezone());
    }

    /**
     * Sets the entry's start. The given date time will be interpreted as having the offset of the
     * given time zone applied. The time will be converted to UTC.
     * <p></p>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting start time will be ...T00:00.
     * <p></p>
     * This method is intended to be used in cases where entry is not yet added to the calendar and thus
     * cannot use its timezone to interpret the offset.
     * <p></p>
     *
     * @param startWithTimezone start with time zone
     * @param timezone timezone
     */
    public void setStartWithOffset(LocalDateTime startWithTimezone, Timezone timezone) {
        setStart(timezone.removeTimezoneOffset(startWithTimezone));
    }

    /**
     * Clears the current start time. Convenience method to prevent unnecessary casting when using
     * setStart(null).
     */
    public void clearStart() {
        setStart((LocalDateTime) null);
    }

    /**
     * Returns the entry's end as an {@link Instant}. The contained time is the same as when calling
     * {@link #getEnd()}.
     *
     * @return end as Instant or null
     */
    public Instant getEndAsInstant() {
        return convertNullable(getEnd(), (LocalDateTime end) -> end.toInstant(ZoneOffset.UTC));
    }

    /**
     * Returns the entry's end date.
     * @return end date or null
     */
    public LocalDate getEndAsLocalDate() {
        return convertNullable(getEnd(), LocalDateTime::toLocalDate);
    }

    /**
     * Returns the end time as a zoned date time using this entry's end time zone. By default this is
     * the calendar's timezone or, if no calendar is set yet, UTC.
     * <p></p>
     * Calling {@link ZonedDateTime#toLocalDateTime()} returns the time including the offset as LocalDateTime.
     * @return end at current timezone or null
     */
    public ZonedDateTime getEndWithTimezone() {
        return getEndTimezone().applyTimezone(getEnd());
    }

    /**
     * Returns the end time as a local date time after applying the timezone's offset to
     * the utc based end date ({@link #getEnd()}). By default the timezone is
     * the calendar's timezone or, if no calendar is set yet, UTC.
     * <p></p>
     * To get a {@link OffsetDateTime} please use {@link #getEndWithTimezone()} and call
     * {@link ZonedDateTime#toOffsetDateTime()}
     * @return end with offset or null
     */
    public LocalDateTime getEndWithOffset() {
        return getEndTimezone().applyTimezoneOffset(getEnd());
    }

    /**
     * Returns the end time as a local date time after applying the timezone's offset to
     * the utc based end date ({@link #getEnd()}).
     * <p></p>
     * This method is intended to be used for new entries that have not yet been added to the
     * calendar and thus have no reference to its timezone.
     * <p></p>
     * To get a {@link OffsetDateTime} please use {@link #getEndWithTimezone()} and call
     * {@link ZonedDateTime#toOffsetDateTime()}
     * @return end with offset or null
     */
    public LocalDateTime getEndWithOffset(Timezone timezone) {
        return timezone.applyTimezoneOffset(getEnd());
    }

    /**
     * Sets the entry's end. The given date time will be interpreted as the UTC end time of this entry.
     *
     * @param end utc end
     */
    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    /**
     * Sets the entry's end. The given instant will be interpreted as the UTC end time of this entry.
     *
     * @param end utc end
     */
    public void setEnd(Instant end) {
        setEnd(end != null ? LocalDateTime.ofInstant(end, Timezone.ZONE_ID_UTC) : null);
    }

    /**
     * Sets the given local date as end using the end of the day as time (utc based).
     *
     * @param end end at 00:00 utc time
     */
    public void setEnd(LocalDate end) {
        setEnd(end != null ? end.atStartOfDay() : null);
    }

    /**
     * Sets the entry's end based on the zoned date time instance. The given date time will be converted to UTC.
     * <p></p>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting end time will be ...T00:00.
     * <p></p>
     * @param endWithTimezone end with time zone
     */
    public void setEndWithTimezone(ZonedDateTime endWithTimezone) {
        setEnd(endWithTimezone != null ? endWithTimezone.withZoneSameInstant(Timezone.ZONE_ID_UTC).toLocalDateTime() : null);
    }

    /**
     * Sets the entry's end. The given date time will be interpreted as having the offset of the
     * end time zone applied. The time will be converted to UTC.
     * <p></p>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting end time will be ...T00:00.
     * <p></p>
     * This method is intended to be used in cases where the end time is edited in relation to
     * the current time zone (like a calendar entry editor).
     * <p></p>
     *
     * @param endWithTimezone end with time zone
     */
    public void setEndWithOffset(LocalDateTime endWithTimezone) {
        setEnd(getEndTimezone().removeTimezoneOffset(endWithTimezone));
    }

    /**
     * Sets the entry's end. The given date time will be interpreted as having the offset of the
     * given time zone applied. The time will be converted to UTC.
     * <p></p>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting end time will be ...T00:00.
     * <p></p>
     * This method is intended to be used in cases where entry is not yet added to the calendar and thus
     * cannot use its timezone to interpret the offset.
     * <p></p>
     *
     * @param endWithTimezone end with time zone
     * @param timezone timezone
     */
    public void setEndWithOffset(LocalDateTime endWithTimezone, Timezone timezone) {
        setEnd(timezone.removeTimezoneOffset(endWithTimezone));
    }

    /**
     * Clears the current end time. Convenience method to prevent unnecessary casting when using
     * setEnd(null).
     */
    public void clearEnd() {
        setEnd((LocalDateTime) null);
    }

    /**
     * Moves the entry by the given delta. Negative deltas will result in moving the entry to the past.
     * @param delta delta to be applied
     */
    public void moveStartEnd(Delta delta) {
        moveStart(delta);
        moveEnd(delta);
    }

    /**
     * Moves the entry's start by the given delta without modifying the end.
     * Negative deltas will result in moving the start to the past.
     * @param delta delta to be applied to the entry' start
     */
    public void moveStart(Delta delta) {
        setStart(delta.applyOn(getStart()));
    }

    /**
     * Moves the entry's end by the given delta without modifying the start.
     * Negative deltas will result in moving the end to the past.
     * @param delta delta to be applied to the entry' end
     */
    public void moveEnd(Delta delta) {
        setEnd(delta.applyOn(getEnd()));
    }

    /**
     * Returns the time zone the offset helpers convert the internal UTC timestamp with: the calendar's time zone, see
     * {@link FullCalendarOptions#getTimeZone()}. UTC while the entry belongs to no calendar.
     *
     * @return timezone
     */
    public Timezone getStartTimezone() {
        return calendar != null ? calendar.getTimezoneForOffsets() : Timezone.UTC;
    }

    /**
     * Returns the time zone the offset helpers convert the internal UTC timestamp with: the calendar's time zone, see
     * {@link FullCalendarOptions#getTimeZone()}. UTC while the entry belongs to no calendar.
     *
     * @return timezone
     */
    public Timezone getEndTimezone() {
        return calendar != null ? calendar.getTimezoneForOffsets() : Timezone.UTC;
    }

    /**
     * Returns the set of class names or creates a new, empty one, if none exists yet. The returned set is
     * the same as used internally, therefore any changes to it will be reflected to the client side on the
     * next refresh.
     *
     * @return class names set
     * @see #getClassNames()
     */
    public Set<String> getOrCreateClassNames() {
        Set<String> classNames = getClassNames();
        if (classNames == null) {
            classNames = new LinkedHashSet<>();
            setClassNames(classNames);
        }

        return classNames;
    }

    /**
     * Adds css class names to this entry. Duplicates will automatically be filtered out.
     *
     * @param classNames class names to add
     * @throws NullPointerException when null is passed
     */
    public void addClassNames(String... classNames) {
        addClassNames(Arrays.asList(classNames));
    }

    /**
     * Adds css class names to this entry. Duplicates will automatically be filtered out.
     *
     * @param classNames class names to add
     * @throws NullPointerException when null is passed
     */
    public void addClassNames(Collection<String> classNames) {
        Objects.requireNonNull(classNames);
        getOrCreateClassNames().addAll(classNames);
    }

    /**
     * Removes the given classNames from this entry.
     *
     * @param classNames class names to remove
     * @throws NullPointerException when null is passed
     */
    public void removeClassNames(String... classNames) {
        removeClassNames(Arrays.asList(classNames));
    }

    /**
     * Removes the given classNames from this entry.
     *
     * @param classNamesToRemove class names to remove
     * @throws NullPointerException when null is passed
     */
    public void removeClassNames(Collection<String> classNamesToRemove) {
        Set<String> classNames = getClassNames();
        if (classNames != null) {
            classNames.removeAll(classNamesToRemove);
        }
    }

    /**
     * Removes the class names from this entry. Copies of the internal class name set will be unaffected;
     */
    public void removeClassNames() {
        setClassNames(null);
    }

    /**
     * Returns the amount of assigned classNames.
     *
     * @return int size of classNames
     */
    public int getClassNamesSize() {
        Set<String> classNames = getClassNames();
        return classNames != null ? classNames.size() : 0;
    }

    /**
     * Returns, if the entry has any class name assigned.
     *
     * @return Boolean hasClassNames
     */
    public boolean hasClassNames() {
        Set<String> classNames = getClassNames();
        return classNames != null && !classNames.isEmpty();
    }

    /**
     * Same as {@link #getOverlap()}.
     *
     * @return is overlap allowed, or null if not set (inherit from calendar-level setting)
     */
    public Boolean isOverlapAllowed() {
        return getOverlap();
    }

    /**
     * Same as {@link #setOverlap(Boolean)}
     *
     * @param overlap overlapping is allowed, or null to inherit from the calendar-level setting
     */
    public void setOverlapAllowed(Boolean overlap) {
        setOverlap(overlap);
    }

    /**
     * Checks whether this entry is editable (draggable + resizable). Returns {@code true}
     * by default — if {@code editable} has not been explicitly set, the entry inherits the
     * calendar-level {@code editable} option at render time.
     * <p>
     * Semantically equivalent to the pre-7.2 getter. Internally, the field is now nullable
     * so that {@code null} can be skipped during JSON serialization, which is what makes
     * a calendar-level {@code editable: false} actually take effect (see issue #212).
     *
     * @return {@code true} if the entry is editable or the editable state has not been
     *         explicitly set; {@code false} if explicitly set to {@code false}.
     */
    public boolean isEditable() {
        return editable == null || editable;
    }

    /**
     * Sets whether this entry is editable (draggable + resizable). The value is an explicit
     * override of the calendar-level {@code editable} option. Pass {@code null} to clear
     * the override and inherit the calendar-level setting instead.
     * <p>
     * Boolean parameter is used so that {@link org.vaadin.stefan.fullcalendar.json.BeanProperties}
     * can discover the setter (it looks for the boxed type matching the field), so that
     * {@code Entry.copy()} and similar framework paths correctly propagate the value. Primitive
     * {@code boolean} callers auto-box and continue to work as before.
     *
     * @param editable {@code true} to make this entry editable, {@code false} to make it read-only,
     *                 or {@code null} to clear the override and inherit the calendar-level setting
     */
    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    /**
     * Checks, if the duration of this entry is editable. An explicitly set {@code durationEditable} wins.
     * Otherwise the entry falls back to {@link #isEditable()}.
     * @return is duration editable
     */
    public boolean isDurationEditable() {
        return durationEditable != null ? durationEditable : isEditable();
    }

    /**
     * Checks, if the start of this entry is editable. An explicitly set {@code startEditable} wins.
     * Otherwise the entry falls back to {@link #isEditable()}.
     * @return is start editable
     */
    public boolean isStartEditable() {
        return startEditable != null ? startEditable : isEditable();
    }

    /**
     * Returns the current constraint value. This may be a {@code String} (groupId or {@code "businessHours"})
     * or a {@link BusinessHours} JSON representation ({@code ObjectNode}).
     *
     * @return constraint value, or {@code null} if unset
     */
    public Object getConstraint() {
        return constraint;
    }

    /**
     * Sets the entry constraint to a groupId or the literal {@code "businessHours"}.
     * Null or empty string resets the constraint to FullCalendar's default.
     *
     * @param constraint constraint string
     */
    public void setConstraint(String constraint) {
        this.constraint = StringUtils.trimToNull(constraint);
    }

    /**
     * Sets the entry constraint to a custom {@link BusinessHours} time window.
     * The entry can then only be dragged/resized within the given hours.
     *
     * @param hours business hours defining the allowed time window
     * @throws NullPointerException when null is passed
     * @see #setConstraint(String)
     * @see #setConstraintToBusinessHours()
     */
    public void setConstraint(BusinessHours hours) {
        this.constraint = Objects.requireNonNull(hours).toJson();
    }

    /**
     * Sets the entry's constraint to business hours, meaning the entry can only be placed during business hours.
     * Equivalent to {@code setConstraint("businessHours")}.
     */
    public void setConstraintToBusinessHours() {
        this.constraint = "businessHours";
    }

    /**
     * Sets the color for this entry. FullCalendar sets it on the entry's element as the CSS variable
     * {@code --fc-event-color}. The theme's styles read it, so where the color shows depends on the theme.
     * Own styles can read the variable as well, e.g. for entries with a CSS class:
     * <pre>{@code
     * .fc-event.important { border-left: 4px solid var(--fc-event-color); }
     * }</pre>
     * The class {@code fc-event} marks foreground entries, background entries carry {@code fc-bg-event}.
     * Null or empty string resets the color to FullCalendar's default.
     *
     * @param color color
     * @see <a href="https://fullcalendar.io/docs/custom-themes">Custom themes: event colors</a>
     */
    public void setColor(String color) {
        this.color = StringUtils.trimToNull(color);
    }

    /**
     * Sets the contrast color for this entry, used for text and other elements drawn on the entry color.
     * FullCalendar sets it on the entry's element as the CSS variable {@code --fc-event-contrast-color}. The
     * theme's styles read it, so where the color shows depends on the theme. Own styles can read the variable
     * as well:
     * <pre>{@code
     * .fc-event.important { outline: 2px dashed var(--fc-event-contrast-color); }
     * }</pre>
     * Null or empty string resets the color to FullCalendar's default.
     *
     * @param contrastColor contrast color
     * @see #setColor(String)
     */
    public void setContrastColor(String contrastColor) {
        this.contrastColor = StringUtils.trimToNull(contrastColor);
    }

    /**
     * Former name of {@link #getContrastColor()}: returns the contrast color of this entry. Delegates to it.
     *
     * @return the contrast color
     * @deprecated use {@link #getContrastColor()}. FullCalendar 7 renamed the text color to contrast color.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public String getTextColor() {
        return getContrastColor();
    }

    /**
     * Former name of {@link #setContrastColor(String)}: sets the contrast color of this entry. Delegates to it.
     *
     * @param textColor contrast color
     * @deprecated use {@link #setContrastColor(String)}. FullCalendar 7 renamed the text color to contrast color.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setTextColor(String textColor) {
        setContrastColor(textColor);
    }

    /**
     * Sets how the calendar renders this entry, see {@link #getDisplayMode()}.
     * @param displayMode how to display the entry, or null to follow the calendar's entry display
     */
    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    /**
     * Indicates, if this entry is recurring. This is indicated by having any "recurring" property set (e.g.
     * {@link #getRecurringDaysOfWeek()}) or an {@link RRule}.
     * @return is a recurring entry
     */
    public boolean isRecurring() {
        if (rrule != null) {
            return true;
        }
        Set<DayOfWeek> daysOfWeek = getRecurringDaysOfWeek();
        return (daysOfWeek != null && !daysOfWeek.isEmpty())
                || getRecurringEndDate() != null
                || getRecurringStartDate() != null
                || recurringStartTime != null
                || recurringEndTime != null;
    }

    /**
     * Sets days of week on which this entry shall recur. Setting a non empty set automatically marks this entry as
     * recurring. An {@link RRule} set before is removed completely, including its excluded dates and rules. Passing
     * null or an empty set removes only the days of week and leaves an {@link RRule} untouched.
     *
     * @param daysOfWeek day of week
     * @see #isRecurring()
     */
    public void setRecurringDaysOfWeek(DayOfWeek... daysOfWeek) {
        setRecurringDaysOfWeek(daysOfWeek.length == 0 ? null : new HashSet<>(Arrays.asList(daysOfWeek)));
    }

    /**
     * Sets days of week on which this entry shall recur. Setting a non empty set automatically marks this entry as
     * recurring. An {@link RRule} set before is removed completely, including its excluded dates and rules. Passing
     * null or an empty set removes only the days of week and leaves an {@link RRule} untouched.
     *
     * @param daysOfWeek days of week
     * @see #isRecurring()
     */
    public void setRecurringDaysOfWeek(Set<DayOfWeek> daysOfWeek) {
        if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
            setRRule(null);
        }
        this.recurringDaysOfWeek = daysOfWeek;
    }

    /**
     * Sets the first date of the recurrence. Passing a non null value automatically marks this entry as recurring. An
     * {@link RRule} set before is removed completely, including its excluded dates and rules.
     *
     * @param recurringStartDate first date of the recurrence
     * @see #isRecurring()
     */
    public void setRecurringStartDate(LocalDate recurringStartDate) {
        if (recurringStartDate != null) {
            setRRule(null);
        }
        this.recurringStartDate = recurringStartDate;
    }

    /**
     * Sets the date the recurrence ends (exclusive). Passing a non null value automatically marks this entry as
     * recurring. An {@link RRule} set before is removed completely, including its excluded dates and rules.
     *
     * @param recurringEndDate end date of the recurrence, exclusive
     * @see #isRecurring()
     */
    public void setRecurringEndDate(LocalDate recurringEndDate) {
        if (recurringEndDate != null) {
            setRRule(null);
        }
        this.recurringEndDate = recurringEndDate;
    }

    public Set<DayOfWeek> getRecurringDaysOfWeek() {
        return recurringDaysOfWeek;
    }

    /**
     * Returns the recurring start time as an {@link ExtendedLocalTime}. <br>
     * Since FullCalendar allows recurring times to be
     * above the normal 24h span of a day, this format is used to represent start and end "times".
     *
     * @return recurring start time
     */
    public ExtendedLocalTime getRecurringStartTime() {
        return recurringStartTime;
    }

    /**
     * Returns the recurring start time as a local time.<br>
     * Since FullCalendar allows recurring times to be above the normal 24h span of a day, using a LocalTime can lead to
     * issues, as it does not support times of 24h or above.
     *
     * @return LocalTime instance
     * @throws DateTimeException if this instance represents a time of 24 hours or above.
     */
    public LocalTime getRecurringStartTimeAsLocalTime() {
        return recurringStartTime != null ? recurringStartTime.toLocalTime() : null;
    }

    /**
     * Sets the start time for a recurring entry. Passing a non null value automatically marks this entry as recurring.
     * An {@link RRule} set before is removed completely, including its excluded dates and rules. Passing null may
     * remove the recurrence or let the recurring entry extend to the end of day.
     *
     * @param start start time
     * @see #isRecurring()
     */
    public void setRecurringStartTime(ExtendedLocalTime start) {
        if (start != null) {
            setRRule(null);
        }
        this.recurringStartTime = start;
    }

    /**
     * Sets the start time for a recurring entry. Passing a non null value automatically marks this entry as recurring.
     * An {@link RRule} set before is removed completely, including its excluded dates and rules. Passing null may
     * remove the recurrence or let the recurring entry extend to the end of day.
     *
     * @param start start time
     * @see #isRecurring()
     */
    public void setRecurringStartTime(LocalTime start) {
        setRecurringStartTime(start != null ? ExtendedLocalTime.of(start) : null);
    }

    /**
     * Returns the recurring end time as an {@link ExtendedLocalTime}. <br>
     * Since FullCalendar allows recurring times to be
     * above the normal 24h span of a day, this format is used to represent start and end "times".
     *
     * @return recurring end time
     */
    public ExtendedLocalTime getRecurringEndTime() {
        return recurringEndTime;
    }

    /**
     * Returns the recurring end time as a local time.<br>
     * Since FullCalendar allows recurring times to be above the normal 24h span of a day, using a LocalTime can lead to
     * issues, as it does not support times of 24h or above.
     *
     * @return LocalTime instance
     * @throws DateTimeException if this instance represents a time of 24 hours or above.
     */
    public LocalTime getRecurringEndTimeAsLocalTime() {
        return recurringEndTime != null ? recurringEndTime.toLocalTime() : null;
    }

    /**
     * Sets the end time for a recurring entry. Passing a non null value automatically marks this entry as recurring. An
     * {@link RRule} set before is removed completely, including its excluded dates and rules. Passing null may remove
     * the recurrence or let the recurring entry extend to the end of day.
     *
     * @param end end time
     * @see #isRecurring()
     */
    public void setRecurringEndTime(ExtendedLocalTime end) {
        if (end != null) {
            setRRule(null);
        }
        this.recurringEndTime = end;
    }

    /**
     * Sets the end time for a recurring entry. Passing a non null value automatically marks this entry as recurring. An
     * {@link RRule} set before is removed completely, including its excluded dates and rules. Passing null may remove
     * the recurrence or let the recurring entry extend to the end of day.
     *
     * @param end end time
     * @see #isRecurring()
     */
    public void setRecurringEndTime(LocalTime end) {
        setRecurringEndTime(end != null ? ExtendedLocalTime.of(end) : null);
    }

    /**
     * Replaces the extended props of this entry with a copy of the given map. {@code null} clears them.
     *
     * @param extendedProps extended props
     * @throws NullPointerException when the map contains a null key
     * @see #setExtendedProp(String, Object)
     */
    public void setExtendedProps(Map<String, Object> extendedProps) {
        Map<String, Object> copy = extendedProps != null ? new HashMap<>(extendedProps) : new HashMap<>();
        if (copy.containsKey(null)) {
            throw new NullPointerException("Extended props must not contain a null key");
        }

        this.extendedProps = copy.isEmpty() ? null : copy;
    }

    /**
     * Sets an extended prop. An extended prop is an additional value of your own, stored under a key of your
     * choice. Extended props do not change the regular properties of the entry, such as its title or color, and FullCalendar does not
     * use them itself. Setting a key again overwrites the extended prop stored under that key.
     * <p>
     * Extended props are sent to the client as the entry's {@code extendedProps}, where your own JavaScript
     * callbacks can read them, for instance in {@link Option#ENTRY_DID_MOUNT}:
     * <pre>{@code
     * entry.setExtendedProp("department", "Engineering");
     *
     * calendar.setOption(Option.ENTRY_DID_MOUNT, JsCallback.of(
     *         "info => info.el.title = info.event.extendedProps.department"));
     * }</pre>
     * Strings, numbers, booleans, maps, collections, arrays and {@link tools.jackson.databind.JsonNode}s are sent
     * as the matching JSON values. Numbers other than {@code Integer} and {@code Long} are sent as doubles, so a
     * {@code BigDecimal} can lose precision. Any other object is serialized with Jackson.
     * <p>
     * Entries that come from the client ({@link EntryReceiveEvent}, {@link DropEvent}) carry their extended props
     * as they were parsed from JSON, so an object you set arrives in those entries as a {@code Map}.
     * <p>
     * Call {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider#refreshItem(Entry)} after changing the
     * extended props of an entry that is already shown.
     *
     * @param key   the name of the property to set
     * @param value value to set, can be null
     */
    public void setExtendedProp(String key, Object value) {
        Objects.requireNonNull(key);
        if (extendedProps == null) {
            extendedProps = new HashMap<>();
        }

        extendedProps.put(key, value);
    }

    /**
     * Returns an extended prop, or null if the key is not set or set to null. Use
     * {@link #hasExtendedProp(String)} to tell the two apart.
     *
     * @param key name of the extended prop
     * @return value or null
     * @see #setExtendedProp(String, Object)
     */
    public Object getExtendedProp(String key) {
        return getExtendedProps().get(key);
    }

    /**
     * Returns whether an extended prop is set under the given key, also when its value is null.
     *
     * @param key name of the extended prop
     * @return true if the key is set
     */
    public boolean hasExtendedProp(String key) {
        return getExtendedProps().containsKey(key);
    }

    /**
     * Returns an extended prop, or the default value if the key is not set. A key set to {@code null} returns
     * {@code null}, not the default value.
     * <p>
     * The value is cast to the type of the default value without a check, so a value of another type leads to a
     * {@link ClassCastException} at the caller.
     *
     * @param key          name of the extended prop
     * @param defaultValue value returned when the key is not set
     * @param <T>          type of the value
     * @return value or the default value
     * @see #setExtendedProp(String, Object)
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtendedProp(String key, T defaultValue) {
        Map<String, Object> props = getExtendedProps();
        return props.containsKey(key) ? (T) props.get(key) : defaultValue;
    }

    /**
     * Removes an extended prop.
     *
     * @param key the name of the extended prop to remove
     */
    public void removeExtendedProp(String key) {
        Objects.requireNonNull(key);
        if (extendedProps != null) {
            extendedProps.remove(key);
            if (extendedProps.isEmpty()) {
                extendedProps = null;
            }
        }
    }

    /**
     * Returns the extended props of this entry as a read-only map. Never null. Change them with
     * {@link #setExtendedProp(String, Object)}, {@link #removeExtendedProp(String)} or
     * {@link #setExtendedProps(Map)}, then call this method again, because an earlier result may not reflect
     * the change.
     *
     * @return extended props, not modifiable
     */
    public Map<String, Object> getExtendedProps() {
        return extendedProps != null ? Collections.unmodifiableMap(extendedProps) : Collections.emptyMap();
    }

    protected <T, R> R convertNullable(T value, SerializableFunction<T, R> converter) {
        return value != null ? converter.apply(value) : null;
    }

}
