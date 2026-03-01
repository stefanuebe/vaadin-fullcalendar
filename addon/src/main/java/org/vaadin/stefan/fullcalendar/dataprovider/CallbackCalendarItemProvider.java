package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link CalendarItemProvider} using predefined callbacks to fetch items.
 * <p>
 * Useful for lazy-loading items from a database or external service.
 *
 * @param <T> the calendar item type
 * @author Stefan Uebe
 */
public class CallbackCalendarItemProvider<T> extends AbstractCalendarItemProvider<T> {

    private final SerializableFunction<CalendarQuery, Stream<T>> fetchCallback;
    private final SerializableFunction<String, T> fetchByIdCallback;

    /**
     * Creates a new callback provider.
     *
     * @param fetchCallback     callback to fetch items based on the given query
     * @param fetchByIdCallback callback to fetch a single item by ID (may return null)
     */
    public CallbackCalendarItemProvider(
            SerializableFunction<CalendarQuery, Stream<T>> fetchCallback,
            SerializableFunction<String, T> fetchByIdCallback) {
        this.fetchCallback = Objects.requireNonNull(fetchCallback, "fetchCallback");
        this.fetchByIdCallback = Objects.requireNonNull(fetchByIdCallback, "fetchByIdCallback");
    }

    @Override
    public Stream<T> fetch(CalendarQuery query) {
        return fetchCallback.apply(query);
    }

    @Override
    public Optional<T> fetchById(String id) {
        return Optional.ofNullable(fetchByIdCallback.apply(id));
    }
}
