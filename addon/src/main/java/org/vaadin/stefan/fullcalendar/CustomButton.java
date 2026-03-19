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
package org.vaadin.stefan.fullcalendar;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import tools.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * Represents a custom button that can be added to the FullCalendar header or footer toolbar.
 * <p>
 * Once created, add the button to the calendar and register a click listener:
 * <pre>{@code
 * CustomButton btn = new CustomButton("myButton");
 * btn.setText("Schedule");
 * btn.setHint("Open scheduling wizard");
 * calendar.addCustomButton(btn, event -> openSchedulingWizard());
 * }</pre>
 * <p>
 * To make the button appear in the toolbar, include the button name in the header or footer
 * toolbar configuration (e.g., {@code "today prev,next myButton"}).
 *
 * @see FullCalendar#addCustomButton(CustomButton)
 * @see FullCalendar#addCustomButton(CustomButton, com.vaadin.flow.component.ComponentEventListener)
 * @see <a href="https://fullcalendar.io/docs/customButtons">FC customButtons documentation</a>
 */
@Getter
@Setter
public class CustomButton {

    /**
     * Unique identifier for this button. Used as the key in the FullCalendar {@code customButtons}
     * option and when referencing the button in header/footer toolbar strings.
     */
    @NonNull
    private final String name;

    /**
     * The label text displayed on the button. If both {@code text} and {@code icon} are set,
     * FC renders the icon; the text is used as a fallback and for accessibility.
     */
    private String text;

    /**
     * Accessible label (aria-label / tooltip) for the button. Helps screen-reader users
     * understand the button's purpose.
     */
    private String hint;

    /**
     * CSS class name of an icon to display inside the button (e.g., from Font Awesome:
     * {@code "fa-calendar-plus"}). FullCalendar adds the class to a {@code <span>} inside
     * the button.
     */
    private String icon;

    /**
     * Bootstrap or Font Awesome icon name used when Bootstrap integration is active.
     * Alternative to {@link #icon} for Bootstrap-themed calendars.
     */
    private String bootstrapFontAwesome;

    /**
     * Vaadin Lumo icon name (e.g., {@code "chevron-right"}). Used when the calendar is styled
     * with Lumo and a Vaadin icon is preferred.
     */
    private String themeIcon;

    /**
     * Creates a new custom button with the given unique name.
     *
     * @param name unique button identifier; must not be null
     */
    public CustomButton(@NonNull String name) {
        this.name = name;
    }

    /**
     * Serialises this button's properties (without the click handler) to a JSON object.
     * The click handler is injected on the client side by the FullCalendar TypeScript companion.
     *
     * @return JSON representation of this button
     */
    ObjectNode toJson() {
        ObjectNode node = JsonFactory.createObject();
        if (text != null) node.put("text", text);
        if (hint != null) node.put("hint", hint);
        if (icon != null) node.put("icon", icon);
        if (bootstrapFontAwesome != null) node.put("bootstrapFontAwesome", bootstrapFontAwesome);
        if (themeIcon != null) node.put("themeIcon", themeIcon);
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomButton)) return false;
        CustomButton that = (CustomButton) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "CustomButton{name='" + name + "', text='" + text + "'}";
    }
}
