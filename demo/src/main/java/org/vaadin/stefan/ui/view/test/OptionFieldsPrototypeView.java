package org.vaadin.stefan.ui.view.test;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.BusinessHours;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsCallback;
import org.vaadin.stefan.fullcalendar.Option;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.prototype.OptionFieldsPrototypeCalendar;
import org.vaadin.stefan.fullcalendar.prototype.ViewOptions;
import org.vaadin.stefan.ui.layouts.MainLayout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Locale;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. Click through the options-as-fields prototype.
 * <p>
 * Route: /option-fields-prototype
 */
@Route(value = "option-fields-prototype", layout = MainLayout.class)
public class OptionFieldsPrototypeView extends VerticalLayout {

    private final Div calendarHolder = new Div();
    private final Pre state = new Pre();
    private final Pre sent = new Pre();
    private OptionFieldsPrototypeCalendar calendar;
    private ViewOptions storedMonthOptions;

    public OptionFieldsPrototypeView() {
        setSizeFull();
        add(new H2("PROTOTYPE: options as fields"));
        add(new Paragraph("Question: do typed fields with Lombok accessors, synced as a diff before each response, "
                + "work for every kind of option value? Each button calls a typed setter. 'Java state' shows the "
                + "getters, 'Sent to the client' shows what the diff sent for that click."));

        FlexLayout buttons = new FlexLayout(
                button("allDayText = 'Whole day'", () -> calendar.setAllDayText("Whole day")),
                button("dayMaxEntryRows = 2", () -> calendar.setDayMaxEntryRows(2)),
                button("month view: own options (rows 1, allDayText 'Month')", () -> {
                    storedMonthOptions = ViewOptions.builder().dayMaxEntryRows(1).allDayText("Month").build();
                    calendar.setViewOptions(CalendarViewImpl.DAY_GRID_MONTH, storedMonthOptions);
                }),
                button("change the passed object afterwards: rows = 3", () -> storedMonthOptions.setDayMaxEntryRows(3)),
                button("month view: rows = 2 via getViewOptions + toBuilder", () -> calendar.setViewOptions(CalendarViewImpl.DAY_GRID_MONTH,
                        calendar.getViewOptions(CalendarViewImpl.DAY_GRID_MONTH).toBuilder().dayMaxEntryRows(2).build())),
                button("week + day view: same options object", () -> {
                    ViewOptions timeGrid = ViewOptions.builder().allDayText("Time grid").build();
                    calendar.setViewOptions(CalendarViewImpl.TIME_GRID_WEEK, timeGrid);
                    calendar.setViewOptions(CalendarViewImpl.TIME_GRID_DAY, timeGrid);
                }),
                button("month view: remove options", () -> calendar.setViewOptions(CalendarViewImpl.DAY_GRID_MONTH, null)),
                button("editable = false", () -> calendar.setEditable(false)),
                button("remove editable", () -> calendar.setEditable(null)),
                button("firstDay = Monday", () -> calendar.setFirstDay(DayOfWeek.MONDAY)),
                button("firstDay = Wednesday", () -> calendar.setFirstDay(DayOfWeek.WEDNESDAY)),
                button("businessHours = Mon-Fri 9-17", () -> calendar.setBusinessHours(
                        BusinessHours.businessWeek().start(LocalTime.of(9, 0)).end(LocalTime.of(17, 0)))),
                button("businessHours = Mon-Fri 9-17 + Sat 10-14", () -> calendar.setBusinessHours(
                        BusinessHours.businessWeek().start(9).end(17),
                        BusinessHours.of(DayOfWeek.SATURDAY).start(10).end(14))),
                button("remove businessHours", () -> calendar.setBusinessHours()),
                button("dayCellClass = 'proto-cell'", () -> calendar.setDayCellClass("proto-cell")),
                button("dayCellClass callback: today-cell", () -> calendar.setDayCellClassCallback(
                        JsCallback.of("function(info) { return info.isToday ? 'proto-today' : ''; }"))),
                button("remove dayCellClass callback", () -> calendar.setDayCellClassCallback(null)),
                button("locale = German", () -> calendar.setCalendarLocale(Locale.GERMAN)),
                button("week view", () -> calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK)),
                button("month view", () -> calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH)),
                button("detach + attach", () -> {
                    calendarHolder.remove(calendar);
                    calendarHolder.add(calendar);
                }),
                button("setRawOption(\"editable\", false)", () -> calendar.setRawOption("editable", false)),
                button("setRawOption(\"editable\", null)", () -> calendar.setRawOption("editable", null)),
                button("setRawOption(\"firstDay\", 5)", () -> calendar.setRawOption("firstDay", 5)),
                button("setRawOption(\"firstDay\", null)", () -> calendar.setRawOption("firstDay", null)),
                button("reset (new calendar)", this::reset));
        buttons.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        buttons.getStyle().set("gap", "4px");
        add(buttons);

        VerticalLayout stateBox = new VerticalLayout(new H4("Java state (getters)"), state);
        VerticalLayout sentBox = new VerticalLayout(new H4("Sent to the client"), sent);
        sent.getElement().setAttribute("data-testid", "sent");
        state.getElement().setAttribute("data-testid", "state");
        HorizontalLayout panels = new HorizontalLayout(stateBox, sentBox);
        panels.setWidthFull();
        add(panels);


        calendarHolder.setWidthFull();
        calendarHolder.setHeight("600px");
        add(calendarHolder);
        reset();
    }

    private void reset() {
        if (calendar != null) {
            calendarHolder.remove(calendar);
        }
        storedMonthOptions = null;
        calendar = new OptionFieldsPrototypeCalendar();
        calendar.getElement().setAttribute("data-testid", "calendar");
        calendar.setOption(Option.INITIAL_DATE, LocalDate.of(2025, 3, 1));
        calendar.setSyncLog(sent::setText);

        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        for (int i = 1; i <= 5; i++) {
            Entry e = new Entry();
            e.setTitle("Entry " + i);
            e.setStart(LocalDate.of(2025, 3, 5));
            e.setAllDay(true);
            provider.addEntry(e);
        }
        calendar.setEntryProvider(provider);

        calendarHolder.add(calendar);
        showState();
    }

    private Button button(String label, Runnable action) {
        return new Button(label, e -> {
            sent.setText("(no sync ran)");
            action.run();
            showState();
        });
    }

    private void showState() {
        state.setText(String.join("\n",
                "allDayText           = " + calendar.getAllDayText(),
                "dayMaxEntryRows      = " + calendar.getDayMaxEntryRows(),
                "month view options   = rows " + calendar.getViewOptions(CalendarViewImpl.DAY_GRID_MONTH).getDayMaxEntryRows()
                        + ", allDayText " + calendar.getViewOptions(CalendarViewImpl.DAY_GRID_MONTH).getAllDayText(),
                "passed month object  = " + (storedMonthOptions == null ? null : "rows " + storedMonthOptions.getDayMaxEntryRows()),
                "editable             = " + calendar.getEditable(),
                "firstDay             = " + calendar.getFirstDay(),
                "businessHours        = " + (calendar.getBusinessHours() == null ? null : Arrays.toString(calendar.getBusinessHours())),
                "dayCellClass         = " + calendar.getDayCellClass(),
                "dayCellClassCallback = " + calendar.getDayCellClassCallback(),
                "calendarLocale       = " + calendar.getCalendarLocale(),
                "raw editable         = " + calendar.getRawOption("editable").orElse(null),
                "raw firstDay         = " + calendar.getRawOption("firstDay").orElse(null)));
    }
}
