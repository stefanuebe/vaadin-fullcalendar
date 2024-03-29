package org.vaadin.stefan.ui.view.demos.multimonthselection;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.demos.entryproviders.CallbackEntryProviderDemo;

/**
 * @author Stefan Uebe
 */
@Route(value = "multi-month-cross-month-selection", layout = MainLayout.class)
@MenuItem(label = "Multi Month Cross Selection")
@JsModule("./multi-month-selection-utils.js")
public class MultiMonthCrossMonthSelectionDemo extends CallbackEntryProviderDemo {

    @Override
    protected boolean isToolbarViewChangeable() {
        return false;
    }

    @Override
    protected void postConstruct(FullCalendar calendar) {
        calendar.setTimeslotsSelectable(false); // important

        calendar.addAttachListener(event -> // refire things, when reattached
            calendar.getElement()
                .executeJs("window.Vaadin.Flow.multiMonthCrossSelectionUtils.register(this.calendar)")
                .then(jsonValue -> calendar.changeView(CalendarViewImpl.MULTI_MONTH))
        );
    }

    @Override
    protected String createTitle() {
        return "Multi Month View - Cross Month Selection";
    }

    @Override
    protected String createDescription() {
        return "This demo shows a sample on how to implement cross month selection for the multi month view. This feature is currently not available" +
                " in the library itself and thus integrated via a custom java script";
    }
}
