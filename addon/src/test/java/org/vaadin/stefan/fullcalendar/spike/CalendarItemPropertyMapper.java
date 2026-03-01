package org.vaadin.stefan.fullcalendar.spike;

import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Spike 0.1: Prototype for mapping arbitrary POJOs to FullCalendar JSON.
 * <p>
 * Maps POJO properties to FullCalendar's expected JSON structure using either
 * lambda-based or string-based property access. Supports bidirectional mapping
 * for client-updatable properties (start, end, allDay).
 *
 * @param <T> the POJO type
 */
public class CalendarItemPropertyMapper<T> implements Serializable {

    private final Class<T> type;

    // Read mappings (server → client JSON)
    private final Map<String, PropertyMapping<T, ?>> readMappings = new LinkedHashMap<>();

    // Write mappings (client JSON → server POJO) — only for updatable properties
    private final Map<String, PropertyWriter<T, ?>> writeMappings = new LinkedHashMap<>();

    private boolean frozen;

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

    // ---- Mandatory property ----

    /**
     * Maps the POJO's ID property. This mapping is mandatory.
     */
    public CalendarItemPropertyMapper<T> id(ValueProvider<T, String> getter) {
        return readOnly("id", getter, null);
    }

    // ---- Core read-only properties ----

    public CalendarItemPropertyMapper<T> groupId(ValueProvider<T, String> getter) {
        return readOnly("groupId", getter, null);
    }

    public CalendarItemPropertyMapper<T> title(ValueProvider<T, String> getter) {
        return readOnly("title", getter, null);
    }

    public CalendarItemPropertyMapper<T> editable(ValueProvider<T, Boolean> getter) {
        return readOnly("editable", getter, null);
    }

    public CalendarItemPropertyMapper<T> startEditable(ValueProvider<T, Boolean> getter) {
        return readOnly("startEditable", getter, null);
    }

    public CalendarItemPropertyMapper<T> durationEditable(ValueProvider<T, Boolean> getter) {
        return readOnly("durationEditable", getter, null);
    }

    public CalendarItemPropertyMapper<T> color(ValueProvider<T, String> getter) {
        return readOnly("color", getter, null);
    }

    public CalendarItemPropertyMapper<T> backgroundColor(ValueProvider<T, String> getter) {
        return readOnly("backgroundColor", getter, null);
    }

    public CalendarItemPropertyMapper<T> borderColor(ValueProvider<T, String> getter) {
        return readOnly("borderColor", getter, null);
    }

    public CalendarItemPropertyMapper<T> textColor(ValueProvider<T, String> getter) {
        return readOnly("textColor", getter, null);
    }

    public CalendarItemPropertyMapper<T> constraint(ValueProvider<T, String> getter) {
        return readOnly("constraint", getter, null);
    }

    public CalendarItemPropertyMapper<T> overlap(ValueProvider<T, Boolean> getter) {
        return readOnly("overlap", getter, null);
    }

    public CalendarItemPropertyMapper<T> display(ValueProvider<T, ?> getter) {
        // DisplayMode or String — client expects the string value
        return readOnly("display", getter, null);
    }

    public CalendarItemPropertyMapper<T> classNames(ValueProvider<T, Set<String>> getter) {
        return readOnly("classNames", getter, null);
    }

    public CalendarItemPropertyMapper<T> customProperties(ValueProvider<T, Map<String, Object>> getter) {
        return readOnly("customProperties", getter, null);
    }

    // ---- Recurring properties (read-only) ----

    public CalendarItemPropertyMapper<T> recurringStartDate(ValueProvider<T, LocalDate> getter) {
        return readOnly("startRecur", getter, CalendarItemPropertyMapper::localDateToJson);
    }

    public CalendarItemPropertyMapper<T> recurringEndDate(ValueProvider<T, LocalDate> getter) {
        return readOnly("endRecur", getter, CalendarItemPropertyMapper::localDateToJson);
    }

    public CalendarItemPropertyMapper<T> recurringDaysOfWeek(ValueProvider<T, Set<DayOfWeek>> getter) {
        return readOnly("daysOfWeek", getter, CalendarItemPropertyMapper::daysOfWeekToJson);
    }

    // ---- Bidirectional properties (can be updated from client) ----

    /**
     * Maps the start property (read-only).
     */
    public CalendarItemPropertyMapper<T> start(ValueProvider<T, LocalDateTime> getter) {
        return readOnly("start", getter, CalendarItemPropertyMapper::localDateTimeToJson);
    }

