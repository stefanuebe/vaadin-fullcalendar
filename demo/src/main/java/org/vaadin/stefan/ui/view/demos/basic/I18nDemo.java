package org.vaadin.stefan.ui.view.demos.basic;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarItemProviderToolbar;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Demonstrates timezone and locale customization in FullCalendar.
 * <p>
 * The browser's timezone is detected automatically on attachment. Users can override it
 * via the timezone selector and switch the locale to see month/day names adapt to different
 * languages.
 * </p>
 */
@Route(value = "i18n", layout = MainLayout.class)
@MenuItem(label = "i18n")
public class I18nDemo extends AbstractDemoView {

    // Initialized in createToolbar(), NOT as field initializers — because super() calls
    // createCalendar()/createToolbar() before subclass field initializers run.
    private ComboBox<Locale> localeSelector;
    private Select<Timezone> timezoneSelector;

    // DEMO-START
    private static final List<Timezone> DEMO_TIMEZONES = List.of(
            Timezone.UTC,
            new Timezone(ZoneId.of("Europe/Berlin")),
            new Timezone(ZoneId.of("America/Los_Angeles")),
            new Timezone(ZoneId.of("Japan")),
            new Timezone(ZoneId.of("Australia/Sydney"))
    );

    @Override
    protected FullCalendar<?> createCalendar() {
        // Reference date: 2024-03-15 at noon UTC — concrete date so times are stable.
        LocalDateTime base = LocalDateTime.of(2024, 3, 15, 12, 0);

        Entry standup = new Entry();
        standup.setTitle("Team Standup");
        standup.setStart(base.withHour(9));
        standup.setEnd(base.withHour(9).withMinute(30));
        standup.setColor("#3788d8");

        Entry lunchWebinar = new Entry();
        lunchWebinar.setTitle("Lunch Webinar");
        lunchWebinar.setStart(base.withHour(12));
        lunchWebinar.setEnd(base.withHour(13));
        lunchWebinar.setColor("#2ecc71");

        Entry release = new Entry();
        release.setTitle("Release Call");
        release.setStart(base.withHour(17));
        release.setEnd(base.withHour(18));
        release.setColor("#e74c3c");

        Entry nightDeploy = new Entry();
        nightDeploy.setTitle("Night Deploy");
        nightDeploy.setStart(base.withHour(22));
        nightDeploy.setEnd(base.withHour(23));
        nightDeploy.setColor("#9b59b6");

        FullCalendar<Entry> calendar = FullCalendarBuilder.<Entry>create()
                .withAutoBrowserTimezone()
                .withInitialEntries(List.of(standup, lunchWebinar, release, nightDeploy))
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);

        // Auto-sync the timezone selector when the browser reports its timezone.
        calendar.addBrowserTimezoneObtainedListener(event -> {
            Timezone detected = event.getTimezone();
            boolean known = DEMO_TIMEZONES.stream().anyMatch(tz -> Objects.equals(tz.getZoneId(), detected.getZoneId()));
            if (known) {
                timezoneSelector.setValue(detected);
            }
            Notification.show("Browser timezone detected: " + detected.getClientSideValue());
        });

        return calendar;
    }

    @Override
    protected Component createToolbar() {
        // --- Navigation toolbar (also implements DemoToolbar for auto-wiring) ---
        CalendarItemProviderToolbar navToolbar = CalendarItemProviderToolbar.builder()
                .calendar(getCalendar())
                .dateChangeable(true)
                .viewChangeable(true)
                .build();

        // --- Locale selector ---
        localeSelector = new ComboBox<>("Locale");
        localeSelector.setItems(Arrays.asList(CalendarLocale.getAvailableLocales()));
        localeSelector.setValue(CalendarLocale.getDefaultLocale());
        localeSelector.setItemLabelGenerator(locale -> locale.getDisplayName(locale));
        localeSelector.setWidth("220px");
        localeSelector.addValueChangeListener(event -> {
            Locale value = event.getValue();
            if (value != null) {
                getCalendar().setLocale(value);
                Notification.show("Locale changed to " + value.toLanguageTag());
            }
        });

        // --- Timezone selector ---
        timezoneSelector = new Select<>();
        timezoneSelector.setLabel("Timezone");
        timezoneSelector.setItems(DEMO_TIMEZONES);
        timezoneSelector.setItemLabelGenerator(Timezone::getClientSideValue);
        timezoneSelector.setValue(Timezone.UTC);
        timezoneSelector.setWidth("220px");
        timezoneSelector.addValueChangeListener(event -> {
            Timezone value = event.getValue();
            if (value != null && !Objects.equals(getCalendar().getTimezone(), value)) {
                getCalendar().setTimezone(value);
                Notification.show("Timezone changed to " + value.getClientSideValue());
            }
        });

        HorizontalLayout i18nControls = new HorizontalLayout(localeSelector, timezoneSelector);
        i18nControls.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.BASELINE);

        // Stack navigation row on top, locale/timezone controls below.
        VerticalLayout composite = new VerticalLayout(navToolbar, i18nControls);
        composite.setSpacing(false);
        composite.setPadding(false);
        composite.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        // Expose DemoToolbar from the nav toolbar so AbstractDemoView can wire date/view updates.
        return new DemoToolbarProxy(composite, navToolbar);
    }

    @Override
    protected String createDescription() {
        return "FullCalendar supports internationalization out of the box. Change the locale to see month names, "
                + "day names, and date formats adapt to different languages. Switch the timezone to see how event "
                + "times shift across time zones. The browser's timezone is detected automatically on page load.";
    }
    // DEMO-END

    /**
     * Thin wrapper that delegates {@link org.vaadin.stefan.ui.view.DemoToolbar} to a nested toolbar
     * while exposing the outer composite layout as the actual component added to the view.
     * <p>
     * This lets us return a {@code VerticalLayout} from {@code createToolbar()} while still
     * satisfying the {@code instanceof DemoToolbar} check in {@link AbstractDemoView#wireToolbar}.
     * </p>
     */
    private static final class DemoToolbarProxy extends VerticalLayout
            implements org.vaadin.stefan.ui.view.DemoToolbar {

        private final org.vaadin.stefan.ui.view.DemoToolbar delegate;

        DemoToolbarProxy(VerticalLayout content, org.vaadin.stefan.ui.view.DemoToolbar delegate) {
            this.delegate = delegate;
            setSpacing(false);
            setPadding(false);
            // Transfer children from `content` to this proxy layout.
            content.getChildren().toList().forEach(child -> {
                content.remove(child);
                add(child);
            });
        }

        @Override
        public void updateInterval(java.time.LocalDate intervalStart) {
            delegate.updateInterval(intervalStart);
        }

        @Override
        public void updateSelectedView(CalendarView view) {
            delegate.updateSelectedView(view);
        }
    }
}
