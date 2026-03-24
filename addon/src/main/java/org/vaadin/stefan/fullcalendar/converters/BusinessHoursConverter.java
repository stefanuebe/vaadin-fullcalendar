package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonArray;
import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.BusinessHours;
import org.vaadin.stefan.fullcalendar.JsonFactory;

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
    public JsonValue toClientModel(Object serverValue, Object currentInstance) {
        if (serverValue instanceof BusinessHours) {
            BusinessHours bh = (BusinessHours) serverValue;
            return bh.toJson();
        }
        if (serverValue instanceof BusinessHours[]) {
            BusinessHours[] arr = (BusinessHours[]) serverValue;
            if (arr.length == 1) {
                return arr[0].toJson();
            }
            JsonArray array = JsonFactory.createArray();
            for (BusinessHours bh : arr) {
                array.set(array.length(), bh.toJson());
            }
            return array;
        }
        throw new IllegalArgumentException("Unsupported type: " + serverValue.getClass());
    }
}
