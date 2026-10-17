package org.vaadin.stefan.ui.view.testviews;

import org.vaadin.stefan.fullcalendar.option.ContentValue;
import org.vaadin.stefan.fullcalendar.option.DisplayMode;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarButtonDisplay;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarParts;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import org.vaadin.stefan.fullcalendar.option.ToolbarValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test view for the FullCalendar 7 options: custom buttons (set before and after attach), the entry
 * display kind class hooks, a content hook, a did-mount hook, the toolbar classes and a few plain options.
 * <p>
 * Buttons switch the view, so each entry display kind can be checked on its own.
 * <p>
 * Route: /test/fc7-options
 */
@Route(value = "fc7-options", layout = TestLayout.class)
@MenuItem(label = "FC7 Options")
public class Fc7OptionsTestView extends VerticalLayout {

    private static final String CLICK_CALLBACK = "function(ev) { document.body.setAttribute('data-%s', 'clicked'); }";

    public Fc7OptionsTestView() {
        setSizeFull();
        setPadding(true);
        add(new H2("FullCalendar 7 options"));

        FullCalendar calendar = new FullCalendar();
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 10));
        calendar.options().setInitialView(CalendarViewImpl.DAY_GRID_MONTH);

        // Buttons set before attach: override the today text, add a custom button
        Map<String, Map<String, Object>> buttons = new LinkedHashMap<>();
        buttons.put(NativeToolbarParts.TODAY, Map.of("text", "Jump to now", "display", NativeToolbarButtonDisplay.TEXT));
        // a button with an icon shows only the icon by default, the display setting shows the text instead
        buttons.put("textOnly", Map.of("text", "Text only", "display", NativeToolbarButtonDisplay.TEXT,
                "iconContent", Map.of("html", "<span class=\"hook-icon\">*</span>")));
        // control: the same button without display shows its icon
        buttons.put("iconAuto", Map.of("text", "Icon auto",
                "iconContent", Map.of("html", "<span class=\"hook-icon\">*</span>")));
        buttons.put("custom1", Map.of("text", "Custom One", "click", JsCallback.of(CLICK_CALLBACK.formatted("custom1"))));
        calendar.options().setButtons(buttons);
        // Toolbar elements: content from a callback and plain text
        calendar.options().setToolbarElements(Map.<String, ContentValue>of(
                "callbackElement", JsCallback.of("function() { return { html: '<span class=\"hook-toolbar-element\">From callback</span>' }; }"),
                "textElement", new TextContent("Plain text element")));
        calendar.options().setHeaderToolbar(ToolbarValue.ofStartEnd(
                "prev,next today custom1 textOnly iconAuto", NativeToolbarParts.TITLE, "callbackElement textElement"));

        // Entries: one timed, one all-day, one background
        Entry timed = new Entry();
        timed.setTitle("Timed");
        timed.setStart(LocalDate.of(2025, 3, 11).atTime(9, 0));
        timed.setEnd(LocalDate.of(2025, 3, 11).atTime(11, 0));

        Entry allDay = new Entry();
        allDay.setTitle("All day");
        allDay.setAllDay(true);
        allDay.setStart(LocalDate.of(2025, 3, 12).atStartOfDay());
        allDay.setEnd(LocalDate.of(2025, 3, 14).atStartOfDay());

        Entry background = new Entry();
        background.setTitle("Background");
        background.setDisplayMode(DisplayMode.BACKGROUND);
        background.setAllDay(true);
        background.setStart(LocalDate.of(2025, 3, 17).atStartOfDay());
        background.setEnd(LocalDate.of(2025, 3, 19).atStartOfDay());

        calendar.setEntryProvider(EntryProvider.inMemoryFrom(timed, allDay, background));

        // Render hooks, one per family. Each marks its target so the test does not depend on FullCalendar's own classes.
        calendar.options().setBlockEntryClass(JsCallback.of("function(info) { return 'hook-block'; }"));
        calendar.options().setRowEntryClass(JsCallback.of("function(info) { return 'hook-row'; }"));
        calendar.options().setColumnEntryClass(JsCallback.of("function(info) { return 'hook-column'; }"));
        calendar.options().setListItemEntryClass(JsCallback.of("function(info) { return 'hook-list-item'; }"));
        calendar.options().setBackgroundEntryClass(JsCallback.of("function(info) { return 'hook-background'; }"));
        calendar.options().setBackgroundEntryContent(
                JsCallback.of("function(info) { return { html: '<span class=\"hook-background-content\">BG</span>' }; }"));
        calendar.options().setBackgroundEntryDidMount(
                JsCallback.of("function(info) { info.el.setAttribute('data-bg-mounted', 'true'); }"));
        calendar.options().setToolbarClass("hook-toolbar");
        calendar.options().setButtonClass(JsCallback.of("function(info) { return 'hook-button'; }"));

        // Plain options
        calendar.options().setNoEntriesText("Nothing planned");
        calendar.options().setHeadingLevel(3);

        // View switchers
        HorizontalLayout views = new HorizontalLayout();
        views.add(viewButton(calendar, "view-time-grid", CalendarViewImpl.TIME_GRID_WEEK));
        views.add(viewButton(calendar, "view-list", CalendarViewImpl.LIST_MONTH));
        views.add(viewButton(calendar, "view-day-grid-day", CalendarViewImpl.DAY_GRID_DAY));

        // Apply a second custom button after attach. Replaces the whole buttons map.
        Button applyLate = new Button("Apply late button", e -> {
            Map<String, Map<String, Object>> all = new LinkedHashMap<>(buttons);
            all.put("custom2", Map.of("text", "Custom Two", "click", JsCallback.of(CLICK_CALLBACK.formatted("custom2"))));
            calendar.options().setButtons(all);
            calendar.options().setHeaderToolbar(ToolbarValue.ofStartEnd(
                "prev,next today custom1 textOnly iconAuto custom2", NativeToolbarParts.TITLE, "callbackElement textElement"));
        });
        applyLate.setId("apply-late-button");
        views.add(applyLate);

        add(views, calendar);
        setFlexGrow(1, calendar);
    }

    private Button viewButton(FullCalendar calendar, String id, CalendarView view) {
        Button button = new Button(id, e -> calendar.changeView(view));
        button.setId(id);
        return button;
    }
}
