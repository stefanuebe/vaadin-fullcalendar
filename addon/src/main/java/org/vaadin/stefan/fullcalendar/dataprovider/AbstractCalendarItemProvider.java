package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Abstract base implementation of the {@link CalendarItemProvider} interface.
 * Provides listener management and event firing.
 *
 * @param <T> the calendar item type
 * @author Stefan Uebe
 */
public abstract class AbstractCalendarItemProvider<T> implements CalendarItemProvider<T> {

    private final Map<Class<?>, List<SerializableConsumer<?>>> listeners = new ConcurrentHashMap<>();

    @Override
    public void refreshAll() {
        fireEvent(new CalendarItemsChangeEvent<>(this));
    }

    @Override
    public void refreshItem(T item) {
        fireEvent(new CalendarItemRefreshEvent<>(this, item));
    }

    @Override
    public Registration addItemsChangeListener(CalendarItemsChangeEvent.Listener<T> listener) {
        return addListener(CalendarItemsChangeEvent.class, listener::onDataChange);
    }

    @Override
    public Registration addItemRefreshListener(CalendarItemRefreshEvent.Listener<T> listener) {
        return addListener(CalendarItemRefreshEvent.class, listener::onDataRefresh);
    }

    /**
     * Registers a new listener for the specified event type.
     *
     * @param eventType the type of event to listen for
     * @param method    the consumer to receive the event
     * @param <E>       the event type
     * @return a registration for the listener
     */
    protected <E> Registration addListener(Class<E> eventType, SerializableConsumer<E> method) {
        List<SerializableConsumer<?>> list = listeners.computeIfAbsent(eventType, key -> new CopyOnWriteArrayList<>());
        return Registration.addAndRemove(list, method);
    }

    /**
     * Sends the event to all registered listeners.
     *
     * @param event the event to fire
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void fireEvent(EventObject event) {
        listeners.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(event.getClass()))
                .forEach(entry -> {
                    for (Consumer consumer : entry.getValue()) {
                        consumer.accept(event);
                    }
                });
    }
}
