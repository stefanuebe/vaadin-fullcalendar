package org.vaadin.stefan.ui.view.testviews;

import org.vaadin.stefan.fullcalendar.option.JsCallback;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * Test view for native entry event listeners combined with an entryDidMount callback.
 * <p>
 * Verifies:
 * <ul>
 *   <li>The entryDidMount callback fires and sets {@code data-did-mount="true"} on the entry element</li>
 *   <li>A native click listener registered via {@code addEntryNativeEventListener} fires on click
 *       and increments a visible counter</li>
 *   <li>Both the user callback and the native listener work when the callback is set via
 *       {@code options().setEntryDidMount(…)}, which the calendar merges with the native listener</li>
 * </ul>
 * <p>
 * Route: /test/native-event-listener
 */
@Route(value = "native-event-listener", layout = TestLayout.class)
@MenuItem(label = "Native Event Listener")
public class NativeEventListenerTestView extends VerticalLayout {

    public NativeEventListenerTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Native Entry Event Listener"));
        add(new Paragraph(
                "The entry below has data-did-mount='true' set by an ENTRY_DID_MOUNT callback. " +
                "Clicking it increments the counter via a native click listener."));

        Span clickCounter = new Span("0");
        clickCounter.setId("click-count");
        add(clickCounter);

        FullCalendar calendar = new FullCalendar();

        // Fix date for reproducible tests
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 10));
        calendar.options().setInitialView(CalendarViewImpl.DAY_GRID_MONTH);

        Entry entry = new Entry();
        entry.setTitle("Click Me");
        entry.setStart(LocalDate.of(2025, 3, 10).atStartOfDay());
        entry.setAllDay(true);
        calendar.setEntryProvider(EntryProvider.inMemoryFrom(entry));

        // a function with a body in braces, so the calendar can merge the native listener into it
        calendar.options().setEntryDidMount(
                JsCallback.of("function(info) { info.el.setAttribute('data-did-mount', 'true'); }"));

        // Native click listener — increments the visible counter element
        calendar.addEntryNativeEventListener("click",
                "e => { var el = document.getElementById('click-count'); " +
                "el.textContent = String(parseInt(el.textContent || '0') + 1); }");

        add(calendar);
        setFlexGrow(1, calendar);
    }
}
