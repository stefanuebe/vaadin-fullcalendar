/*
 * Copyright 2026, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.LocaleValue;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Converts a {@link LocaleValue} to its client form and back: the lower-case language tag, or a locale object with
 * the language tag as {@code code} and the options beside it.
 */
public class LocaleValueConverter implements JsonItemPropertyConverter<LocaleValue, Object> {

    private static final String CODE = "code";

    private static String toCode(Locale locale) {
        return locale.toLanguageTag().toLowerCase();
    }

    /** Reads an option value as the custom locale holds it: maps, lists, callbacks and JSON scalars. */
    private static Object toValue(JsonNode json) {
        if (JsCallback.isMarker(json)) {
            return JsCallback.fromMarkerJson(json);
        }
        if (json.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            json.properties().forEach(property -> map.put(property.getKey(), toValue(property.getValue())));
            return map;
        }
        if (json.isArray()) {
            List<Object> list = new ArrayList<>();
            json.forEach(item -> list.add(toValue(item)));
            return list;
        }

        return json.isInt() ? json.asInt() : JsonUtils.ofJsonNode(json);
    }

    @Override
    public boolean supports(Object type) {
        return type instanceof LocaleValue;
    }

    @Override
    public JsonNode toClientModel(LocaleValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case LocaleValue.Code code -> JsonUtils.toJsonNode(toCode(code.locale()));
            case LocaleValue.Custom custom -> {
                ObjectNode json = (ObjectNode) JsonUtils.toJsonNode(custom.options());
                json.put(CODE, toCode(custom.locale()));
                yield json;
            }
        };
    }

    @Override
    public LocaleValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isString()) {
            return LocaleValue.of(Locale.forLanguageTag(clientValue.asString()));
        }
        if (!clientValue.isObject() || !clientValue.path(CODE).isString()) {
            throw new IllegalArgumentException("Not a locale value: " + clientValue);
        }

        Map<String, Object> options = new LinkedHashMap<>();
        clientValue.properties().stream()
                .filter(property -> !property.getKey().equals(CODE))
                .forEach(property -> options.put(property.getKey(), toValue(property.getValue())));

        return LocaleValue.ofCustom(Locale.forLanguageTag(clientValue.get(CODE).asString()), options);
    }
}
