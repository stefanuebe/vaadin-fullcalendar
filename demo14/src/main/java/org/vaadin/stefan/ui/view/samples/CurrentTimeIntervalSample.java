package org.vaadin.stefan.ui.view.samples;

import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import org.vaadin.stefan.fullcalendar.CalendarView;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Stefan Uebe
 */
public class CurrentTimeIntervalSample extends AbstractSample {

    @Override
    protected void buildSample(FullCalendar calendar) {
        // The element that should show the current interval.
        HasText intervalLabel = new Span();

        // combo box to select a view for the calendar, like "monthly", "weekly", ...
        ComboBox<CalendarView> viewBox = new ComboBox<>("", CalendarViewImpl.values());
        viewBox.addValueChangeListener(e -> {
            CalendarView value = e.getValue();
            calendar.changeView(value == null ? CalendarViewImpl.DAY_GRID_MONTH : value);
        });
        viewBox.setValue(CalendarViewImpl.DAY_GRID_MONTH);

        /*
         * The view rendered listener is called when the view has been rendererd on client side
         * and FC is aware of the current shown interval. Might be accessible more directly in
         * future.
         */
        calendar.addDatesRenderedListener(event -> {
            LocalDate intervalStart = event.getIntervalStart();
            CalendarView cView = viewBox.getValue();

            String formattedInterval = intervalStart.toString(); // format the intervalStart based on cView. See the demos for examples.

            intervalLabel.setText(formattedInterval);
        });
    }
}
