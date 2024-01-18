package org.vaadin.stefan.fullcalendar;

import lombok.Getter;

/**
 * Themevariants for the {@link FullCalendar}. Use {@link FullCalendar#addThemeVariants(FullCalendarVariant...)} to
 * apply them.
 *
 */
@Getter
public enum FullCalendarVariant {
    /**
     * An experimental theme variant, that applies lumo styles to the calendar to align it more with the
     * default styling of other Vaadin components.
     */
    LUMO("lumo");

    private final String variantName;

    FullCalendarVariant(String variantName) {
        this.variantName = variantName;
    }


}