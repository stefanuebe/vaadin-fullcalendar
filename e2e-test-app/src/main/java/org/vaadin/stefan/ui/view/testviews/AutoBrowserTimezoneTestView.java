package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

/**
 * Test view for {@link FullCalendar#withAutoBrowserTimezone()}: the time zone the browser reports becomes the
 * calendar's time zone option. {@code #calendar-timezone} shows {@code getTimezone()} once the browser reported.
 * <p>
 * Route: /test/auto-browser-timezone
 */
@Route(value = "auto-browser-timezone", layout = TestLayout.class)
@MenuItem(label = "Auto Browser Timezone")
public class AutoBrowserTimezoneTestView extends VerticalLayout {

    public AutoBrowserTimezoneTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Auto Browser Timezone"));

        FullCalendar calendar = new FullCalendar().withAutoBrowserTimezone();
        calendar.getElement().setAttribute("data-testid", "calendar");

        Span timezone = new Span("-");
        timezone.setId("calendar-timezone");
        calendar.addBrowserTimezoneObtainedListener(e -> timezone.setText(calendar.getTimezone().getClientSideValue()));

        add(timezone, calendar);
    }
}
