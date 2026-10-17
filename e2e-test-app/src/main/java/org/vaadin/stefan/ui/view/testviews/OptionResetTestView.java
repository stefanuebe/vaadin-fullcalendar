package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

/**
 * Test view for removing options of an attached calendar with {@code options().setXxx(null)}.
 * <p>
 * The calendar shows the week of 2025-03-03 in German, with an all-day entry and a timed entry. Its entries come from
 * a callback entry provider. The span "fetch-count" shows how often it was asked for entries.
 * <p>
 * Its initial JSON options set at most one entry row per day, so the month view shows a "+N more" link on 2025-03-06,
 * and "day" as the action of that link. They also set an eventDataTransform callback, at
 * top level and for the listDay view. The code of both callbacks counts how often it is evaluated in
 * {@code window.optionResetJsonEvaluations}. For the week view they set long weekday names in the day headers.
 * <p>
 * Before attach, the server sets 20:00 as the end of the time slots of the week view, so the week view has
 * view-specific options from both the initial JSON options and the server.
 * <p>
 * Buttons set and remove these options:
 * <ul>
 *   <li>the entry color, also while the calendar is detached,</li>
 *   <li>the entry text color, which the initial JSON options do not set,</li>
 *   <li>the all-day text,</li>
 *   <li>the display of the entry time, which has no default in FullCalendar,</li>
 *   <li>the time zone, also while the calendar is detached,</li>
 *   <li>a viewDidMount callback,</li>
 *   <li>an eventDidMount callback, removed while the calendar is detached together with the add-on's own
 *   eventDidMount code for entry ids, so no callback remains.</li>
 *   <li>the action of the "+N more" link,</li>
 *   <li>the view-specific all-day text and entry color of the week view.</li>
 * </ul>
 * <p>
 * Further buttons set the all-day text to the text of the German locale, switch to the English locale, set an
 * eventAdd callback that counts its calls in {@code window.optionResetEventAdds}, show the month or the week view,
 * attach the calendar again in one request, and detach and attach it in separate requests.
 * <p>
 * The span "view-rendered-count" counts how often the server was told that a view was rendered.
 * <p>
 * Route: /test/option-reset
 */
@Route(value = "option-reset", layout = TestLayout.class)
@MenuItem(label = "Option Reset")
public class OptionResetTestView extends VerticalLayout {

    private static final Timezone NEW_YORK = new Timezone(ZoneId.of("America/New_York"));

    private int fetchCount;
    private int viewRenderedCount;

