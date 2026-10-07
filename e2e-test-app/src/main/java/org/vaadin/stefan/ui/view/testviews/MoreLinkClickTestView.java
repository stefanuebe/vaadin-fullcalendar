package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.JsCallback;
import org.vaadin.stefan.fullcalendar.Option;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * Test view for {@link Option#MORE_LINK_CLICK}: the value set as option decides what the "+N more" link does, and the
 * server receives every click.
 * <p>
 * Before attach the calendar gets {@code MoreLinkClickAction.DAY}, or with the parameter {@code callback} a
 * {@link JsCallback} returning {@code "week"}. A button sets that callback after attach.
 * {@code #more-link-count} counts the {@code MoreLinkClickedEvent}s on the server.
 * <p>
 * Route: /test/more-link-click, /test/more-link-click/callback
 */
@Route(value = "more-link-click", layout = TestLayout.class)
@MenuItem(label = "More Link Click")
public class MoreLinkClickTestView extends VerticalLayout implements HasUrlParameter<String> {

    private static final JsCallback RETURN_WEEK = JsCallback.of("function(info) { return 'week'; }");

    private final FullCalendar calendar = new FullCalendar();

    public MoreLinkClickTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("More Link Click"));

        calendar.getElement().setAttribute("data-testid", "calendar");
        calendar.setOption(Option.INITIAL_DATE, LocalDate.of(2025, 3, 1));
        calendar.setOption(Option.INITIAL_VIEW, CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());
        calendar.setOption(Option.DAY_MAX_ENTRY_ROWS, 2);

        // 5 all-day entries on 2025-03-05 produce a "+N more" link with 2 rows
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        for (int i = 1; i <= 5; i++) {
            Entry entry = new Entry();
            entry.setTitle("Entry " + i);
            entry.setStart(LocalDate.of(2025, 3, 5));
            entry.setAllDay(true);
            provider.addEntry(entry);
        }
        calendar.setEntryProvider(provider);

        Span count = new Span("0");
        count.setId("more-link-count");
        calendar.addMoreLinkClickedListener(e -> count.setText(String.valueOf(Integer.parseInt(count.getText()) + 1)));

        Button callbackButton = new Button("Callback returning week",
                e -> calendar.setOption(Option.MORE_LINK_CLICK, RETURN_WEEK));
        callbackButton.getElement().setAttribute("data-testid", "btn-callback");

        add(callbackButton, count, calendar);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        // runs before the view is attached, so the option goes with the initial options
        calendar.setOption(Option.MORE_LINK_CLICK, "callback".equals(parameter) ? RETURN_WEEK : FullCalendar.MoreLinkClickAction.DAY);
    }
}
