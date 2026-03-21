package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.BusinessHours;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

/**
 * Converts {@link BusinessHours} or {@link BusinessHours}{@code []} to their JSON representation
 * for the FullCalendar client side.
 */
public class BusinessHoursConverter implements JsonItemPropertyConverter<Object, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof BusinessHours || type instanceof BusinessHours[];
    }

    @Override
    public JsonNode toClientModel(Object serverValue, Object currentInstance) {
        if (serverValue instanceof BusinessHours bh) {
            return bh.toJson();
        }
        if (serverValue instanceof BusinessHours[] arr) {
            if (arr.length == 1) {
                return arr[0].toJson();
            }
            ArrayNode array = JsonFactory.createArray();
            for (BusinessHours bh : arr) {
                array.add(bh.toJson());
            }
            return array;
        }
        throw new IllegalArgumentException("Unsupported type: " + serverValue.getClass());
    }
}