    public OptionResetTestView() {
        setSizeFull();
        add(new H2("Option Reset"));

        Entry allDay = new Entry("all-day");
        allDay.setTitle("All-day entry");
        allDay.setStart(LocalDate.of(2025, 3, 4).atStartOfDay());
        allDay.setAllDay(true);

        Entry timed = new Entry("timed");
        timed.setTitle("Timed entry");
        timed.setStart(LocalDateTime.of(2025, 3, 5, 10, 0));
        timed.setEnd(LocalDateTime.of(2025, 3, 5, 11, 0));

        // two all-day entries on one day, so a "+N more" link shows in the month view
        Entry extra1 = new Entry("extra-1");
        extra1.setTitle("Extra entry 1");
        extra1.setStart(LocalDate.of(2025, 3, 6).atStartOfDay());
        extra1.setAllDay(true);
        Entry extra2 = new Entry("extra-2");
        extra2.setTitle("Extra entry 2");
        extra2.setStart(LocalDate.of(2025, 3, 6).atStartOfDay());
        extra2.setAllDay(true);

        List<Entry> entries = List.of(allDay, timed, extra1, extra2);
        Span fetchCountSpan = new Span("0");
        fetchCountSpan.setId("fetch-count");
        Span viewRenderedCountSpan = new Span("0");
        viewRenderedCountSpan.setId("view-rendered-count");

        ObjectNode initialOptions = JsonFactory.createObject();
        initialOptions.put("dayMaxEventRows", 1);
        initialOptions.put("moreLinkClick", "day");
        initialOptions.set("eventDataTransform", JsCallback.of("""
                (() => {
                    window.optionResetJsonEvaluations = (window.optionResetJsonEvaluations || 0) + 1;
                    return function(data) { return data; };
                })()""").toMarkerJson());
        ObjectNode listDayOptions = JsonFactory.createObject();
        listDayOptions.set("eventDataTransform", initialOptions.get("eventDataTransform").deepCopy());
        ObjectNode views = JsonFactory.createObject();
        views.set("listDay", listDayOptions);
        ObjectNode weekOptions = JsonFactory.createObject();
        weekOptions.set("dayHeaderFormat", JsonFactory.createObject().put("weekday", "long"));
        views.set("timeGridWeek", weekOptions);
        initialOptions.set("views", views);
        FullCalendar calendar = new FullCalendar(initialOptions);
        calendar.setId("cal-option-reset");
        calendar.options().setLocale(Locale.GERMAN);
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 3));
        calendar.options().setInitialView(CalendarViewImpl.TIME_GRID_WEEK);
        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setSlotMaxTime("20:00:00");
        calendar.setEntryProvider(EntryProvider.fromCallbacks(query -> {
            fetchCountSpan.setText(String.valueOf(++fetchCount));
            return entries.stream();
        }, id -> entries.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null)));

        calendar.addViewSkeletonRenderedListener(
                e -> viewRenderedCountSpan.setText(String.valueOf(++viewRenderedCount)));

        HorizontalLayout buttons = new HorizontalLayout(
                new Button("Set entry color", e -> calendar.options().setEntryColor("red")),
                new Button("Remove entry color", e -> calendar.options().setEntryColor(null)),
                new Button("Set entry color while detached",
                        e -> attachAgain(calendar, () -> calendar.options().setEntryColor("orange"))),
                new Button("Remove entry color while detached",
                        e -> attachAgain(calendar, () -> calendar.options().setEntryColor(null))),
                new Button("Set entry text color", e -> calendar.options().setEntryContrastColor("yellow")),
                new Button("Remove entry text color", e -> calendar.options().setEntryContrastColor(null)),
                new Button("Set all-day text", e -> calendar.options().setAllDayText("Custom")),
                new Button("Remove all-day text", e -> calendar.options().setAllDayText(null)),
                new Button("Set locale all-day text", e -> calendar.options().setAllDayText("Ganztägig")),
                new Button("Set English locale", e -> calendar.options().setLocale(Locale.ENGLISH)),
                new Button("Hide entry time", e -> calendar.options().setDisplayEntryTime(false)),
                new Button("Remove entry time display", e -> calendar.options().setDisplayEntryTime(null)),
                // no typed field exists for eventAdd, because the add-on itself never uses it
                new Button("Set entry add callback", e -> calendar.options().setRawOption("eventAdd", JsCallback.of(
                        "function() { window.optionResetEventAdds = (window.optionResetEventAdds || 0) + 1; }"))),
                new Button("Set time zone", e -> calendar.options().setTimeZone(NEW_YORK)),
                new Button("Remove time zone", e -> calendar.options().setTimeZone(Timezone.LOCAL)),
                new Button("Set time zone while detached",
                        e -> attachAgain(calendar, () -> calendar.options().setTimeZone(NEW_YORK))),
                new Button("Set week entry color", e -> calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setEntryColor("blue")),
                new Button("Set week all-day text", e -> calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setAllDayText("Week")),
                new Button("Remove week all-day text", e -> calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setAllDayText(null)),
                new Button("Set view did mount", e -> calendar.options().setViewDidMount(
                        JsCallback.of("function() {}"))),
                new Button("Remove view did mount", e -> calendar.options().setViewDidMount(null)),
                // moreLinkClick of the initial JSON is a raw option, so these change the raw option
                new Button("Set more link popover", e -> calendar.options().setRawOption("moreLinkClick", "popover")),
                new Button("Remove more link action", e -> calendar.options().setRawOption("moreLinkClick", null)),
                new Button("Set entry did mount", e -> calendar.options().setEntryDidMount(
                        JsCallback.of("function(info) { info.el.dataset.mounted = 'yes'; }"))),
                // also without the add-on's own eventDidMount code for entry ids, so no eventDidMount callback remains
                new Button("Remove entry did mount while detached", e -> attachAgain(calendar, () -> {
                    calendar.setAutoProvideEntryIdOnClient(false);
                    calendar.options().setEntryDidMount(null);
                })),
                new Button("Show month", e -> calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH)),
                new Button("Show week", e -> calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK)),
                new Button("Detach", e -> remove(calendar)),
                new Button("Attach", e -> add(calendar)),
                new Button("Attach again", e -> attachAgain(calendar, () -> {})),
                new Span("Fetches:"), fetchCountSpan,
                new Span("View renders:"), viewRenderedCountSpan);

        add(buttons, calendar);
        setFlexGrow(1, calendar);
        setHorizontalComponentAlignment(Alignment.STRETCH, calendar);
    }

    /** Detaches and attaches the calendar in one request, and runs the given code while it is detached. */
    private void attachAgain(FullCalendar calendar, Runnable whileDetached) {
        int index = indexOf(calendar);
        remove(calendar);
        whileDetached.run();
        addComponentAtIndex(index, calendar);
    }
}
