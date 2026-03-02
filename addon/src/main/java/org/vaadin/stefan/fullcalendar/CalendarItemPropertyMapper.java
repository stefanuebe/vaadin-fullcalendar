package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * Maps arbitrary POJO properties to FullCalendar JSON event properties.
 * <p>
 * Supports both read (server→client via getters) and write (client→server via optional setters)
 * mappings. The mapper is immutable after first use (thread-safe): mutations after the first
 * call to {@link #toJson(Object)}, {@link #forItem(Object)}, or {@link #validate()} will throw
 * {@link IllegalStateException}.
 * <p>
 * Usage example:
 * <pre>{@code
 * var mapper = CalendarItemPropertyMapper.of(Meeting.class)
 *     .id(Meeting::getId)
 *     .title(Meeting::getSubject)
 *     .start(Meeting::getBegin, Meeting::setBegin)
 *     .end(Meeting::getFinish, Meeting::setFinish)
 *     .color(Meeting::getColor);
 * }</pre>
 *
 * @param <T> the POJO type
 * @author Stefan Uebe
 */
public class CalendarItemPropertyMapper<T> implements Serializable {

    private final Class<T> type;

    // Read mappings (server → client JSON)
    private final Map<String, PropertyMapping<T, ?>> readMappings = new LinkedHashMap<>();

    // Write mappings (client JSON → server POJO) — only for updatable properties
    private final Map<String, PropertyWriter<T, ?>> writeMappings = new LinkedHashMap<>();

    private volatile boolean frozen;

    // Optional JSON serializer hook — when set, toJson() delegates to this function instead of using readMappings
    private Function<T, ObjectNode> jsonSerializer;

    private CalendarItemPropertyMapper(Class<T> type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    /**
     * Creates a new mapper for the given POJO type.
     *
     * @param type the POJO class
     * @param <T>  the POJO type
     * @return a new mapper instance
     */
    public static <T> CalendarItemPropertyMapper<T> of(Class<T> type) {
        return new CalendarItemPropertyMapper<>(type);
    }

    /**
     * Sets a custom JSON serializer that will be used by {@link #toJson(Object)} instead of
     * the registered read mappings. This is useful when the POJO already has its own JSON
     * serialization method (e.g., {@code Entry::toJson}).
     * <p>
     * The {@code id} mapping is still required for cache lookups via {@link #getId(Object)}.
     *
     * @param serializer the function that converts a POJO to an ObjectNode
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> jsonSerializer(Function<T, ObjectNode> serializer) {
        ensureNotFrozen();
        this.jsonSerializer = Objects.requireNonNull(serializer, "serializer");
        return this;
    }

    // ---- Mandatory property ----

    /**
     * Maps the POJO's ID property. This mapping is mandatory.
     *
     * @param getter the ID getter
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> id(ValueProvider<T, String> getter) {
        return readOnly("id", getter, null);
    }

    /**
     * Maps the POJO's ID property by bean property name (reflection).
     *
     * @param propertyName the Java property name
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> id(String propertyName) {
        return mapByReflection("id", propertyName);
    }

    // ---- Core read-only properties ----

    public CalendarItemPropertyMapper<T> title(ValueProvider<T, String> getter) {
        return readOnly("title", getter, null);
    }

    public CalendarItemPropertyMapper<T> title(String propertyName) {
        return mapByReflection("title", propertyName);
    }

    public CalendarItemPropertyMapper<T> groupId(ValueProvider<T, String> getter) {
        return readOnly("groupId", getter, null);
    }

    public CalendarItemPropertyMapper<T> groupId(String propertyName) {
        return mapByReflection("groupId", propertyName);
    }

    public CalendarItemPropertyMapper<T> color(ValueProvider<T, String> getter) {
        return readOnly("color", getter, null);
    }

    public CalendarItemPropertyMapper<T> color(String propertyName) {
        return mapByReflection("color", propertyName);
    }

    public CalendarItemPropertyMapper<T> backgroundColor(ValueProvider<T, String> getter) {
        return readOnly("backgroundColor", getter, null);
    }

    public CalendarItemPropertyMapper<T> backgroundColor(String propertyName) {
        return mapByReflection("backgroundColor", propertyName);
    }

    public CalendarItemPropertyMapper<T> borderColor(ValueProvider<T, String> getter) {
        return readOnly("borderColor", getter, null);
    }

    public CalendarItemPropertyMapper<T> borderColor(String propertyName) {
        return mapByReflection("borderColor", propertyName);
    }

    public CalendarItemPropertyMapper<T> textColor(ValueProvider<T, String> getter) {
        return readOnly("textColor", getter, null);
    }

    public CalendarItemPropertyMapper<T> textColor(String propertyName) {
        return mapByReflection("textColor", propertyName);
    }

    public CalendarItemPropertyMapper<T> editable(ValueProvider<T, Boolean> getter) {
        return readOnly("editable", getter, null);
    }

    public CalendarItemPropertyMapper<T> editable(String propertyName) {
        return mapByReflection("editable", propertyName);
    }

    public CalendarItemPropertyMapper<T> startEditable(ValueProvider<T, Boolean> getter) {
        return readOnly("startEditable", getter, null);
    }

    public CalendarItemPropertyMapper<T> startEditable(String propertyName) {
        return mapByReflection("startEditable", propertyName);
    }

    public CalendarItemPropertyMapper<T> durationEditable(ValueProvider<T, Boolean> getter) {
        return readOnly("durationEditable", getter, null);
    }

    public CalendarItemPropertyMapper<T> durationEditable(String propertyName) {
        return mapByReflection("durationEditable", propertyName);
    }

    public CalendarItemPropertyMapper<T> display(ValueProvider<T, ?> getter) {
        // DisplayMode or String — the converter handles ClientSideValue instances
        return readOnly("display", getter, CalendarItemPropertyMapper::displayToJson);
    }

    public CalendarItemPropertyMapper<T> display(String propertyName) {
        return mapByReflection("display", propertyName, DISPLAY_CONVERTER);
    }

    public CalendarItemPropertyMapper<T> constraint(ValueProvider<T, String> getter) {
        return readOnly("constraint", getter, null);
    }

    public CalendarItemPropertyMapper<T> constraint(String propertyName) {
        return mapByReflection("constraint", propertyName);
    }

    public CalendarItemPropertyMapper<T> overlap(ValueProvider<T, Boolean> getter) {
        return readOnly("overlap", getter, null);
    }

    public CalendarItemPropertyMapper<T> overlap(String propertyName) {
        return mapByReflection("overlap", propertyName);
    }

    public CalendarItemPropertyMapper<T> classNames(ValueProvider<T, Set<String>> getter) {
        return readOnly("classNames", getter, null);
    }

    public CalendarItemPropertyMapper<T> classNames(String propertyName) {
        return mapByReflection("classNames", propertyName);
    }

    public CalendarItemPropertyMapper<T> customProperties(ValueProvider<T, Map<String, Object>> getter) {
        return readOnly("customProperties", getter, null);
    }

    public CalendarItemPropertyMapper<T> customProperties(String propertyName) {
        return mapByReflection("customProperties", propertyName);
    }

    // ---- Recurring properties (read-only) ----

    public CalendarItemPropertyMapper<T> recurringStartDate(ValueProvider<T, LocalDate> getter) {
        return readOnly("startRecur", getter, CalendarItemPropertyMapper::localDateToJson);
    }

    public CalendarItemPropertyMapper<T> recurringStartDate(String propertyName) {
        return mapByReflection("startRecur", propertyName, LOCAL_DATE_CONVERTER);
    }

    public CalendarItemPropertyMapper<T> recurringEndDate(ValueProvider<T, LocalDate> getter) {
        return readOnly("endRecur", getter, CalendarItemPropertyMapper::localDateToJson);
    }

    public CalendarItemPropertyMapper<T> recurringEndDate(String propertyName) {
        return mapByReflection("endRecur", propertyName, LOCAL_DATE_CONVERTER);
    }

    public CalendarItemPropertyMapper<T> recurringStartTime(ValueProvider<T, RecurringTime> getter) {
        return readOnly("startTime", getter, CalendarItemPropertyMapper::recurringTimeToJson);
    }

    public CalendarItemPropertyMapper<T> recurringStartTime(String propertyName) {
        return mapByReflection("startTime", propertyName, RECURRING_TIME_CONVERTER);
    }

    public CalendarItemPropertyMapper<T> recurringEndTime(ValueProvider<T, RecurringTime> getter) {
        return readOnly("endTime", getter, CalendarItemPropertyMapper::recurringTimeToJson);
    }

    public CalendarItemPropertyMapper<T> recurringEndTime(String propertyName) {
        return mapByReflection("endTime", propertyName, RECURRING_TIME_CONVERTER);
    }

    public CalendarItemPropertyMapper<T> recurringDaysOfWeek(ValueProvider<T, Set<DayOfWeek>> getter) {
        return readOnly("daysOfWeek", getter, CalendarItemPropertyMapper::daysOfWeekToJson);
    }

    public CalendarItemPropertyMapper<T> recurringDaysOfWeek(String propertyName) {
        return mapByReflection("daysOfWeek", propertyName, DAYS_OF_WEEK_CONVERTER);
    }

    // ---- Resource properties (scheduler) ----

    /**
     * Maps the POJO's resource IDs property (read-only). Used by scheduler views
     * to associate calendar items with resources.
     *
     * @param getter the resource IDs getter (must return {@link Set} of {@link String})
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> resourceIds(ValueProvider<T, Set<String>> getter) {
        return readOnly("resourceIds", getter, null);
    }

    /**
     * Maps the POJO's resource IDs property (bidirectional: getter + setter).
     * Used by scheduler views to associate calendar items with resources.
     *
     * @param getter the resource IDs getter
     * @param setter the resource IDs setter
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> resourceIds(ValueProvider<T, Set<String>> getter, Setter<T, Set<String>> setter) {
        readOnly("resourceIds", getter, null);
        writeMappings.put("resourceIds", new PropertyWriter<>(setter, CalendarItemPropertyMapper::jsonToStringSet));
        return this;
    }

    /**
     * Maps the POJO's resource-editable property (read-only). Controls whether
     * the item can be dragged between resources in scheduler views.
     *
     * @param getter the resourceEditable getter
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> resourceEditable(ValueProvider<T, Boolean> getter) {
        return readOnly("resourceEditable", getter, null);
    }

    // ---- Bidirectional properties (can be updated from client) ----

    /**
     * Maps the start property (read-only).
     *
     * @param getter the start getter (must return {@link LocalDateTime})
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> start(ValueProvider<T, LocalDateTime> getter) {
        return readOnly("start", getter, CalendarItemPropertyMapper::localDateTimeToJson);
    }

    /**
     * Maps the start property (bidirectional: getter + setter).
     *
     * @param getter the start getter
     * @param setter the start setter
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> start(ValueProvider<T, LocalDateTime> getter, Setter<T, LocalDateTime> setter) {
        readOnly("start", getter, CalendarItemPropertyMapper::localDateTimeToJson);
        writeMappings.put("start", new PropertyWriter<>(setter, CalendarItemPropertyMapper::jsonToLocalDateTime));
        return this;
    }

    /**
     * Maps the end property (read-only).
     *
     * @param getter the end getter (must return {@link LocalDateTime})
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> end(ValueProvider<T, LocalDateTime> getter) {
        return readOnly("end", getter, CalendarItemPropertyMapper::localDateTimeToJson);
    }

    /**
     * Maps the end property (bidirectional: getter + setter).
     *
     * @param getter the end getter
     * @param setter the end setter
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> end(ValueProvider<T, LocalDateTime> getter, Setter<T, LocalDateTime> setter) {
        readOnly("end", getter, CalendarItemPropertyMapper::localDateTimeToJson);
        writeMappings.put("end", new PropertyWriter<>(setter, CalendarItemPropertyMapper::jsonToLocalDateTime));
        return this;
    }

    /**
     * Maps the allDay property (read-only).
     *
     * @param getter the allDay getter
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> allDay(ValueProvider<T, Boolean> getter) {
        return readOnly("allDay", getter, null);
    }

    /**
     * Maps the allDay property (bidirectional: getter + setter).
     *
     * @param getter the allDay getter
     * @param setter the allDay setter
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> allDay(ValueProvider<T, Boolean> getter, Setter<T, Boolean> setter) {
        readOnly("allDay", getter, null);
        writeMappings.put("allDay", new PropertyWriter<>(setter, CalendarItemPropertyMapper::jsonToBoolean));
        return this;
    }

    // ---- Generic string-based mapping (reflection) ----

    /**
     * Maps a POJO field by name using reflection. The field must have a public getter.
     *
     * @param jsonName  the JSON property name in the FullCalendar event object
     * @param fieldName the Java field name on the POJO
     * @return this mapper for chaining
     */
    public CalendarItemPropertyMapper<T> map(String jsonName, String fieldName) {
        ensureNotFrozen();
        try {
            var getter = resolveGetter(fieldName);
            readMappings.put(jsonName, new PropertyMapping<>(getter, null));
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Cannot resolve getter for field '" + fieldName + "' on " + type.getName(), e);
        }
        return this;
    }

    // ---- Core API ----

    /**
     * Extracts the mapped ID from the given item.
     *
     * @param item the POJO
     * @return the ID string
     * @throws IllegalStateException if no id mapping is registered
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String getId(T item) {
        Objects.requireNonNull(item, "item");
        PropertyMapping idMapping = readMappings.get("id");
        if (idMapping == null) {
            throw new IllegalStateException("No id mapping registered. Use .id(Pojo::getId) to map the ID property.");
        }
        return (String) idMapping.getter.apply(item);
    }

    /**
     * Converts a POJO instance to a FullCalendar JSON event object.
     *
     * @param item the POJO
     * @return ObjectNode representing the calendar event
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ObjectNode toJson(T item) {
        Objects.requireNonNull(item, "item");
        freezeIfNeeded();

        if (jsonSerializer != null) {
            return jsonSerializer.apply(item);
        }

        ObjectNode json = JsonFactory.createObject();

        for (var entry : readMappings.entrySet()) {
            String jsonName = entry.getKey();
            PropertyMapping mapping = entry.getValue();

            Object value = mapping.getter.apply(item);
            JsonNode jsonValue;

            if (mapping.converter != null && value != null) {
                jsonValue = (JsonNode) mapping.converter.apply(value);
            } else {
                jsonValue = JsonUtils.toJsonNode(value);
            }

            if (jsonValue != null && !(jsonValue instanceof NullNode)) {
                json.set(jsonName, jsonValue);
            }
        }

        return json;
    }

    /**
     * Creates a bound mapper for a specific item instance.
     * Provides property access without repeated item reference.
     *
     * @param item the POJO to bind
     * @return a bound mapper
     */
    public BoundMapper<T> forItem(T item) {
        Objects.requireNonNull(item, "item");
        freezeIfNeeded();
        return new BoundMapper<>(this, item);
    }

    /**
     * Applies changes from a JSON object to the given POJO using registered setters.
     * Only properties with setters registered via bidirectional mapping methods are applied.
     *
     * @param item       the POJO to update
     * @param jsonChanges the JSON changes from the client
     * @throws IllegalStateException if no setters are registered
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void applyChanges(T item, ObjectNode jsonChanges) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(jsonChanges, "jsonChanges");

        if (writeMappings.isEmpty()) {
            throw new IllegalStateException(
                    "No setters registered. Use bidirectional mapping methods (e.g., .start(getter, setter)) " +
                    "or use a CalendarItemUpdateHandler instead.");
        }

        for (var entry : writeMappings.entrySet()) {
            String jsonName = entry.getKey();
            PropertyWriter writer = entry.getValue();

            if (jsonChanges.hasNonNull(jsonName)) {
                JsonNode jsonValue = jsonChanges.get(jsonName);
                Object serverValue = writer.fromJsonConverter.apply(jsonValue);
                writer.setter.accept(item, serverValue);
            }
        }
    }

    /**
     * Validates that all mandatory mappings are present.
     *
     * @throws IllegalStateException if id mapping is missing
     */
    public void validate() {
        freezeIfNeeded();
    }

    /**
     * Returns true if any setter (write mapping) has been registered.
     *
     * @return true if bidirectional mappings exist
     */
    public boolean hasSetters() {
        return !writeMappings.isEmpty();
    }

    /**
     * Returns the mapped POJO type.
     *
     * @return the POJO class
     */
    public Class<T> getType() {
        return type;
    }

    // ---- Converter constants for string-based (reflection) mappings ----
    // Raw ValueProvider to avoid generic type inference issues with method references
    @SuppressWarnings("rawtypes")
    private static final ValueProvider LOCAL_DATE_CONVERTER = (ValueProvider<LocalDate, JsonNode>) CalendarItemPropertyMapper::localDateToJson;
    @SuppressWarnings("rawtypes")
    private static final ValueProvider RECURRING_TIME_CONVERTER = (ValueProvider<RecurringTime, JsonNode>) CalendarItemPropertyMapper::recurringTimeToJson;
    @SuppressWarnings("rawtypes")
    private static final ValueProvider DAYS_OF_WEEK_CONVERTER = (ValueProvider<Set<DayOfWeek>, JsonNode>) CalendarItemPropertyMapper::daysOfWeekToJson;
    @SuppressWarnings("rawtypes")
    private static final ValueProvider DISPLAY_CONVERTER = (ValueProvider<Object, JsonNode>) CalendarItemPropertyMapper::displayToJson;

    // ---- Internal helpers ----

    private <V> CalendarItemPropertyMapper<T> readOnly(String jsonName, ValueProvider<T, V> getter,
                                                        ValueProvider<V, JsonNode> converter) {
        ensureNotFrozen();
        Objects.requireNonNull(jsonName, "jsonName");
        Objects.requireNonNull(getter, "getter");
        readMappings.put(jsonName, new PropertyMapping<>(getter, converter));
        return this;
    }

    private CalendarItemPropertyMapper<T> mapByReflection(String jsonName, String propertyName) {
        return mapByReflection(jsonName, propertyName, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private CalendarItemPropertyMapper<T> mapByReflection(String jsonName, String propertyName,
                                                           ValueProvider converter) {
        ensureNotFrozen();
        Objects.requireNonNull(jsonName, "jsonName");
        Objects.requireNonNull(propertyName, "propertyName");
        try {
            ValueProvider<T, Object> getter = resolveGetter(propertyName);
            readMappings.put(jsonName, new PropertyMapping(getter, converter));
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Cannot resolve getter for property '" + propertyName + "' on " + type.getName(), e);
        }
        return this;
    }

    private void ensureNotFrozen() {
        if (frozen) {
            throw new IllegalStateException("Mapper is frozen after first use and cannot be modified.");
        }
    }

    private void freezeIfNeeded() {
        if (!frozen) {
            if (!readMappings.containsKey("id")) {
                throw new IllegalStateException(
                        "Mandatory 'id' mapping is missing. Use .id(Pojo::getId) to map the ID property.");
            }
            frozen = true;
        }
    }

    @SuppressWarnings("unchecked")
    private ValueProvider<T, Object> resolveGetter(String fieldName) throws ReflectiveOperationException {
        var method = findMethod("get" + capitalize(fieldName));
        if (method == null) {
            method = findMethod("is" + capitalize(fieldName));
        }
        if (method == null) {
            method = findMethod(fieldName);
        }
        if (method == null) {
            throw new NoSuchMethodException(
                    "No getter found for field '" + fieldName + "' on " + type.getName());
        }

        var finalMethod = method;
        return item -> {
            try {
                return finalMethod.invoke(item);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to invoke getter for field " + fieldName, e);
            }
        };
    }

    private java.lang.reflect.Method findMethod(String name) {
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ---- Type converters (server → JSON) ----

    static JsonNode localDateTimeToJson(LocalDateTime value) {
        return value != null ? JsonFactory.create(JsonUtils.formatClientSideDateTimeString(value)) : null;
    }

    static JsonNode localDateToJson(LocalDate value) {
        return value != null ? JsonFactory.create(JsonUtils.formatClientSideDateString(value)) : null;
    }

    static JsonNode recurringTimeToJson(RecurringTime value) {
        return value != null ? JsonFactory.create(value.toFormattedString()) : null;
    }

    static JsonNode daysOfWeekToJson(Set<DayOfWeek> value) {
        if (value == null) return null;
        var array = JsonFactory.createArray();
        value.stream()
                .map(day -> day == DayOfWeek.SUNDAY ? 0 : day.getValue())
                .forEach(array::add);
        return array;
    }

    static JsonNode displayToJson(Object value) {
        if (value == null) return null;
        if (value instanceof ClientSideValue csv) {
            String clientValue = csv.getClientSideValue();
            return clientValue != null ? JsonFactory.create(clientValue) : null;
        }
        return JsonFactory.create(value.toString());
    }

    // ---- Type converters (JSON → server) ----

    static LocalDateTime jsonToLocalDateTime(JsonNode node) {
        if (node == null || node instanceof NullNode) return null;
        return JsonUtils.parseClientSideDateTime(node.asString());
    }

    static Boolean jsonToBoolean(JsonNode node) {
        if (node == null || node instanceof NullNode) return null;
        return node.asBoolean();
    }

    static Set<String> jsonToStringSet(JsonNode node) {
        if (node == null || node instanceof NullNode) return null;
        if (!node.isArray()) return Set.of();
        var result = new LinkedHashSet<String>();
        node.forEach(element -> result.add(element.asString()));
        return result;
    }

    // ---- Inner types ----

    /**
     * A read mapping: getter + optional JSON converter.
     */
    @SuppressWarnings("rawtypes")
    private record PropertyMapping<T, V>(
            ValueProvider<T, V> getter,
            ValueProvider converter
    ) implements Serializable {}

    /**
     * A write mapping: setter + JSON-to-server converter.
     */
    private record PropertyWriter<T, V>(
            Setter<T, V> setter,
            ValueProvider<JsonNode, V> fromJsonConverter
    ) implements Serializable {}

    /**
     * A mapper bound to a specific item instance. Provides typed property access.
     *
     * @param <T> the POJO type
     */
    public static class BoundMapper<T> {
        private final CalendarItemPropertyMapper<T> mapper;
        private final T item;

        private BoundMapper(CalendarItemPropertyMapper<T> mapper, T item) {
            this.mapper = mapper;
            this.item = item;
        }

        public ObjectNode toJson() {
            return mapper.toJson(item);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public String getId() {
            PropertyMapping idMapping = mapper.readMappings.get("id");
            if (idMapping == null) throw new IllegalStateException("No id mapping");
            return (String) idMapping.getter.apply(item);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public String getTitle() {
            PropertyMapping titleMapping = mapper.readMappings.get("title");
            if (titleMapping == null) return null;
            return (String) titleMapping.getter.apply(item);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public LocalDateTime getStart() {
            PropertyMapping startMapping = mapper.readMappings.get("start");
            if (startMapping == null) return null;
            return (LocalDateTime) startMapping.getter.apply(item);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public LocalDateTime getEnd() {
            PropertyMapping endMapping = mapper.readMappings.get("end");
            if (endMapping == null) return null;
            return (LocalDateTime) endMapping.getter.apply(item);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public Set<String> getResourceIds() {
            PropertyMapping resourceIdsMapping = mapper.readMappings.get("resourceIds");
            if (resourceIdsMapping == null) return null;
            return (Set<String>) resourceIdsMapping.getter.apply(item);
        }

        public T getItem() {
            return item;
        }
    }
}
