package org.vaadin.stefan.fullcalendar;

import javax.annotation.Nullable;

/**
 * Marks a class as a client side setting, which returns client side representation for an instance of this class.
 * An enumeration for instance would return a client side parseable version of the server side enum instance,
 * e.g. RenderingMode.INVERSE_BACKGROUND.getClientSideName() // returns "inverse-background"
 */
public interface ClientSideValue {
    /**
     * Returns a client side representation of this instance.
     * @return String
     */
    @Nullable String getClientSideValue();
}
