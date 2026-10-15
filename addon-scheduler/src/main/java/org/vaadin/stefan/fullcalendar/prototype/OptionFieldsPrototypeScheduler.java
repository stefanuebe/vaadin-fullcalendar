package org.vaadin.stefan.fullcalendar.prototype;

import com.vaadin.flow.component.AttachEvent;
import lombok.experimental.Delegate;
import org.vaadin.stefan.fullcalendar.CalendarView;
import org.vaadin.stefan.fullcalendar.FullCalendarScheduler;

import java.util.List;
import java.util.function.Consumer;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. The scheduler with options as fields. Its option classes extend
 * those of the core calendar, so the delegates bring both the core and the scheduler accessors.
 */
public class OptionFieldsPrototypeScheduler extends FullCalendarScheduler {

    private final OptionFieldsSync sync = new OptionFieldsSync(this,
            (key, value) -> setOption(key, value, null, List.of()), new SchedulerCalendarOptions(),
            SchedulerViewOptions::new);

    @Delegate(excludes = NotDelegated.class)
    private SchedulerCalendarOptions options() {
        return (SchedulerCalendarOptions) sync.options();
    }

    @Delegate(excludes = NotDelegated.class)
    private SchedulerViewOptions viewDefaults() {
        return (SchedulerViewOptions) sync.viewDefaults();
    }

    public void setViewOptions(CalendarView view, SchedulerViewOptions viewOptions) {
        sync.setViewOptions(view, viewOptions);
    }

    public SchedulerViewOptions getViewOptions(CalendarView view) {
        return (SchedulerViewOptions) sync.getViewOptions(view);
    }

    public void setRawOption(String option, Object value) {
        sync.setRawOption(option, value);
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

    private interface NotDelegated {
        SchedulerViewOptions.SchedulerViewOptionsBuilder<?, ?> toBuilder();
    }
}
