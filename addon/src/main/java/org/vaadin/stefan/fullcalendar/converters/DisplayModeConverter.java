package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonType;
import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.DisplayMode;

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
    public DisplayMode toServerModel(JsonValue clientValue, Object currentInstance) {
        if (clientValue == null || clientValue.getType() == JsonType.NULL) {
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
