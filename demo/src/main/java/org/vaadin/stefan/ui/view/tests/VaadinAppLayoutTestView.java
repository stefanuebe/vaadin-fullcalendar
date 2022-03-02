package org.vaadin.stefan.ui.view.tests;

import com.vaadin.flow.router.Route;
import elemental.json.JsonObject;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.ui.view.AbstractCalendarView;

/**
 * @author Stefan Uebe
 */
@Route(value = "test-vaadin-app-layout", layout = VaadinAppLayoutTestLayout.class)
public class VaadinAppLayoutTestView extends AbstractCalendarView {

    @Override
    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
        FullCalendar calendar = FullCalendarBuilder.create().withInitialOptions(defaultInitialOptions).withEntryLimit(3).build();

        return calendar;
    }
}
