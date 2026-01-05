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
     * Applies Vaadin styles (Lumo / Aura) to the calendar to align it more with the theming of other Vaadin components.
     */
    VAADIN("vaadin");

    private final String variantName;

    FullCalendarVariant(String variantName) {
        this.variantName = variantName;
    }


}