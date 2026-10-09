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
import org.vaadin.stefan.fullcalendar.Option;
import org.vaadin.stefan.ui.layouts.MainLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manual check for removing options of an attached calendar (#294). Pick an option, set a value, then remove it
 * with "Unset". The calendar should look as if the option had never been set. The view is not in the menu, so open
 * /option-reset-playground directly.
 */
@Route(value = "option-reset-playground", layout = MainLayout.class)
public class OptionResetPlayground extends VerticalLayout {

    private enum Kind {BOOLEAN, NUMBER, TEXT}

    private record OptionChoice(Option option, Kind kind, String hint) {
    }

    private static final OptionChoice[] CHOICES = {
            new OptionChoice(Option.WEEKENDS, Kind.BOOLEAN, "show Saturday and Sunday"),
            new OptionChoice(Option.WEEK_NUMBERS, Kind.BOOLEAN, "the add-on sets true at creation"),
            new OptionChoice(Option.DISPLAY_ENTRY_TIME, Kind.BOOLEAN, "no default, the view decides"),
            new OptionChoice(Option.DISPLAY_ENTRY_END, Kind.BOOLEAN, "no default, the view decides"),
            new OptionChoice(Option.DEFAULT_ALL_DAY, Kind.BOOLEAN, "no default"),
            new OptionChoice(Option.EDITABLE, Kind.BOOLEAN, "drag and resize entries"),
            new OptionChoice(Option.ENTRY_START_EDITABLE, Kind.BOOLEAN, "no default, falls back to editable"),
            new OptionChoice(Option.ENTRY_DURATION_EDITABLE, Kind.BOOLEAN, "no default, falls back to editable"),
            new OptionChoice(Option.SELECTABLE, Kind.BOOLEAN, "select time spans"),
            new OptionChoice(Option.NAV_LINKS, Kind.BOOLEAN, "day numbers become links"),
            new OptionChoice(Option.NOW_INDICATOR, Kind.BOOLEAN, "time grid views"),
            new OptionChoice(Option.ALL_DAY_SLOT, Kind.BOOLEAN, "time grid views"),
            new OptionChoice(Option.DAY_HEADERS, Kind.BOOLEAN, ""),
            new OptionChoice(Option.BORDERLESS, Kind.BOOLEAN, "no default"),
            new OptionChoice(Option.FIRST_DAY, Kind.NUMBER, "0 = Sunday, no default"),
            new OptionChoice(Option.DAY_MAX_ENTRIES, Kind.NUMBER, "month view, try 1"),
            new OptionChoice(Option.ENTRY_MAX_STACK, Kind.NUMBER, "time grid, no default"),
            new OptionChoice(Option.ASPECT_RATIO, Kind.NUMBER, "e.g. 3"),
            new OptionChoice(Option.HEIGHT, Kind.NUMBER, "pixels, no default"),
            new OptionChoice(Option.ENTRY_COLOR, Kind.TEXT, "e.g. red"),
            new OptionChoice(Option.ENTRY_TEXT_COLOR, Kind.TEXT, "e.g. yellow"),
            new OptionChoice(Option.ALL_DAY_TEXT, Kind.TEXT, "time grid views"),
            new OptionChoice(Option.NO_ENTRIES_TEXT, Kind.TEXT, "list views, go to an empty week"),
            new OptionChoice(Option.MORE_LINK_TEXT, Kind.TEXT, "month view with day max entries 1"),
            new OptionChoice(Option.WEEK_TEXT, Kind.TEXT, "the week number prefix"),
            new OptionChoice(Option.COLOR_SCHEME, Kind.TEXT, "light or dark"),
            new OptionChoice(Option.LOCALE, Kind.TEXT, "e.g. de or fr"),
            new OptionChoice(Option.SLOT_MIN_TIME, Kind.TEXT, "time grid, e.g. 08:00:00"),
            new OptionChoice(Option.SLOT_MAX_TIME, Kind.TEXT, "time grid, e.g. 18:00:00"),
            new OptionChoice(Option.SLOT_DURATION, Kind.TEXT, "time grid, e.g. 01:00:00"),
            new OptionChoice(Option.NOW, Kind.TEXT, "known limit: no effect once the calendar was attached"),
    };

    private final Map<Option, Object> setOptions = new LinkedHashMap<>();

    public OptionResetPlayground() {
        setSizeFull();

        FullCalendar calendar = new FullCalendar();
        calendar.setOption(Option.INITIAL_DATE, LocalDate.of(2025, 3, 3));
        calendar.getEntryProvider().asInMemory().addEntries(createEntries());

        Select<CalendarViewImpl> view = new Select<>("View", e -> calendar.changeView(e.getValue()),
                CalendarViewImpl.DAY_GRID_MONTH, CalendarViewImpl.TIME_GRID_WEEK, CalendarViewImpl.LIST_WEEK);
        view.setItemLabelGenerator(CalendarViewImpl::getClientSideValue);
        view.setValue(CalendarViewImpl.DAY_GRID_MONTH);

        Select<OptionChoice> option = new Select<>();
        option.setLabel("Option");
        option.setItems(CHOICES);
        option.setItemLabelGenerator(c -> c.option().name());

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
                calendar.setOption(choice.option(), value);
                setOptions.put(choice.option(), value);
                showState(state);
            }
        });
        Button unset = new Button("Unset (null)", e -> {
            calendar.setOption(option.getValue().option(), null);
            setOptions.remove(option.getValue().option());
            showState(state);
        });
        Button unsetAll = new Button("Unset all", e -> {
            setOptions.keySet().forEach(o -> calendar.setOption(o, null));
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
