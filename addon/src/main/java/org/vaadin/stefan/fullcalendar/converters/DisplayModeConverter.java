package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.DisplayMode;
import tools.jackson.databind.JsonNode;

/**
 * Converts {@link DisplayMode} to/from its FullCalendar client-side string representation.
 * Extends {@link ClientSideValueConverter} for the toClientModel direction.
 */
public class DisplayModeConverter extends ClientSideValueConverter {

    @Override
    public boolean supports(Object type) {
        return type instanceof DisplayMode;
    }

    @Override
    public DisplayMode toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue == null || clientValue.isNull()) {
            return DisplayMode.AUTO;
        }
        String value = clientValue.asString();
        if (value == null || value.isEmpty()) {
            return DisplayMode.AUTO;
        }
        for (DisplayMode mode : DisplayMode.values()) {
            if (value.equals(mode.getClientSideValue())) {
                return mode;
            }
        }
        return DisplayMode.AUTO;
    }
}
