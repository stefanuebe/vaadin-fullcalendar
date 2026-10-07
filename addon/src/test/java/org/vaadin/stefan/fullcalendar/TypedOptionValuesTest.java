package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Typed option values (time zone, valid range, more-link click action, day max entries) reach the client the same
 * way before and after attach, and the deprecated setters store the same value as the option (#259).
 */
@SuppressWarnings("removal")
public class TypedOptionValuesTest extends BrowserlessTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    /** Client value of the option in the initial options, which the client reads on attach. */
    private JsonNode initialClientValue(Option option) {
        JsonNode initialOptions = (JsonNode) calendar.getElement().getPropertyRaw("initialOptions");
        return initialOptions.get(option.getOptionKey());
    }

    /** Attaches the calendar and returns the client value of the option sent by the next setOption call. */
    private Serializable clientValueAfterAttach(Option option, Runnable setter) {
        UI ui = UI.getCurrent();
        ui.add(calendar);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().dumpPendingJavaScriptInvocations();

        setter.run();

        // JS calls are queued until the server prepares its response
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        List<PendingJavaScriptInvocation> invocations = ui.getInternals().dumpPendingJavaScriptInvocations();
        for (PendingJavaScriptInvocation invocation : invocations) {
            List<Object> parameters = invocation.getInvocation().getParameters();
            int keyIndex = parameters.indexOf(option.getOptionKey());
            if (invocation.getInvocation().getExpression().contains("setOption") && keyIndex >= 0) {
                return (Serializable) parameters.get(keyIndex + 1);
            }
        }
        return fail("no setOption call for " + option.getOptionKey());
    }

    // -------------------------------------------------------------------------
    // Time zone
    // -------------------------------------------------------------------------

    @Test
    void timezone_asId_isStoredAsTimezone() {
        calendar.setOption(Option.TIMEZONE, "Europe/Berlin");

        Timezone expected = new Timezone(ZoneId.of("Europe/Berlin"));
        assertEquals(expected, calendar.getTimezone());
        assertEquals(expected, calendar.getOption(Option.TIMEZONE).orElseThrow());
        assertEquals("Europe/Berlin", initialClientValue(Option.TIMEZONE).asString());
    }

    @Test
    void timezone_idAsDeprecatedServerSideValue_isStoredAsTimezone() {
        calendar.setOption(Option.TIMEZONE, "Europe/Berlin", "Europe/Berlin");

        assertEquals(new Timezone(ZoneId.of("Europe/Berlin")), calendar.getOption(Option.TIMEZONE).orElseThrow());
    }

    @Test
    void timezone_utcId_equalsUtcConstant() {
        calendar.setOption(Option.TIMEZONE, "UTC");

        assertEquals(Timezone.UTC, calendar.getTimezone());
    }

    @Test
    void timezone_local_isRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> calendar.setOption(Option.TIMEZONE, "local"));
        assertTrue(e.getMessage().contains("withAutoBrowserTimezone"));
    }

    @Test
    void timezone_unknownId_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> calendar.setOption(Option.TIMEZONE, "Not/AZone"));
    }

    @Test
    void timezone_setterAndOption_storeSameValue() {
        Timezone berlin = new Timezone(ZoneId.of("Europe/Berlin"));
        FullCalendar viaOption = new FullCalendar();

        calendar.setTimezone(berlin);
        viaOption.setOption(Option.TIMEZONE, berlin);

        assertEquals(viaOption.getOption(Option.TIMEZONE), calendar.getOption(Option.TIMEZONE));
    }

    @Test
    void timezone_deprecatedGetter_returnsUtcWhileNotSet() {
        assertEquals(Timezone.UTC, calendar.getTimezone());
        assertTrue(calendar.getOption(Option.TIMEZONE).isEmpty());
    }

    @Test
    void timezone_setterWithDefaultUtc_storesOptionLikeTheOption() {
        calendar.setTimezone(Timezone.UTC);

        assertEquals(Timezone.UTC, calendar.getOption(Option.TIMEZONE).orElseThrow());
    }

    @Test
    void timezone_afterAttach_sendsId() {
        Serializable sent = clientValueAfterAttach(Option.TIMEZONE, () -> calendar.setOption(Option.TIMEZONE, "Europe/Berlin"));

        assertEquals("Europe/Berlin", sent);
    }

    // -------------------------------------------------------------------------
    // Valid range
    // -------------------------------------------------------------------------

    @Test
    void validRange_beforeAttach_sendsStartAndEnd() {
        calendar.setOption(Option.VALID_RANGE, new DateRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 1)));

        JsonNode json = initialClientValue(Option.VALID_RANGE);
        assertEquals("2025-03-01", json.get("start").asString());
        assertEquals("2025-05-01", json.get("end").asString());
    }

    @Test
    void validRange_openEnd_leavesEndOut() {
        calendar.setOption(Option.VALID_RANGE, new DateRange(LocalDate.of(2025, 3, 1), null));

        JsonNode json = initialClientValue(Option.VALID_RANGE);
        assertEquals("2025-03-01", json.get("start").asString());
        assertFalse(json.has("end"));
    }

    @Test
    void validRange_afterAttach_sendsStartAndEnd() {
        Serializable sent = clientValueAfterAttach(Option.VALID_RANGE,
                () -> calendar.setOption(Option.VALID_RANGE, new DateRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 1))));

        JsonNode json = (JsonNode) sent;
        assertEquals("2025-03-01", json.get("start").asString());
        assertEquals("2025-05-01", json.get("end").asString());
    }

    @Test
    void validRange_setterAndOption_storeSameValue() {
        calendar.setValidRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 1));

        assertEquals(new DateRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 1)),
                calendar.getOption(Option.VALID_RANGE).orElseThrow());
    }

    @Test
    void validRange_startAndEndSetters_setOpenRanges() {
        calendar.setValidRangeStart(LocalDate.of(2025, 3, 1));
        assertEquals(new DateRange(LocalDate.of(2025, 3, 1), null), calendar.getOption(Option.VALID_RANGE).orElseThrow());

        calendar.setValidRangeEnd(LocalDate.of(2025, 5, 1));
        assertEquals(new DateRange(null, LocalDate.of(2025, 5, 1)), calendar.getOption(Option.VALID_RANGE).orElseThrow());
        assertFalse(initialClientValue(Option.VALID_RANGE).has("start"));
    }

    @Test
    void validRange_clear_removesOption() {
        calendar.setValidRangeStart(LocalDate.of(2025, 3, 1));

        calendar.clearValidRange();

        assertTrue(calendar.getOption(Option.VALID_RANGE).isEmpty());
        assertNull(initialClientValue(Option.VALID_RANGE));
    }

    @Test
    void dateRange_startNotBeforeEnd_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new DateRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 1)));
    }

    // -------------------------------------------------------------------------
    // More-link click action
    // -------------------------------------------------------------------------

    @Test
    void moreLinkClick_action_sendsClientValue() {
        calendar.setOption(Option.MORE_LINK_CLICK, FullCalendar.MoreLinkClickAction.DAY);

        assertEquals("day", initialClientValue(Option.MORE_LINK_CLICK).asString());
        assertEquals(FullCalendar.MoreLinkClickAction.DAY, calendar.getOption(Option.MORE_LINK_CLICK).orElseThrow());
    }

    @Test
    void moreLinkClick_setterAndOption_storeSameValue_withoutElementProperty() {
        calendar.setMoreLinkClickAction(FullCalendar.MoreLinkClickAction.WEEK);

        assertEquals(FullCalendar.MoreLinkClickAction.WEEK, calendar.getOption(Option.MORE_LINK_CLICK).orElseThrow());
        assertEquals("week", initialClientValue(Option.MORE_LINK_CLICK).asString());
        assertFalse(calendar.getElement().hasProperty("moreLinkClickAction"));

        // null removes the option, the client then falls back to FullCalendar's "popover"
        calendar.setMoreLinkClickAction(null);
        assertTrue(calendar.getOption(Option.MORE_LINK_CLICK).isEmpty());
        assertNull(initialClientValue(Option.MORE_LINK_CLICK));
    }

    @Test
    void moreLinkClick_afterAttach_sendsClientValue() {
        Serializable sent = clientValueAfterAttach(Option.MORE_LINK_CLICK,
                () -> calendar.setOption(Option.MORE_LINK_CLICK, FullCalendar.MoreLinkClickAction.DAY));

        assertEquals("day", sent);
    }

    // -------------------------------------------------------------------------
    // Day max entries
    // -------------------------------------------------------------------------

    @Test
    void dayMaxEntries_setterAndOption_storeSameValue() {
        calendar.setMaxEntriesPerDay(3);
        assertEquals(3, calendar.getOption(Option.DAY_MAX_ENTRIES).orElseThrow());
        assertEquals(3, initialClientValue(Option.DAY_MAX_ENTRIES).asInt());

        calendar.setMaxEntriesPerDayFitToCell();
        assertEquals(true, calendar.getOption(Option.DAY_MAX_ENTRIES).orElseThrow());
        assertTrue(initialClientValue(Option.DAY_MAX_ENTRIES).isBoolean());
        assertTrue(initialClientValue(Option.DAY_MAX_ENTRIES).booleanValue());

        calendar.setMaxEntriesPerDayUnlimited();
        assertEquals(false, calendar.getOption(Option.DAY_MAX_ENTRIES).orElseThrow());
        assertTrue(initialClientValue(Option.DAY_MAX_ENTRIES).isBoolean());
        assertFalse(initialClientValue(Option.DAY_MAX_ENTRIES).booleanValue());
    }
}
