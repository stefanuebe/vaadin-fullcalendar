package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * The calendar merges the entry id, the user's entryDidMount callback and the native entry listeners into one
 * function, whatever form the callback has. One calendar per form:
 * <ul>
 *   <li>an arrow function with a block body</li>
 *   <li>an arrow function with an expression body, plus a native click listener</li>
 *   <li>a function that overrides the entry id</li>
 * </ul>
 * <p>
 * Route: /test/entry-did-mount-forms
 */
@Route(value = "entry-did-mount-forms", layout = TestLayout.class)
@MenuItem(label = "Entry Did Mount Forms")
public class EntryDidMountFormsTestView extends VerticalLayout {

    private static final LocalDate DATE = LocalDate.of(2025, 3, 10);

    public EntryDidMountFormsTestView() {
        setPadding(true);
        add(new H2("entryDidMount in any function form"));

        FullCalendar blockBody = createCalendar("block");
        blockBody.options().setEntryDidMount(JsCallback.of("info => { info.el.dataset.mounted = 'block'; }"));

        Span clickCount = new Span("0");
        clickCount.setId("click-count");
        FullCalendar expressionBody = createCalendar("expression");
        expressionBody.options().setEntryDidMount(JsCallback.of("info => info.el.dataset.mounted = 'expression'"));
        expressionBody.addEntryNativeEventListener("click",
                "e => { var el = document.getElementById('click-count'); "
                        + "el.textContent = String(parseInt(el.textContent) + 1); }");

        FullCalendar override = createCalendar("override");
        override.options().setEntryDidMount(
                JsCallback.of("function(info) { info.el.id = 'custom-' + info.event.id; }"));

        add(blockBody, clickCount, expressionBody, override);
    }

    private static FullCalendar createCalendar(String entryId) {
        FullCalendar calendar = new FullCalendar();
        calendar.setHeight("300px");
        calendar.options().setInitialDate(DATE);
        calendar.options().setInitialView(CalendarViewImpl.DAY_GRID_MONTH);

        Entry entry = new Entry(entryId);
        entry.setTitle(entryId);
        entry.setStart(DATE.atStartOfDay());
        entry.setAllDay(true);
        calendar.setEntryProvider(EntryProvider.inMemoryFrom(entry));

        return calendar;
    }
}
