package org.vaadin.stefan.fullcalendar.prototype;

import com.vaadin.flow.component.AttachEvent;
import lombok.experimental.Delegate;
import org.vaadin.stefan.fullcalendar.CalendarView;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. A calendar whose options are fields. Lombok's {@code @Delegate}
 * puts the accessors of {@link CalendarOptions} (global) and {@link ViewOptions} (for all views) on the calendar.
 * {@link OptionFieldsSync} sends only the changes before each response.
 */
public class OptionFieldsPrototypeCalendar extends FullCalendar {

    private final OptionFieldsSync sync = new OptionFieldsSync(this,
            (key, value) -> setOption(key, value, null, List.of()), new CalendarOptions(), ViewOptions::new);

    @Delegate(excludes = NotDelegated.class)
    private CalendarOptions options() {
        return sync.options();
    }

    @Delegate(excludes = NotDelegated.class)
    private ViewOptions viewDefaults() {
        return sync.viewDefaults();
    }

    /**
     * Sets the options for one view type only. A value set here overrides the calendar value while that view is
     * shown. The calendar keeps a copy, so later changes to the passed instance have no effect. Null removes the
     * options of the view.
     */
    public void setViewOptions(CalendarView view, ViewOptions viewOptions) {
        sync.setViewOptions(view, viewOptions);
    }

    /**
     * Returns a copy of the options of one view type, or empty options when none are set. Changes to it have no
     * effect until it is set again.
     */
    public ViewOptions getViewOptions(CalendarView view) {
        return sync.getViewOptions(view);
    }

    /**
     * Sets an option by its name in the library, for an option the add-on has no field for. Advanced use only: the
     * value is passed on as it is, without conversion, and it always wins over a field with the same name. The
     * field's getter still returns the field value. Null removes the raw option, then the field applies again.
     */
    public void setRawOption(String option, Object value) {
        sync.setRawOption(option, value);
    }

    /**
     * Returns the value set with {@link #setRawOption(String, Object)}, or empty.
     */
    public Optional<Object> getRawOption(String option) {
        return sync.getRawOption(option);
    }

    public void setSyncLog(Consumer<String> syncLog) {
        sync.setSyncLog(syncLog);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (!attachEvent.isInitialAttach()) {
            sync.resendAllOnNextSync();
        }
    }

    /** Methods of the options classes that must not appear on the calendar. */
    private interface NotDelegated {
        ViewOptions.ViewOptionsBuilder<?, ?> toBuilder();
    }
}
