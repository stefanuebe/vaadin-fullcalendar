package org.vaadin.stefan.fullcalendar;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. Fixed CSS class names, separated by spaces.
 *
 * @param names the class names
 */
public record ClassName(String names) implements ClassNameValue {

    public static ClassName of(String names) {
        return new ClassName(names);
    }
}
