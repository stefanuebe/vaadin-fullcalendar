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
package org.vaadin.stefan.fullcalendar.option;

import java.io.Serializable;
import java.util.Objects;

/**
 * The value of a render hook that sets the content of an element: a text, HTML, or a {@link JsCallback} that returns
 * it. Render hooks such as {@code allDayHeaderContent} take it. A {@code switch} over it needs no {@code default}
 * branch.
 * <p>
 * A sealed type rather than one Java type, because the library takes a value or a function, and a text as well as
 * HTML.
 * <p>
 * The library also takes {@code {domNodes: [...]}}. DOM nodes cannot come from the server, so that form is left out;
 * a {@link JsCallback} can return them.
 */
public sealed interface ContentValue extends Serializable permits TextContent, ContentValue.Html, JsCallback {

    /**
     * Creates the content from HTML. The library sets it as the inner HTML of the element without escaping it, so it
     * must not contain unchecked user input. For a text, use {@link TextContent#of(String)}.
     *
     * @param html the HTML
     * @return the content
     * @throws NullPointerException when null is passed
     */
    static ContentValue ofHtml(String html) {
        return new Html(html);
    }

    /**
     * HTML as the content of an element, the library's {@code {html: '...'}}. It is not escaped.
     *
     * @param html the HTML
     */
    record Html(String html) implements ContentValue {

        public Html {
            Objects.requireNonNull(html);
        }
    }
}
