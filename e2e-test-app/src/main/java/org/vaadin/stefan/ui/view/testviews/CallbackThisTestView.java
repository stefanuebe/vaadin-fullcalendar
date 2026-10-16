package org.vaadin.stefan.ui.view.testviews;

import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarParts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.util.Map;

/**
 * Test view for the {@code this} of function callbacks.
 * <p>
 * Every callback records whether {@code this} is the calendar object of its own calendar in
 * {@code window.cbThis[name]}. Calendar "before" gets its callbacks before it is attached. Calendar "after" gets
 * them on a click of the button. The arrow function records its lexical {@code this}. The validRange function
 * records its {@code this} itself, because the library calls it while the calendar is created.
 * <p>
 * Route: /test/callback-this
 */
@Route(value = "callback-this", layout = TestLayout.class)
@MenuItem(label = "Callback this")
public class CallbackThisTestView extends VerticalLayout {

    public CallbackThisTestView() {
        setSizeFull();
        setPadding(true);
        add(new H2("Callback this"));

        FullCalendar before = createCalendar("before");
        before.setOption(Option.ENTRY_DID_MOUNT, recordCalendarThis("before", "mount", "info"));
        before.setOption(Option.SELECT_OVERLAP, recordCalendarThis("before", "selectOverlap", "info"));
        before.setOption(Option.BUTTONS, Map.of("custom", Map.of("text", "Custom",
                "click", recordCalendarThis("before", "button", "ev"))));
        before.setOption(Option.HEADER_TOOLBAR, Map.of(NativeToolbarParts.START, "custom"));
        before.setOption(Option.ENTRY_ALLOW, recordArrowThis("before", "arrow"));
        // called while the calendar is created, so it stores this and the test compares it afterwards
        before.setOption(Option.VALID_RANGE, JsCallback.of(
                "function(nowDate) { (window.cbThis = window.cbThis || {})['before-validRange'] = this; return {}; }"));

        FullCalendar after = createCalendar("after");

        Button setAfter = new Button("Set after attach", e -> {
            after.setOption(Option.ENTRY_DID_MOUNT, recordCalendarThis("after", "mount", "info"));
            after.setOption(Option.SELECT_OVERLAP, recordCalendarThis("after", "selectOverlap", "info"));
        });
        setAfter.setId("set-after");

        add(setAfter, before, after);
    }

    /**
     * A function callback that stores whether {@code this} is the calendar object of the calendar with the given id.
     */
    private static JsCallback recordCalendarThis(String calendarId, String name, String parameter) {
        return record(calendarId, name, "function(" + parameter + ")",
                "this === document.getElementById('cal-" + calendarId + "').calendar");
    }

    /**
     * An arrow function callback that stores whether {@code this} is {@code window}, its lexical {@code this}.
     */
    private static JsCallback recordArrowThis(String calendarId, String name) {
        return record(calendarId, name, "(info) =>", "this === window");
    }

    private static JsCallback record(String calendarId, String name, String signature, String condition) {
        return JsCallback.of(signature + " { (window.cbThis = window.cbThis || {})['" + calendarId + "-" + name
                + "'] = " + condition + "; }");
    }

    private FullCalendar createCalendar(String id) {
        FullCalendar calendar = new FullCalendar();
        calendar.setId("cal-" + id);
        calendar.setHeight("250px");
        calendar.setOption(Option.INITIAL_DATE, LocalDate.of(2025, 3, 1));
        calendar.setOption(Option.INITIAL_VIEW, CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());

        Entry entry = new Entry();
        entry.setTitle("Entry " + id);
        entry.setStart(LocalDate.of(2025, 3, 10).atTime(9, 0));
        entry.setEnd(LocalDate.of(2025, 3, 10).atTime(10, 0));
        calendar.setEntryProvider(EntryProvider.inMemoryFrom(entry));

        return calendar;
    }
}
