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

import org.vaadin.stefan.fullcalendar.ClassName;
import org.vaadin.stefan.fullcalendar.ClassNameValue;
import org.vaadin.stefan.fullcalendar.JsCallback;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

/**
 * Converts a {@link ClassNameValue} to its client form and back: the class names as a string, or the callback marker.
 */
public class ClassNameValueConverter implements JsonItemPropertyConverter<ClassNameValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof ClassNameValue;
    }

    @Override
    public JsonNode toClientModel(ClassNameValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case ClassName className -> JsonUtils.toJsonNode(className.names());
            case JsCallback callback -> callback.toMarkerJson();
        };
    }

    @Override
    public ClassNameValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isObject()) {
            return JsCallback.fromMarkerJson(clientValue);
        }
        if (!clientValue.isString()) {
            throw new IllegalArgumentException("Not a class name value: " + clientValue);
        }
        return ClassName.of(clientValue.asString());
    }
}
