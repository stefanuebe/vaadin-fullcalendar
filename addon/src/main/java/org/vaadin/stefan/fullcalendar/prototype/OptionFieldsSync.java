package org.vaadin.stefan.fullcalendar.prototype;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableBiConsumer;
import org.vaadin.stefan.fullcalendar.BeanProperties;
import org.vaadin.stefan.fullcalendar.CalendarView;
import org.vaadin.stefan.fullcalendar.JsCallback;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. The option state of one calendar: the global options, the options
 * for all views, the options per view and the raw options. Before each response it compares them with what the
 * client got last and sends only the changes. Shared by the core and the scheduler prototype calendar.
 */
public class OptionFieldsSync implements Serializable {

    private static final Map<Class<?>, List<BeanProperties<?>>> PROPERTIES = new ConcurrentHashMap<>();

    private final Component calendar;
    private final SerializableBiConsumer<String, JsonNode> sender;
    private final Supplier<? extends ViewOptions> viewOptionsFactory;

    private final CalendarOptions options;
    private final ViewOptions viewDefaults;
    private final Map<String, ViewOptions> viewOptions = new LinkedHashMap<>();
    private final Map<String, Object> rawOptions = new LinkedHashMap<>();
    private final Map<String, JsonNode> lastSent = new HashMap<>();
    private boolean syncScheduled;
    private Consumer<String> syncLog = s -> {};

    /**
     * @param sender sends one option to the client, null removes it
     */
    public <V extends ViewOptions> OptionFieldsSync(Component calendar, SerializableBiConsumer<String, JsonNode> sender,
                                                    CalendarOptions options, Supplier<V> viewOptionsFactory) {
        this.calendar = calendar;
        this.sender = sender;
        this.options = options;
        this.viewOptionsFactory = viewOptionsFactory;
        this.viewDefaults = viewOptionsFactory.get();
        this.viewDefaults.setEditable(true); // add-on default, replaces addonDefaultOptions
        scheduleSync(); // the initial values reach the client on the first attach, even if no option is touched
    }

    public CalendarOptions options() {
        scheduleSync();
        return options;
    }

    public ViewOptions viewDefaults() {
        scheduleSync();
        return viewDefaults;
    }

    public void setViewOptions(CalendarView view, ViewOptions options) {
        scheduleSync();
        if (options == null) {
            viewOptions.remove(view.getClientSideValue());
        } else {
            // ponytail: shallow copy, an array value (businessHours) is shared; copy it too if that matters
            viewOptions.put(view.getClientSideValue(), options.toBuilder().build());
        }
    }

    public ViewOptions getViewOptions(CalendarView view) {
        ViewOptions options = viewOptions.get(view.getClientSideValue());
        return options == null ? viewOptionsFactory.get() : options.toBuilder().build();
    }

    public void setRawOption(String option, Object value) {
        Objects.requireNonNull(option);
        scheduleSync();
        if (value == null) {
            rawOptions.remove(option);
        } else {
            rawOptions.put(option, value);
        }
    }

    public Optional<Object> getRawOption(String option) {
        return Optional.ofNullable(rawOptions.get(option));
    }

    public void setSyncLog(Consumer<String> syncLog) {
        this.syncLog = syncLog;
    }

    /**
     * After a detach, the client element may be a new one (for example after a refresh with @PreserveOnRefresh), so
     * the next sync sends every value again. A key removed while detached is still removed.
     */
    public void resendAllOnNextSync() {
        lastSent.replaceAll((key, value) -> JsonFactory.createNull());
        scheduleSync();
    }

    private void scheduleSync() {
        if (syncScheduled) {
            return;
        }
        syncScheduled = true;
        calendar.getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(calendar, ctx -> sync()));
    }

    private void sync() {
        syncScheduled = false;

        Map<String, JsonNode> current = toJson(viewDefaults);
        current.putAll(toJson(options));
        ObjectNode views = JsonFactory.createObject();
        viewOptions.forEach((view, o) -> {
            Map<String, JsonNode> json = toJson(o);
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
                sender.accept(key, value);
                log.append("set    ").append(key).append(" = ").append(value).append('\n');
            }
        });
        for (String key : List.copyOf(lastSent.keySet())) {
            if (!current.containsKey(key)) {
                sender.accept(key, null);
                log.append("remove ").append(key).append('\n');
            }
        }
        // A client element creates its calendar from this property: the first one, and a new one after a detach.
        // Options that work only at creation (initialDate, initialView, now) take effect only this way.
        ObjectNode initial = calendar.getElement().getPropertyRaw("initialOptions") instanceof ObjectNode node
                ? node.deepCopy() : JsonFactory.createObject();
        lastSent.keySet().forEach(initial::remove);
        initial.setAll(current);
        calendar.getElement().setPropertyJson("initialOptions", initial);

        lastSent.clear();
        lastSent.putAll(current);

        syncLog.accept(log.isEmpty() ? "(nothing changed)" : log.toString());
    }

    // ponytail: callback fields are applied last, so a set callback wins over a value with the same JSON name
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map<String, JsonNode> toJson(Object bean) {
        Map<String, JsonNode> json = new LinkedHashMap<>();
        for (BeanProperties def : properties(bean.getClass())) {
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

    /** The properties of the type and its superclasses. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<BeanProperties<?>> properties(Class<?> type) {
        return PROPERTIES.computeIfAbsent(type, t -> {
            List<BeanProperties<?>> list = new ArrayList<>();
            for (Class<?> c = t; c != Object.class; c = c.getSuperclass()) {
                BeanProperties.read((Class) c).forEach(p -> list.add((BeanProperties<?>) p));
            }
            list.sort(Comparator.comparing((BeanProperties<?> p) -> p.getField().getType() == JsCallback.class));
            return List.copyOf(list);
        });
    }
}
