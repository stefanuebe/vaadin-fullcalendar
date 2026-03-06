package org.vaadin.stefan.ui.view.demos.multimonthselection;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarItemProviderToolbar;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

/**
 * Demonstrates cross-month selection in the Multi Month view.
 * This feature uses a custom JavaScript integration for selecting date ranges
 * across month boundaries.
 *
 * @author Stefan Uebe
 */
@Route(value = "multi-month-cross-month-selection", layout = MainLayout.class)
@MenuItem(label = "Multi Month Cross Selection")
@JsModule("./multi-month-selection-utils.js")
public class MultiMonthCrossMonthSelectionDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        EntryService<Entry> service = EntryService.createSimpleInstance();

        // Use a CallbackEntryProvider to supply entries from the service
        var provider = EntryProvider.fromCallbacks(
                query -> service.streamEntries(query),
                id -> service.getEntry(id).orElse(null)
        );

        FullCalendar<Entry> calendar = FullCalendarBuilder.<Entry>create()
                .withEntryProvider(provider)
                .withCalendarItemLimit(3)
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setSizeFull();

        // Cross-month selection requires timeslot selection to be disabled
        calendar.setTimeslotsSelectable(false);

        // Register the custom JS utility and switch to the multi-month view on attach
        calendar.addAttachListener(event ->
                calendar.getElement()
                        .executeJs("window.Vaadin.Flow.multiMonthCrossSelectionUtils.register(this.calendar)")
                        .then(jsonValue -> calendar.changeView(CalendarViewImpl.MULTI_MONTH))
        );

        return calendar;
    }

    @Override
    protected Component createToolbar() {
        // CalendarItemProviderToolbar: simpler toolbar without CRUD —
        // view switching is not useful here since this demo is multi-month only
        return CalendarItemProviderToolbar.builder()
                .calendar(getCalendar())
                .dateChangeable(true)
                .viewChangeable(false)
                .settingsAvailable(true)
                .build();
    }
    // DEMO-END

    @Override
    protected String createDescription() {
        return "Demonstrates cross-month selection in the Multi Month view. This feature uses a custom JavaScript integration for selecting date ranges across month boundaries.";
    }
}