    /**
     * Maps the start property (bidirectional: getter + setter).
     */
    public CalendarItemPropertyMapper<T> start(ValueProvider<T, LocalDateTime> getter, Setter<T, LocalDateTime> setter) {
        readOnly("start", getter, CalendarItemPropertyMapper::localDateTimeToJson);
        writeMappings.put("start", new PropertyWriter<>(setter, CalendarItemPropertyMapper::jsonToLocalDateTime));
        return this;
    }

    /**
     * Maps the end property (read-only).
     */
    public CalendarItemPropertyMapper<T> end(ValueProvider<T, LocalDateTime> getter) {
        return readOnly("end", getter, CalendarItemPropertyMapper::localDateTimeToJson);
    }

    /**
     * Maps the end property (bidirectional: getter + setter).
     */
    public CalendarItemPropertyMapper<T> end(ValueProvider<T, LocalDateTime> getter, Setter<T, LocalDateTime> setter) {
        readOnly("end", getter, CalendarItemPropertyMapper::localDateTimeToJson);
        writeMappings.put("end", new PropertyWriter<>(setter, CalendarItemPropertyMapper::jsonToLocalDateTime));
        return this;
    }

    /**
     * Maps the allDay property (read-only).
     */
    public CalendarItemPropertyMapper<T> allDay(ValueProvider<T, Boolean> getter) {
        return readOnly("allDay", getter, null);
    }

    /**
     * Maps the allDay property (bidirectional: getter + setter).
     */
    public CalendarItemPropertyMapper<T> allDay(ValueProvider<T, Boolean> getter, Setter<T, Boolean> setter) {
        readOnly("allDay", getter, null);
        writeMappings.put("allDay", new PropertyWriter<>(setter, CalendarItemPropertyMapper::jsonToBoolean));
        return this;
    }

    // ---- String-based mapping (reflection) ----

    /**
     * Maps a POJO field by name using reflection. The field must have a public getter.
     * The JSON property name defaults to the field name unless a jsonName is provided.
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
     * Converts a POJO instance to a FullCalendar JSON event object.
     *
     * @param item the POJO
     * @return ObjectNode representing the calendar event
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ObjectNode toJson(T item) {
        Objects.requireNonNull(item, "item");
        freezeIfNeeded();

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
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void applyChanges(T item, ObjectNode jsonChanges) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(jsonChanges, "jsonChanges");

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
        if (!readMappings.containsKey("id")) {
            throw new IllegalStateException("Mandatory 'id' mapping is missing. Use .id(Pojo::getId) to map the ID property.");
        }
    }

    /**
     * Returns true if any setter (write mapping) has been registered.
     */
    public boolean hasSetters() {
        return !writeMappings.isEmpty();
    }

    /**
     * Returns the mapped POJO type.
     */
    public Class<T> getType() {
        return type;
    }

    // ---- Internal helpers ----

    private <V> CalendarItemPropertyMapper<T> readOnly(String jsonName, ValueProvider<T, V> getter,
                                                        ValueProvider<V, JsonNode> converter) {
        ensureNotFrozen();
        Objects.requireNonNull(jsonName, "jsonName");
        Objects.requireNonNull(getter, "getter");
        readMappings.put(jsonName, new PropertyMapping<>(getter, converter));
        return this;
    }

    private void ensureNotFrozen() {
        if (frozen) {
            throw new IllegalStateException("Mapper is frozen after first use and cannot be modified.");
        }
    }

    private void freezeIfNeeded() {
        if (!frozen) {
            validate();
            frozen = true;
        }
    }

    @SuppressWarnings("unchecked")
    private ValueProvider<T, Object> resolveGetter(String fieldName) throws ReflectiveOperationException {
        // Try get + capitalized name
        var method = findMethod("get" + capitalize(fieldName));
        if (method == null) {
            method = findMethod("is" + capitalize(fieldName));
        }
        if (method == null) {
            method = findMethod(fieldName);
        }
        if (method == null) {
            throw new NoSuchMethodException("No getter found for field '" + fieldName + "' on " + type.getName());
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

    static JsonNode daysOfWeekToJson(Set<DayOfWeek> value) {
        if (value == null) return null;
        var array = JsonFactory.createArray();
        value.stream()
                .map(day -> day == DayOfWeek.SUNDAY ? 0 : day.getValue())
                .forEach(array::add);
        return array;
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

        public T getItem() {
            return item;
        }
    }
}
