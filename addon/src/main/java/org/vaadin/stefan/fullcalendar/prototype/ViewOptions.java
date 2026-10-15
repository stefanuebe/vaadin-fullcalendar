package org.vaadin.stefan.fullcalendar.prototype;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.vaadin.stefan.fullcalendar.BusinessHours;
import org.vaadin.stefan.fullcalendar.ClassName;
import org.vaadin.stefan.fullcalendar.ClassNameValue;
import org.vaadin.stefan.fullcalendar.JsCallback;
import org.vaadin.stefan.fullcalendar.converters.BusinessHoursConverter;
import org.vaadin.stefan.fullcalendar.converters.DayOfWeekConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;

import java.io.Serializable;
import java.time.DayOfWeek;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. Options that can apply per view. Null means "not set".
 * The calendar copies an instance when it is set for a view, so later changes to it have no effect there.
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class ViewOptions implements Serializable {

    // FC-UPDATE: check the options of the new FullCalendar version against these fields: new, renamed, removed
    // or retyped options, and options that became view-specific or global. A renamed option changes only @JsonName.

    /**
     * Text for the all-day slot of time grid views.
     */
    private String allDayText;

    /**
     * Maximum number of entry rows per day. Further entries go into a "+N more" link.
     */
    @JsonName("dayMaxEventRows")
    private Integer dayMaxEntryRows;

    /**
     * Whether entries can be dragged and resized.
     */
    private Boolean editable;

    /**
     * The first day of the week.
     */
    @JsonConverter(DayOfWeekConverter.class)
    private DayOfWeek firstDay;

    /**
     * The hours in which someone is available.
     */
    @JsonConverter(BusinessHoursConverter.class)
    private BusinessHours[] businessHours;

    /**
     * CSS classes for day cells: fixed class names or a {@link JsCallback} that returns them.
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellClass;

    /**
     * Sets fixed CSS classes for day cells, separated by spaces. Null removes the option.
     */
    public void setDayCellClass(String classNames) {
        this.dayCellClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets a callback that returns the CSS classes for day cells. Null removes the option.
     */
    public void setDayCellClass(JsCallback callback) {
        this.dayCellClass = callback;
    }

    // ponytail: hand-written, Lombok skips its own setter when one with this name exists
    public void setBusinessHours(BusinessHours... businessHours) {
        this.businessHours = businessHours == null || businessHours.length == 0 ? null : businessHours;
    }
}
