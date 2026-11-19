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
import org.vaadin.stefan.fullcalendar.option.MoreLinkClickValue;
import org.vaadin.stefan.fullcalendar.option.ViewName;
import tools.jackson.databind.JsonNode;

/**
 * Converts a {@link MoreLinkClickValue} to its client form and back: the action or the view name as a string, or the
 * callback marker. Reading back, a string that names an action is that action, any other string a view name. A view
 * named like an action, such as {@code "day"}, therefore reads back as the action, which the library treats alike.
 */
public class MoreLinkClickValueConverter implements JsonItemPropertyConverter<MoreLinkClickValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof MoreLinkClickValue;
    }

    @Override
    public JsonNode toClientModel(MoreLinkClickValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case MoreLinkClickValue.Action action -> JsonUtils.toJsonNode(action.getClientSideValue());
            case ViewName name -> JsonUtils.toJsonNode(name.name());
            case JsCallback callback -> callback.toMarkerJson();
        };
    }

    @Override
    public MoreLinkClickValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isObject()) {
            return JsCallback.fromMarkerJson(clientValue);
        }
        if (!clientValue.isString()) {
            throw new IllegalArgumentException("Not a more link click value: " + clientValue);
        }
        String value = clientValue.asString();
        for (MoreLinkClickValue.Action action : MoreLinkClickValue.Action.values()) {
            if (action.getClientSideValue().equals(value)) {
                return action;
            }
        }
        return ViewName.of(value);
    }
}
