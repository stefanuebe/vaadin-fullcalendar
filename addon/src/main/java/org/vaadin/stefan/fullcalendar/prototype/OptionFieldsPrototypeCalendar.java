package org.vaadin.stefan.fullcalendar.prototype;

import lombok.experimental.Delegate;
import org.vaadin.stefan.fullcalendar.BeanProperties;
import org.vaadin.stefan.fullcalendar.CalendarView;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.JsCallback;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. A calendar whose options are fields. Lombok's {@code @Delegate}
 * puts the accessors of {@link CalendarOptions} on the calendar. Before each response, the fields are compared with
 * what the client got last, and only the changes are sent.
 */
public class OptionFieldsPrototypeCalendar extends FullCalendar {

    private static final List<BeanProperties<?>> VIEW_PROPERTIES = properties(ViewOptions.class);
    private static final List<BeanProperties<?>> CALENDAR_PROPERTIES = Stream.concat(VIEW_PROPERTIES.stream(),
            properties(CalendarOptions.class).stream()).toList();

    private final CalendarOptions options = new CalendarOptions();
    private final Map<String, ViewOptions> viewOptions = new LinkedHashMap<>();
    private final Map<String, JsonNode> lastSent = new HashMap<>();
    private final Map<String, Object> rawOptions = new LinkedHashMap<>();
    private boolean syncScheduled;
    private Consumer<String> syncLog = s -> {};

    /**
     * The delegate target. Every delegated call goes through here, so a getter or setter call schedules the sync.
     */
    @Delegate(excludes = NotDelegated.class)
    private CalendarOptions options() {
        scheduleSync();
        return options;
    }

    /**
     * Sets the options for one view type only. A value set here overrides the calendar value while that view is
     * shown. The calendar keeps a copy, so later changes to the passed instance have no effect. Null removes the
     * options of the view.
     */
    public void setViewOptions(CalendarView view, ViewOptions viewOptions) {
        scheduleSync();
        if (viewOptions == null) {
            this.viewOptions.remove(view.getClientSideValue());
        } else {
            // ponytail: shallow copy, an array value (businessHours) is shared; copy it too if that matters
            this.viewOptions.put(view.getClientSideValue(), viewOptions.toBuilder().build());
        }
    }

    /**
     * Returns a copy of the options of one view type, or empty options when none are set. Changes to it have no
     * effect until it is set again.
     */
    public ViewOptions getViewOptions(CalendarView view) {
        ViewOptions options = viewOptions.get(view.getClientSideValue());
        return options == null ? new ViewOptions() : options.toBuilder().build();
    }

    /**
     * Sets an option by its name in the library, for an option the add-on has no field for. Advanced use only: the
     * value is passed on as it is, without conversion, and it always wins over a field with the same name. The
     * field's getter still returns the field value. Null removes the raw option, then the field applies again.
     */
    public void setRawOption(String option, Object value) {
        Objects.requireNonNull(option);
        scheduleSync();
        if (value == null) {
            rawOptions.remove(option);
        } else {
            rawOptions.put(option, value);
        }
    }

    /**
     * Returns the value set with {@link #setRawOption(String, Object)}, or empty.
     */
    public Optional<Object> getRawOption(String option) {
        return Optional.ofNullable(rawOptions.get(option));
    }

    public void setSyncLog(Consumer<String> syncLog) {
        this.syncLog = syncLog;
    }

    /** Methods of the options classes that must not appear on the calendar. */
    private interface NotDelegated {
        ViewOptions.ViewOptionsBuilder<?, ?> toBuilder();
    }

    private void scheduleSync() {
        if (syncScheduled) {
            return;
        }
        syncScheduled = true;
        getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(this, ctx -> sync()));
    }

    private void sync() {
        syncScheduled = false;

        Map<String, JsonNode> current = toJson(options, CALENDAR_PROPERTIES);
        ObjectNode views = JsonFactory.createObject();
        viewOptions.forEach((view, o) -> {
            Map<String, JsonNode> json = toJson(o, VIEW_PROPERTIES);
            if (!json.isEmpty()) {
                views.set(view, JsonFactory.createObject().setAll(json));
            }
        });
        if (!views.isEmpty()) {
            current.put("views", views);
        }
        rawOptions.forEach((key, value) -> current.put(key, JsonUtils.toJsonNode(value)));

        StringBuilder log = new StringBuilder();
        current.forEach((key, value) -> {
            if (!value.equals(lastSent.get(key))) {
                setOption(key, value, null, List.of());
                log.append("set    ").append(key).append(" = ").append(value).append('\n');
            }
        });
        for (String key : List.copyOf(lastSent.keySet())) {
            if (!current.containsKey(key)) {
                setOption(key, null, null, List.of());
                log.append("remove ").append(key).append('\n');
            }
        }
        lastSent.clear();
        lastSent.putAll(current);

        syncLog.accept(log.isEmpty() ? "(nothing changed)" : log.toString());
    }

    // ponytail: callback fields are applied last, so a set callback wins over a value with the same JSON name
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map<String, JsonNode> toJson(Object bean, List<BeanProperties<?>> properties) {
        Map<String, JsonNode> json = new LinkedHashMap<>();
        for (BeanProperties def : properties) {
            Object value = def.getGetter().apply(bean);
            if (value == null) {
                continue;
            }
            JsonItemPropertyConverter converter = def.getConverter();
            json.put(def.getJsonName(), converter != null && converter.supports(value)
                    ? converter.toClientModel(value, bean)
                    : JsonUtils.toJsonNode(value));
        }
        return json;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<BeanProperties<?>> properties(Class<?> type) {
        return BeanProperties.read((Class) type).stream()
                .map(p -> (BeanProperties<?>) p)
                .sorted(Comparator.comparing((BeanProperties<?> p) -> p.getField().getType() == JsCallback.class))
                .toList();
    }
}
