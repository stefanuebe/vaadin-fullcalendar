package org.vaadin.stefan.ui.view.test;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarOptions;
import org.vaadin.stefan.fullcalendar.option.EntryLimitValue;
import org.vaadin.stefan.ui.layouts.MainLayout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Manual check for removing options of an attached calendar (#294). Pick an option, set a value, then remove it
 * with "Unset". The calendar should look as if the option had never been set. The view is not in the menu, so open
 * /option-reset-playground directly.
 */
@Route(value = "option-reset-playground", layout = MainLayout.class)
public class OptionResetPlayground extends VerticalLayout {

    private enum Kind {BOOLEAN, NUMBER, TEXT}

    /** An option by its FullCalendar name, and the typed setter of its field. The setter gets null to remove it. */
    private record OptionChoice(String name, Kind kind, String hint, BiConsumer<FullCalendarOptions, Object> setter) {
    }

    private static final OptionChoice[] CHOICES = {
            new OptionChoice("weekends", Kind.BOOLEAN, "show Saturday and Sunday",
                    (o, v) -> o.setWeekends((Boolean) v)),
            new OptionChoice("weekNumbers", Kind.BOOLEAN, "no default, the library shows no week numbers",
                    (o, v) -> o.setWeekNumbers((Boolean) v)),
            new OptionChoice("displayEventTime", Kind.BOOLEAN, "no default, the view decides",
                    (o, v) -> o.setDisplayEntryTime((Boolean) v)),
            new OptionChoice("displayEventEnd", Kind.BOOLEAN, "no default, the view decides",
                    (o, v) -> o.setDisplayEntryEnd((Boolean) v)),
            new OptionChoice("defaultAllDay", Kind.BOOLEAN, "no default",
                    (o, v) -> o.setDefaultAllDay((Boolean) v)),
            new OptionChoice("editable", Kind.BOOLEAN, "drag and resize entries",
                    (o, v) -> o.setEditable((Boolean) v)),
            new OptionChoice("eventStartEditable", Kind.BOOLEAN, "no default, falls back to editable",
                    (o, v) -> o.setEntryStartEditable((Boolean) v)),
            new OptionChoice("eventDurationEditable", Kind.BOOLEAN, "no default, falls back to editable",
                    (o, v) -> o.setEntryDurationEditable((Boolean) v)),
            new OptionChoice("selectable", Kind.BOOLEAN, "select time spans",
                    (o, v) -> o.setSelectable((Boolean) v)),
            new OptionChoice("navLinks", Kind.BOOLEAN, "day numbers become links",
                    (o, v) -> o.setNavLinks((Boolean) v)),
            new OptionChoice("nowIndicator", Kind.BOOLEAN, "time grid views",
                    (o, v) -> o.setNowIndicator((Boolean) v)),
            new OptionChoice("allDaySlot", Kind.BOOLEAN, "time grid views",
                    (o, v) -> o.setAllDaySlot((Boolean) v)),
            new OptionChoice("dayHeaders", Kind.BOOLEAN, "",
                    (o, v) -> o.setDayHeaders((Boolean) v)),
            new OptionChoice("borderless", Kind.BOOLEAN, "no default",
                    (o, v) -> o.setBorderless((Boolean) v)),
            new OptionChoice("firstDay", Kind.NUMBER, "0 = Sunday, 1 = Monday and so on, no default",
                    (o, v) -> o.setFirstDay(v == null ? null : DayOfWeek.of(((Number) v).intValue() == 0
                            ? 7 : ((Number) v).intValue()))),
            new OptionChoice("dayMaxEvents", Kind.NUMBER, "month view, try 1",
                    (o, v) -> {
                        if (v == null) {
                            o.setDayMaxEntries((EntryLimitValue) null);
                        } else {
                            o.setDayMaxEntries(((Number) v).intValue());
                        }
                    }),
            new OptionChoice("eventMaxStack", Kind.NUMBER, "time grid, no default",
                    (o, v) -> o.setEntryMaxStack(v == null ? null : ((Number) v).intValue())),
            new OptionChoice("aspectRatio", Kind.NUMBER, "e.g. 3",
                    (o, v) -> o.setAspectRatio(v == null ? null : ((Number) v).doubleValue())),
            // no field: the option stays reachable as a raw option
            new OptionChoice("height", Kind.NUMBER, "pixels, no default, raw option",
                    (o, v) -> o.setRawOption("height", v)),
            new OptionChoice("eventColor", Kind.TEXT, "e.g. red",
                    (o, v) -> o.setEntryColor((String) v)),
            new OptionChoice("eventContrastColor", Kind.TEXT, "e.g. yellow",
                    (o, v) -> o.setEntryContrastColor((String) v)),
            new OptionChoice("allDayText", Kind.TEXT, "time grid views",
                    (o, v) -> o.setAllDayText((String) v)),
            new OptionChoice("noEventsText", Kind.TEXT, "list views, go to an empty week",
                    (o, v) -> o.setNoEntriesText((String) v)),
            new OptionChoice("moreLinkText", Kind.TEXT, "month view with day max entries 1",
                    (o, v) -> o.setMoreLinkText((String) v)),
            new OptionChoice("weekTextShort", Kind.TEXT, "the week number prefix",
                    (o, v) -> o.setWeekTextShort((String) v)),
            new OptionChoice("colorScheme", Kind.TEXT, "light or dark",
                    (o, v) -> o.setColorScheme((String) v)),
            new OptionChoice("slotMinTime", Kind.TEXT, "time grid, e.g. 08:00:00",
                    (o, v) -> o.setSlotMinTime((String) v)),
            new OptionChoice("slotMaxTime", Kind.TEXT, "time grid, e.g. 18:00:00",
                    (o, v) -> o.setSlotMaxTime((String) v)),
            new OptionChoice("slotDuration", Kind.TEXT, "time grid, e.g. 01:00:00",
                    (o, v) -> o.setSlotDuration((String) v)),
            new OptionChoice("now", Kind.TEXT, "a date like 2025-03-05, known limit: no effect once the calendar "
                    + "was attached",
                    (o, v) -> o.setNow(v == null ? null : LocalDate.parse((String) v))),
    };

    private final Map<OptionChoice, Object> setOptions = new LinkedHashMap<>();

    public OptionResetPlayground() {
        setSizeFull();

        FullCalendar calendar = new FullCalendar();
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 3));
        calendar.getEntryProvider().asInMemory().addEntries(createEntries());

        Select<CalendarViewImpl> view = new Select<>("View", e -> calendar.changeView(e.getValue()),
                CalendarViewImpl.DAY_GRID_MONTH, CalendarViewImpl.TIME_GRID_WEEK, CalendarViewImpl.LIST_WEEK);
        view.setItemLabelGenerator(CalendarViewImpl::getClientSideValue);
        view.setValue(CalendarViewImpl.DAY_GRID_MONTH);

        Select<OptionChoice> option = new Select<>();
        option.setLabel("Option");
        option.setItems(CHOICES);
        option.setItemLabelGenerator(c -> c.name());

        RadioButtonGroup<Boolean> booleanValue = new RadioButtonGroup<>("Value", true, false);
        NumberField numberValue = new NumberField("Value");
        TextField textValue = new TextField("Value");
        Span hint = new Span();
        Span state = new Span();

        option.addValueChangeListener(e -> {
            Kind kind = e.getValue().kind();
            booleanValue.setVisible(kind == Kind.BOOLEAN);
            numberValue.setVisible(kind == Kind.NUMBER);
            textValue.setVisible(kind == Kind.TEXT);
            hint.setText(e.getValue().hint());
        });
        option.setValue(CHOICES[0]);

        Button set = new Button("Set", e -> {
            OptionChoice choice = option.getValue();
            Object value = switch (choice.kind()) {
                case BOOLEAN -> booleanValue.getValue();
                // whole numbers as Integer, so FullCalendar options like firstDay get 1, not 1.0
                case NUMBER -> numberValue.getValue() == null ? null
                        : numberValue.getValue() % 1 == 0 ? (Object) numberValue.getValue().intValue()
                        : numberValue.getValue();
                case TEXT -> textValue.getValue();
            };
            if (value != null) {
                choice.setter().accept(calendar.options(), value);
                setOptions.put(choice, value);
                showState(state);
            }
        });
        Button unset = new Button("Unset (null)", e -> {
            option.getValue().setter().accept(calendar.options(), null);
            setOptions.remove(option.getValue());
            showState(state);
        });
        Button unsetAll = new Button("Unset all", e -> {
            setOptions.keySet().forEach(o -> o.setter().accept(calendar.options(), null));
            setOptions.clear();
            showState(state);
        });
        Button reattach = new Button("Re-attach calendar", e -> {
            int index = indexOf(calendar);
            remove(calendar);
            addComponentAtIndex(index, calendar);
        });

        HorizontalLayout controls = new HorizontalLayout(view, option, booleanValue, numberValue, textValue, set,
                unset, unsetAll, reattach);
        controls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        controls.setWrap(true);

        showState(state);
        add(controls, hint, state, calendar);
        setFlexGrow(1, calendar);
    }

    private void showState(Span state) {
        state.setText("Set options: " + (setOptions.isEmpty() ? "none" : setOptions.entrySet().stream()
                .map(e -> e.getKey().name() + "=" + e.getValue())
                .collect(Collectors.joining(", "))));
    }

    private static Entry[] createEntries() {
        Entry allDay = new Entry();
        allDay.setTitle("All-day entry");
        allDay.setStart(LocalDate.of(2025, 3, 4).atStartOfDay());
        allDay.setAllDay(true);

        Entry[] entries = new Entry[6];
        entries[0] = allDay;
        for (int i = 1; i < entries.length; i++) {
            Entry timed = new Entry();
            timed.setTitle("Timed entry " + i);
            timed.setStart(LocalDateTime.of(2025, 3, 5, 8 + i, 0));
            timed.setEnd(LocalDateTime.of(2025, 3, 5, 10 + i, 30));
            entries[i] = timed;
        }
        return entries;
    }
}
