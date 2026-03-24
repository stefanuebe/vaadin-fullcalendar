package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.BusinessHours;

/**
 * Converts {@link BusinessHours} or {@link BusinessHours}{@code []} to their JSON representation
 * for the FullCalendar client side.
 */
public class BusinessHoursConverter<T> implements JsonItemPropertyConverter<Object, T> {

    @Override
    public boolean supports(Object type) {
        return type instanceof BusinessHours || type instanceof BusinessHours[];
    }

    @Override
    public JsonValue toClientModel(Object serverValue, T currentInstance) {
        if (serverValue instanceof BusinessHours) {
            return ((BusinessHours) serverValue).toJson();
        }
        if (serverValue instanceof BusinessHours[]) {
            BusinessHours[] arr = (BusinessHours[]) serverValue;
            if (arr.length == 1) {
                return arr[0].toJson();
            }
            JsonArray array = Json.createArray();
            for (int i = 0; i < arr.length; i++) {
                array.set(i, arr[i].toJson());
            }
            return array;
        }
        throw new IllegalArgumentException("Unsupported type: " + serverValue.getClass());
    }
}
