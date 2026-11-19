package org.vaadin.stefan.fullcalendar.option;

import org.vaadin.stefan.fullcalendar.ClientSideValue;

/**
 * Constants for rendering of an entry.
 */
public enum DisplayMode implements ClientSideValue {
    /**
     * Does not render the entry. The entry is still sent to the client, with all its data, and only the browser
     * leaves it out. So this does not keep an entry from a user who must not see it. Leave such an entry out on the
     * server, for example in the entry provider.
     */
    NONE("none"),

    /**
     * Renders as a solid rectangle in day grid
     */
    BLOCK("block"),

    /**
     * Renders with a dot when in day grid
     */
    LIST_ITEM("list-item"),

    /**
     * Renders as 'block' if all-day or multi-day, otherwise will display as 'list-item'
     */
    AUTO("auto"),

    /**
     * Renders as background entry (marks the area of the entry interval).
     */
    BACKGROUND("background"),

    /**
     * Renders as inversed background entry (marks everything except the entry interval).
     */
    INVERSE_BACKGROUND("inverse-background");

    private final String clientSideName;

    DisplayMode(String clientSideName) {
        this.clientSideName = clientSideName;
    }

    @Override
    public String getClientSideValue() {
        return clientSideName;
    }
}
