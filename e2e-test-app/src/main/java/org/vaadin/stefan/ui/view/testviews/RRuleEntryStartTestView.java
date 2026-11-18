package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

/**
 * Test view for rules without {@code dtstart}, which start at the start of their entry (#451, #452). The calendar
 * runs in {@code Europe/Berlin}, one hour ahead of UTC in January, and shows the week of 2025-01-06.
 * <ul>
 *   <li>{@code plain-entry}: no rule, starts 2025-01-06 14:00 UTC, shown at 15:00</li>
 *   <li>{@code rule-entry}: daily rule with one occurrence, starts 2025-01-07 14:00 UTC, expected at 15:00</li>
 *   <li>{@code positional-entry}: monthly rule on the second Thursday, starts 2025-01-01 08:00 UTC, expected on
 *       2025-01-09 at 09:00</li>
 *   <li>{@code excluded-entry}: daily rule with five occurrences from 2025-01-06 14:00 UTC, 2025-01-08 excluded
 *       (#461), expected on the other four days</li>
 *   <li>{@code excluded-positional-entry}: like {@code positional-entry}, with 2025-01-09 excluded, expected nowhere</li>
 * </ul>
 * <p>
 * With the query parameter {@code local}, the calendar follows the browser ({@code Timezone.LOCAL}) instead. The
 * browser then has to run in {@code Europe/Berlin}.
 * <p>
 * Route: /test/rrule-entry-start
 */
@Route(value = "rrule-entry-start", layout = TestLayout.class)
@MenuItem(label = "RRule entry start")
public class RRuleEntryStartTestView extends VerticalLayout implements BeforeEnterObserver {

    private final FullCalendar calendar = new FullCalendar();

    public RRuleEntryStartTestView() {
        setSizeFull();
        setPadding(true);
        add(new H2("Rules without dtstart in a calendar in Europe/Berlin"));

        calendar.options().setTimeZone(new Timezone(ZoneId.of("Europe/Berlin")));
        calendar.options().setLocale(Locale.GERMANY);
        calendar.options().setInitialView(CalendarViewImpl.TIME_GRID_WEEK);
        calendar.options().setInitialDate(LocalDate.of(2025, 1, 6));

        Entry plain = new Entry("Plain");
        plain.addClassNames("plain-entry");
        plain.setStart(LocalDateTime.of(2025, 1, 6, 14, 0));
        plain.setEnd(LocalDateTime.of(2025, 1, 6, 15, 0));

        Entry rule = new Entry("Rule");
        rule.addClassNames("rule-entry");
        rule.setStart(LocalDateTime.of(2025, 1, 7, 14, 0));
        rule.setRRule(RRule.daily().count(1));

        Entry positional = new Entry("Positional");
        positional.addClassNames("positional-entry");
        positional.setStart(LocalDateTime.of(2025, 1, 1, 8, 0));
        positional.setRRule(RRule.monthly().byWeekday("2th"));

        Entry excluded = new Entry("Excluded");
        excluded.addClassNames("excluded-entry");
        excluded.setStart(LocalDateTime.of(2025, 1, 6, 14, 0));
        excluded.setRRule(RRule.daily().count(5).excludeDates(LocalDate.of(2025, 1, 8)));

        Entry excludedPositional = new Entry("Excluded positional");
        excludedPositional.addClassNames("excluded-positional-entry");
        excludedPositional.setStart(LocalDateTime.of(2025, 1, 1, 8, 0));
        excludedPositional.setRRule(RRule.monthly().byWeekday("2th").excludeDates(LocalDate.of(2025, 1, 9)));

        calendar.getEntryProvider().asInMemory().setItems(plain, rule, positional, excluded, excludedPositional);
        add(calendar);
        setFlexGrow(1, calendar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("local")) {
            calendar.options().setTimeZone(Timezone.LOCAL);
        }
    }
}
