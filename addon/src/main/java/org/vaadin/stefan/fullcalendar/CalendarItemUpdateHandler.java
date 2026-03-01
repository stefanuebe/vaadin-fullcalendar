package org.vaadin.stefan.fullcalendar;

import java.io.Serializable;

/**
 * Strategy B functional interface for applying client-side changes to calendar items.
 * <p>
 * This is an alternative to registering setters on the {@link CalendarItemPropertyMapper}.
 * Use this when you need full control over how changes are applied, for instance when
 * working with immutable objects or when additional business logic is needed.
 * <p>
 * Only one strategy can be active: either setters on the mapper (Strategy A) or an
 * update handler (Strategy B). If both are registered, an exception is thrown at
 * configuration time.
 *
 * @param <T> the calendar item type
 * @author Stefan Uebe
 */
@FunctionalInterface
public interface CalendarItemUpdateHandler<T> extends Serializable {

    /**
     * Handles an update from the client side (e.g., drag/drop/resize).
     *
     * @param item    the calendar item to update
     * @param changes the changes received from the client
     */
    void handleUpdate(T item, CalendarItemChanges changes);
}
