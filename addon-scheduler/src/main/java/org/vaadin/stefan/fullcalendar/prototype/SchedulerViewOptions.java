package org.vaadin.stefan.fullcalendar.prototype;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. The scheduler options that can apply per view, on top of the
 * options of {@link ViewOptions}.
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class SchedulerViewOptions extends ViewOptions {

    // FC-UPDATE: check the scheduler options of the new FullCalendar version against these fields, see the note in
    // ViewOptions.

    /**
     * Minimum width of a time slot in timeline views, in pixels.
     *
     * @see <a href="https://fullcalendar.io/docs/slotMinWidth">slotMinWidth</a>
     */
    private Integer slotMinWidth;

    /**
     * Width of the resource columns, as a CSS width such as {@code "200px"} or {@code "25%"}.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceColumnsWidth">resourceColumnsWidth</a>
     */
    private String resourceColumnsWidth;
}
