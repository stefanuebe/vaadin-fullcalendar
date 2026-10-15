package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The time zone field with {@link Timezone#LOCAL}, {@link FullCalendar#withAutoBrowserTimezone()} and
 * {@link FullCalendar#withAutoUiLocale()} (#273).
 */
public class AutoBrowserSettingsTest extends BrowserlessTest {

    private static final Timezone BERLIN = new Timezone(ZoneId.of("Europe/Berlin"));
    private static final Timezone TOKYO = new Timezone(ZoneId.of("Asia/Tokyo"));
    private static final Timezone NEW_YORK = new Timezone(ZoneId.of("America/New_York"));

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
        UI.getCurrent().setLocale(Locale.ENGLISH);
    }

    private ObjectNode initialOptions() {
        return (ObjectNode) calendar.getElement().getPropertyRaw("initialOptions");
    }

    private void attach() {
        UI.getCurrent().add(calendar);
        respond();
    }

    /** Runs what the server does before its response and returns the option changes sent to the client. */
    private List<JsonNode> respond() {
        UI ui = UI.getCurrent();
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression().contains("setOptions"))
                .map(invocation -> (JsonNode) invocation.getParameters().getLast())
                .toList();
    }

    // -------------------------------------------------------------------------
    // Browser time zone
    // -------------------------------------------------------------------------

    @Test
    void timeZone_beforeTheReport_isLocalAndConvertsWithUtc() {
        assertEquals(Timezone.LOCAL, calendar.options().getTimeZone());
        assertEquals(LocalDateTime.of(2025, 3, 3, 10, 0),
                calendar.getTimezoneForOffsets().applyTimezoneOffset(LocalDateTime.of(2025, 3, 3, 10, 0)));
        assertEquals("local", initialOptions().get("timeZone").asString());
    }

    @Test
    void timeZone_null_throws() {
        assertThrows(NullPointerException.class, () -> calendar.options().setTimeZone(null));
    }

    @Test
    void report_whileLocal_setsTheZoneAndSendsNothing() {
        attach();

        calendar.setBrowserTimezone("Europe/Berlin");

        assertEquals(BERLIN, calendar.options().getTimeZone());
        assertEquals(LocalDateTime.of(2025, 3, 3, 11, 0),
                calendar.getTimezoneForOffsets().applyTimezoneOffset(LocalDateTime.of(2025, 3, 3, 10, 0)));
        assertEquals(List.of(), respond());
    }

    @Test
    void report_whileLocal_aSecondReportReplacesTheZone() {
        calendar.setBrowserTimezone("Europe/Berlin");
        calendar.setBrowserTimezone("Asia/Tokyo");

        assertEquals(TOKYO, calendar.options().getTimeZone());
    }

    @Test
    void report_withAConcreteZone_doesNotReplaceIt() {
        calendar.options().setTimeZone(TOKYO);

        calendar.setBrowserTimezone("Europe/Berlin");

        assertEquals(TOKYO, calendar.options().getTimeZone());
        assertEquals(BERLIN, calendar.getBrowserTimezone().orElseThrow());
    }

    @Test
    void concreteZone_isSentAsItIs() {
        attach();

        calendar.options().setTimeZone(TOKYO);

        assertEquals(List.of(JsonFactory.createObject().put("timeZone", "Asia/Tokyo")), respond());
    }

    @Test
    void local_afterAReport_appliesTheReportedZoneAtOnce() {
        calendar.options().setTimeZone(TOKYO);
        calendar.setBrowserTimezone("Europe/Berlin");
        attach();

        calendar.options().setTimeZone(Timezone.LOCAL);

        assertEquals(BERLIN, calendar.options().getTimeZone());
        assertEquals(List.of(JsonFactory.createObject().put("timeZone", "local")), respond());
    }

    @Test
    void withAutoBrowserTimezone_isSetTimeZoneLocal() {
        FullCalendar viaOptions = new FullCalendar();
        for (FullCalendar c : List.of(calendar, viaOptions)) {
            c.options().setTimeZone(TOKYO);
            c.setBrowserTimezone("Europe/Berlin");
        }

        calendar.withAutoBrowserTimezone();
        viaOptions.options().setTimeZone(Timezone.LOCAL);

        // both take the known zone at once and follow the next report
        assertEquals(BERLIN, calendar.options().getTimeZone());
        assertEquals(BERLIN, viaOptions.options().getTimeZone());
        calendar.setBrowserTimezone("America/New_York");
        viaOptions.setBrowserTimezone("America/New_York");
        assertEquals(NEW_YORK, calendar.options().getTimeZone());
        assertEquals(NEW_YORK, viaOptions.options().getTimeZone());
    }

    @Test
    void timezoneNull_viaSetOption_isLocal() {
        calendar.options().setTimeZone(TOKYO);

        calendar.setOption(Option.TIMEZONE, null);

        assertEquals(Timezone.LOCAL, calendar.getOption(Option.TIMEZONE).orElseThrow());
        assertEquals("local", calendar.getOption(Option.TIMEZONE, true).orElseThrow());
    }

    @Test
    void timezoneClientSideValue_isWhatTheClientGets() {
        calendar.setBrowserTimezone("Europe/Berlin");
        assertEquals("local", calendar.getOption(Option.TIMEZONE, true).orElseThrow());

        calendar.options().setTimeZone(TOKYO);
        assertEquals("Asia/Tokyo", calendar.getOption(Option.TIMEZONE, true).orElseThrow());
    }

    @Test
    void timezoneLocal_viaSetOption_followsTheBrowser() {
        calendar.options().setTimeZone(TOKYO);

        calendar.setOption(Option.TIMEZONE, "Local");
        calendar.setBrowserTimezone("Europe/Berlin");

        assertEquals(BERLIN, calendar.getOption(Option.TIMEZONE).orElseThrow());
    }

    @Test
    void timezoneLocal_stringKey_behavesLikeOptionKey() {
        calendar.setBrowserTimezone("Europe/Berlin");
        calendar.options().setTimeZone(TOKYO);

        calendar.setOption("timeZone", "local");

        assertEquals(BERLIN, calendar.getOption(Option.TIMEZONE).orElseThrow());
    }

    @Test
    void timezoneLocal_inInitialOptions_followsTheBrowser() {
        ObjectNode initialOptions = JsonFactory.createObject();
        initialOptions.put("timeZone", "local");
        calendar = new FullCalendar(initialOptions);

        assertEquals(Timezone.LOCAL, calendar.getOption(Option.TIMEZONE).orElseThrow());
        assertTrue(calendar.getRawOption("timeZone").isEmpty());

        calendar.setBrowserTimezone("Europe/Berlin");

        assertEquals(BERLIN, calendar.getOption(Option.TIMEZONE).orElseThrow());
    }

    // -------------------------------------------------------------------------
    // UI locale
    // -------------------------------------------------------------------------

    @Test
    void autoUiLocale_onAttach_takesUiLocale() {
        UI.getCurrent().setLocale(Locale.GERMAN);
        calendar.withAutoUiLocale();

        UI.getCurrent().add(calendar);

        assertEquals(Locale.GERMAN, calendar.getOption(Option.LOCALE).orElseThrow());
    }

    @Test
    void autoUiLocale_followsLocaleChange() {
        calendar.withAutoUiLocale();
        UI.getCurrent().add(calendar);

        UI.getCurrent().setLocale(Locale.FRENCH);

        assertEquals(Locale.FRENCH, calendar.getOption(Option.LOCALE).orElseThrow());
    }

    @Test
    void autoUiLocale_enabledAfterAttach_isAppliedAtOnce() {
        UI.getCurrent().setLocale(Locale.GERMAN);
        UI.getCurrent().add(calendar);

        calendar.withAutoUiLocale();

        assertEquals(Locale.GERMAN, calendar.getOption(Option.LOCALE).orElseThrow());
    }

    @Test
    void withoutAutoUiLocale_localeChange_isIgnored() {
        Locale before = calendar.<Locale>getOption(Option.LOCALE).orElseThrow();
        UI.getCurrent().add(calendar);

        UI.getCurrent().setLocale(Locale.FRENCH);

        assertEquals(before, calendar.getOption(Option.LOCALE).orElseThrow());
    }
}
