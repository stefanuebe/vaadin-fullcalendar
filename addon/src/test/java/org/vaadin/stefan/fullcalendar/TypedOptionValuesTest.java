package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Typed option values (time zone, valid range, more-link click action, day max entries) reach the client the same
 * way before and after attach, and the deprecated setters store the same value as the option (#259). The same holds
 * for the typed values of the FullCalendar 7 options (#264), including callbacks nested in a map like the buttons.
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
    private JsonNode clientValueAfterAttach(Option option, Runnable setter) {
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
                return (JsonNode) parameters.get(keyIndex + 1);
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
        JsonNode sent = clientValueAfterAttach(Option.TIMEZONE, () -> calendar.setOption(Option.TIMEZONE, "Europe/Berlin"));

        assertEquals("Europe/Berlin", sent.asString());
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
        JsonNode json = clientValueAfterAttach(Option.VALID_RANGE,
                () -> calendar.setOption(Option.VALID_RANGE, new DateRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 1))));

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
        JsonNode sent = clientValueAfterAttach(Option.MORE_LINK_CLICK,
                () -> calendar.setOption(Option.MORE_LINK_CLICK, FullCalendar.MoreLinkClickAction.DAY));

        assertEquals("day", sent.asString());
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

    // -------------------------------------------------------------------------
    // Buttons: callbacks nested in the map
    // -------------------------------------------------------------------------

    private static Map<String, Object> buttons() {
        return Map.of(
                ToolbarParts.TODAY, Map.of("text", "Now", "display", ButtonDisplay.TEXT),
                "hello", Map.of("text", "Hello", "click", JsCallback.of("function() { window.helloClicked = true; }")));
    }

    private static void assertButtonsJson(JsonNode json) {
        assertEquals("Now", json.get("today").get("text").asString());
        assertEquals("text", json.get("today").get("display").asString());
        assertEquals("function() { window.helloClicked = true; }",
                json.get("hello").get("click").get("__jsCallback").asString());
    }

    @Test
    void buttons_beforeAttach_sendsNestedCallbacksAsMarkers() {
        calendar.setOption(Option.BUTTONS, buttons());

        assertButtonsJson(initialClientValue(Option.BUTTONS));
        assertEquals(buttons(), calendar.getOption(Option.BUTTONS).orElseThrow());
    }

    @Test
    void buttons_afterAttach_sendsNestedCallbacksAsMarkers() {
        JsonNode sent = clientValueAfterAttach(Option.BUTTONS, () -> calendar.setOption(Option.BUTTONS, buttons()));

        assertButtonsJson(sent);
        assertEquals(buttons(), calendar.getOption(Option.BUTTONS).orElseThrow());
    }

    /** A value type without own conversion, Jackson serializes it as an object. */
    public record Badge(String label, int count) {
    }

    @Test
    void objectInAMap_isSentAsJsonObject_beforeAndAfterAttach() {
        Map<String, Object> buttons = Map.of("inbox", Map.of("text", "Inbox", "badge", new Badge("new", 3)));

        calendar.setOption(Option.BUTTONS, buttons);
        JsonNode before = initialClientValue(Option.BUTTONS).get("inbox").get("badge");
        JsonNode after = clientValueAfterAttach(Option.BUTTONS, () -> calendar.setOption(Option.BUTTONS, buttons))
                .get("inbox").get("badge");

        assertEquals("new", before.get("label").asString());
        assertEquals(3, before.get("count").asInt());
        assertEquals(before, after);
    }

    @Test
    void callbackInACollection_isSentAsMarker_beforeAndAfterAttach() {
        List<Object> elements = List.of(JsCallback.of("function() { return 'x'; }"));

        calendar.setOption(Option.TOOLBAR_ELEMENTS, elements);
        JsonNode before = initialClientValue(Option.TOOLBAR_ELEMENTS);
        JsonNode after = clientValueAfterAttach(Option.TOOLBAR_ELEMENTS,
                () -> calendar.setOption(Option.TOOLBAR_ELEMENTS, elements));

        assertEquals("function() { return 'x'; }", before.get(0).get("__jsCallback").asString());
        assertEquals(before, after);
    }

    @Test
    void objectInAMap_isSentAsJsonObject_onReattach() {
        UI ui = UI.getCurrent();
        ui.add(calendar);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        calendar.setOption(Option.BUTTONS, Map.of("inbox", Map.of("badge", new Badge("new", 3))));
        ui.remove(calendar);
        ui.getInternals().dumpPendingJavaScriptInvocations();

        ui.add(calendar);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        JsonNode restored = ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression().contains("restoreStateFromServer"))
                .map(invocation -> (JsonNode) invocation.getParameters().get(1))
                .findFirst().orElseGet(() -> fail("no restoreStateFromServer call"));
        assertEquals("new", restored.get("buttons").get("inbox").get("badge").get("label").asString());
    }

    @Test
    void objectInAMap_isSentAsJsonObject_inViewSpecificOptions() {
        calendar.setViewSpecificOption(CalendarViewImpl.DAY_GRID_MONTH, Option.BUTTONS,
                Map.of("inbox", Map.of("badge", new Badge("new", 3))));

        JsonNode views = ((JsonNode) calendar.getElement().getPropertyRaw("initialOptions")).get("views");
        JsonNode badge = views.get("dayGridMonth").get("buttons").get("inbox").get("badge");
        assertEquals(3, badge.get("count").asInt());
    }

    // -------------------------------------------------------------------------
    // Enum values of the FullCalendar 7 options
    // -------------------------------------------------------------------------

    @Test
    void buttonDisplay_sendsClientValue() {
        calendar.setOption(Option.BUTTON_DISPLAY, ButtonDisplay.ICON_TEXT);

        assertEquals("icon-text", initialClientValue(Option.BUTTON_DISPLAY).asString());
        assertEquals(ButtonDisplay.ICON_TEXT, calendar.getOption(Option.BUTTON_DISPLAY).orElseThrow());
    }

    @Test
    void headerAlign_afterAttach_sendsClientValue() {
        JsonNode sent = clientValueAfterAttach(Option.DAY_HEADER_ALIGN,
                () -> calendar.setOption(Option.DAY_HEADER_ALIGN, HeaderAlign.CENTER));

        assertEquals("center", sent.asString());
        assertEquals(HeaderAlign.CENTER, calendar.getOption(Option.DAY_HEADER_ALIGN).orElseThrow());
    }

    // -------------------------------------------------------------------------
    // Converted values of the FullCalendar 7 options
    // -------------------------------------------------------------------------

    @Test
    void visibleRange_sendsStartAndEnd() {
        calendar.setOption(Option.VISIBLE_RANGE, new DateRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 15)));

        JsonNode json = initialClientValue(Option.VISIBLE_RANGE);
        assertEquals("2025-03-01", json.get("start").asString());
        assertEquals("2025-03-15", json.get("end").asString());
    }

    @Test
    void now_sendsIsoDateOrUtcDateTime() {
        calendar.setOption(Option.NOW, LocalDate.of(2025, 3, 10));
        assertEquals("2025-03-10", initialClientValue(Option.NOW).asString());

        LocalDateTime now = LocalDateTime.of(2025, 3, 10, 9, 30);
        calendar.setOption(Option.NOW, now);
        assertEquals("2025-03-10T09:30Z", initialClientValue(Option.NOW).asString());
        assertEquals(now, calendar.getOption(Option.NOW).orElseThrow());
    }

    @Test
    void defaultTimedEntryDuration_sendsDurationString() {
        calendar.setOption(Option.DEFAULT_TIMED_ENTRY_DURATION, Duration.ofMinutes(90));

        assertEquals("01:30:00", initialClientValue(Option.DEFAULT_TIMED_ENTRY_DURATION).asString());
    }
}
