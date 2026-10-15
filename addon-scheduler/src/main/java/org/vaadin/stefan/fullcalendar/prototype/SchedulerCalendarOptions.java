package org.vaadin.stefan.fullcalendar.prototype;

import lombok.Getter;
import lombok.Setter;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. The scheduler options that apply to the whole calendar only, on top
 * of the options of {@link CalendarOptions}.
 */
@Getter
@Setter
public class SchedulerCalendarOptions extends CalendarOptions {

    // FC-UPDATE: check the global scheduler options of the new FullCalendar version against these fields, see the
    // note in ViewOptions.

    /**
     * The order of the resources, for example {@code "title"} or {@code "-type1,type2"}.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceOrder">resourceOrder</a>
     */
    private String resourceOrder;
}
