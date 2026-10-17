package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.LocaleValue;
import org.vaadin.stefan.fullcalendar.option.CalendarLocale;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The time zone field with {@link Timezone#LOCAL} (#273), and the UI locale, which the calendar does not follow.
 */
public class AutoBrowserSettingsTest extends BrowserlessTest {

    private static final Timezone BERLIN = new Timezone(ZoneId.of("Europe/Berlin"));
    private static final Timezone TOKYO = new Timezone(ZoneId.of("Asia/Tokyo"));

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
        assertTrue(calendar.options().getRawOption("timeZone").isEmpty());

        calendar.setBrowserTimezone("Europe/Berlin");

        assertEquals(BERLIN, calendar.getOption(Option.TIMEZONE).orElseThrow());
    }

    @Test
    void concreteTimezone_inInitialOptions_isTheField_andIgnoresTheBrowser() {
        ObjectNode initialOptions = JsonFactory.createObject();
        initialOptions.put("timeZone", "Europe/Berlin");
        calendar = new FullCalendar(initialOptions);

        assertEquals(BERLIN, calendar.getOption(Option.TIMEZONE).orElseThrow());
        assertEquals(BERLIN, calendar.getTimezoneForOffsets());
        assertTrue(calendar.options().getRawOption("timeZone").isEmpty());

        calendar.setBrowserTimezone("Asia/Tokyo");

        assertEquals(BERLIN, calendar.getOption(Option.TIMEZONE).orElseThrow());
    }

    @Test
    void nullTimezone_inInitialOptions_isDropped() {
        ObjectNode initialOptions = JsonFactory.createObject();
        initialOptions.putNull("timeZone");
        calendar = new FullCalendar(initialOptions);

        assertEquals(Timezone.LOCAL, calendar.getOption(Option.TIMEZONE).orElseThrow());
        assertTrue(calendar.options().getRawOption("timeZone").isEmpty());
    }

    @Test
    void unknownTimezone_inInitialOptions_throws() {
        ObjectNode initialOptions = JsonFactory.createObject();
        initialOptions.put("timeZone", "Nowhere/Nothing");

        assertThrows(IllegalArgumentException.class, () -> new FullCalendar(initialOptions));
    }

    @Test
    void rawTimezone_throws_andNamesTheField() {
        calendar = new FullCalendar();

        for (AbstractOptions options : List.of(calendar.options(),
                calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH))) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> options.setRawOption("timeZone", "Europe/Berlin"));
            assertTrue(e.getMessage().contains("options().setTimeZone"), e.getMessage());
            assertThrows(IllegalArgumentException.class, () -> options.setRawOption("timeZone", null));
        }
    }

    // -------------------------------------------------------------------------
    // UI locale
    // -------------------------------------------------------------------------

    @Test
    void initialState_hasNoToolbar_andTheDefaultLocale() {
        attach();

        assertEquals(BooleanNode.FALSE, initialOptions().get("headerToolbar"));
        assertFalse(initialOptions().has("footerToolbar"));
        assertEquals(CalendarLocale.getDefaultLocale().toLanguageTag().toLowerCase(),
                initialOptions().get("locale").asString());
    }

    @Test
    void headerToolbar_setToNull_isRemovedOnTheClient() {
        attach();

        calendar.options().setHeaderToolbar(null);

        assertEquals(List.of(JsonFactory.createObject().putNull("headerToolbar")), respond());
    }

    @Test
    void locale_null_throws() {
        assertThrows(NullPointerException.class, () -> calendar.options().setLocale((Locale) null));
        assertThrows(NullPointerException.class, () -> calendar.options().setLocale((LocaleValue) null));
    }

    @Test
    void nullLocale_inInitialOptions_keepsTheDefault() {
        calendar = new FullCalendar(JsonFactory.createObject().putNull("locale"));
        attach();

        assertTrue(calendar.options().getRawOption("locale").isEmpty());
        assertEquals(CalendarLocale.getDefaultLocale().toLanguageTag().toLowerCase(),
                initialOptions().get("locale").asString());
    }

    @Test
    void localeOption_isNotTheComponentLocale() {
        var subclass = new FullCalendar() {
            Locale componentLocale() {
                return getLocale();
            }
        };
        calendar = subclass;
        UI.getCurrent().setLocale(Locale.FRENCH);
        calendar.options().setLocale(Locale.GERMAN);
        attach();

        assertEquals(Locale.FRENCH, subclass.componentLocale());
        assertEquals("de", initialOptions().get("locale").asString());
    }

    @Test
    void headerToolbarInInitialOptions_winsOverTheField() {
        calendar = new FullCalendar(JsonFactory.createObject().set("headerToolbar",
                JsonFactory.createObject().put("center", "title")));
        attach();

        assertEquals("title", initialOptions().get("headerToolbar").get("center").asString());
    }

    @Test
    void localeInInitialOptions_winsOverTheField() {
        calendar = new FullCalendar(JsonFactory.createObject().put("locale", "de"));
        attach();

        assertEquals("de", initialOptions().get("locale").asString());
    }

    @Test
    void uiLocaleChange_isIgnored() {
        LocaleValue before = calendar.options().getLocale();
        UI.getCurrent().add(calendar);

        UI.getCurrent().setLocale(Locale.FRENCH);

        assertEquals(before, calendar.options().getLocale());
    }
}
