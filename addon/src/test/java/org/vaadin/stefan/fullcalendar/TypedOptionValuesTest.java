package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.ContentValue;
import org.vaadin.stefan.fullcalendar.option.DateRange;
import org.vaadin.stefan.fullcalendar.option.EntryLimitValue;
import org.vaadin.stefan.fullcalendar.option.HeaderAlign;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.MoreLinkClickValue;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarButtonDisplay;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarParts;
import org.vaadin.stefan.fullcalendar.option.NoneValue;
import org.vaadin.stefan.fullcalendar.option.NowValue;
import org.vaadin.stefan.fullcalendar.option.Timezone;
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
 * way before and after attach (#259). The same holds for the typed values of the newer library options (#264),
 * including callbacks nested in a map like the buttons.
 */
public class TypedOptionValuesTest extends BrowserlessTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    /**
     * Client value of the option in the initial options, which the client reads on attach. The options reach the
     * property with the response, so this attaches the calendar. Later changes do not update the property, a test
     * with several steps takes a new calendar for each.
     */
    private JsonNode initialClientValue(String key) {
        return initialClientValue(calendar, key);
    }

    private static JsonNode initialClientValue(FullCalendar calendar, String key) {
        UI ui = UI.getCurrent();
        ui.add(calendar);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        JsonNode initialOptions = (JsonNode) calendar.getElement().getPropertyRaw("initialOptions");
        return initialOptions.get(key);
    }

    /** Attaches the calendar and returns the client value of the option sent by the setter. */
    private JsonNode clientValueAfterAttach(String key, Runnable setter) {
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
            if (invocation.getInvocation().getExpression().contains("setOptions")
                    && parameters.getLast() instanceof JsonNode changes && changes.has(key)) {
                return changes.get(key);
            }
        }
        return fail("no setOptions call for " + key);
    }

    // -------------------------------------------------------------------------
    // Time zone
    // -------------------------------------------------------------------------

    @Test
    void timezone_isStoredAndSent() {
        Timezone expected = new Timezone(ZoneId.of("Europe/Berlin"));
        calendar.options().setTimeZone(expected);

        assertEquals(expected, calendar.options().getTimeZone());
        assertEquals("Europe/Berlin", initialClientValue("timeZone").asString());
    }

    @Test
    void timezone_utcId_sendsUtc() {
        calendar.options().setTimeZone(new Timezone(ZoneId.of("UTC")));

        assertEquals(Timezone.UTC, calendar.options().getTimeZone());
        assertEquals("UTC", initialClientValue("timeZone").asString());
    }

    @Test
    void timezone_getter_returnsLocalBeforeTheBrowserReports() {
        assertEquals(Timezone.LOCAL, calendar.options().getTimeZone());
    }

    @Test
    void timezone_afterAttach_sendsId() {
        JsonNode sent = clientValueAfterAttach("timeZone",
                () -> calendar.options().setTimeZone(new Timezone(ZoneId.of("Europe/Berlin"))));

        assertEquals("Europe/Berlin", sent.asString());
    }

    // -------------------------------------------------------------------------
    // Valid range
    // -------------------------------------------------------------------------

    @Test
    void validRange_beforeAttach_sendsStartAndEnd() {
        calendar.options().setValidRange(new DateRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 1)));

        JsonNode json = initialClientValue("validRange");
        assertEquals("2025-03-01", json.get("start").asString());
        assertEquals("2025-05-01", json.get("end").asString());
    }

    @Test
    void validRange_openEnd_leavesEndOut() {
        calendar.options().setValidRange(new DateRange(LocalDate.of(2025, 3, 1), null));

        JsonNode json = initialClientValue("validRange");
        assertEquals("2025-03-01", json.get("start").asString());
        assertFalse(json.has("end"));
    }

    @Test
    void validRange_afterAttach_sendsStartAndEnd() {
        JsonNode json = clientValueAfterAttach("validRange", () -> calendar.options()
                .setValidRange(new DateRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 1))));

        assertEquals("2025-03-01", json.get("start").asString());
        assertEquals("2025-05-01", json.get("end").asString());
    }

    @Test
    void validRange_null_removesOption() {
        calendar.options().setValidRange(new DateRange(LocalDate.of(2025, 3, 1), null));

        calendar.options().setValidRange(null);

        assertNull(calendar.options().getValidRange());
        assertNull(initialClientValue("validRange"));
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
        calendar.options().setMoreLinkClick(MoreLinkClickValue.Action.DAY);

        assertEquals("day", initialClientValue("moreLinkClick").asString());
        assertEquals(MoreLinkClickValue.Action.DAY, calendar.options().getMoreLinkClick());
    }

    @Test
    void moreLinkClick_null_removesOption() {
        FullCalendar week = new FullCalendar();
        week.options().setMoreLinkClick(MoreLinkClickValue.Action.WEEK);
        assertEquals("week", initialClientValue(week, "moreLinkClick").asString());

        // null removes the option, the client then falls back to FullCalendar's "popover"
        calendar.options().setMoreLinkClick(MoreLinkClickValue.Action.WEEK);
        calendar.options().setMoreLinkClick((MoreLinkClickValue) null);
        assertNull(calendar.options().getMoreLinkClick());
        assertNull(initialClientValue("moreLinkClick"));
    }

    @Test
    void moreLinkClick_afterAttach_sendsClientValue() {
        JsonNode sent = clientValueAfterAttach("moreLinkClick",
                () -> calendar.options().setMoreLinkClick(MoreLinkClickValue.Action.DAY));

        assertEquals("day", sent.asString());
    }

    // -------------------------------------------------------------------------
    // Day max entries
    // -------------------------------------------------------------------------

    @Test
    void dayMaxEntries_values_reachTheClient() {
        calendar.options().setDayMaxEntries(3);
        assertEquals(EntryLimitValue.of(3), calendar.options().getDayMaxEntries());
        assertEquals(3, initialClientValue("dayMaxEvents").asInt());

        FullCalendar cellHeight = new FullCalendar();
        cellHeight.options().setDayMaxEntries(EntryLimitValue.CELL_HEIGHT);
        assertTrue(initialClientValue(cellHeight, "dayMaxEvents").isBoolean());
        assertTrue(initialClientValue(cellHeight, "dayMaxEvents").booleanValue());

        FullCalendar none = new FullCalendar();
        none.options().setDayMaxEntries(NoneValue.NONE);
        assertTrue(initialClientValue(none, "dayMaxEvents").isBoolean());
        assertFalse(initialClientValue(none, "dayMaxEvents").booleanValue());
    }

    // -------------------------------------------------------------------------
    // Buttons: callbacks nested in the map
    // -------------------------------------------------------------------------

    private static Map<String, Map<String, Object>> buttons() {
        return Map.of(
                NativeToolbarParts.TODAY, Map.of("text", "Now", "display", NativeToolbarButtonDisplay.TEXT),
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
        calendar.options().setButtons(buttons());

        assertButtonsJson(initialClientValue("buttons"));
        assertEquals(buttons(), calendar.options().getButtons());
    }

    @Test
    void buttons_afterAttach_sendsNestedCallbacksAsMarkers() {
        JsonNode sent = clientValueAfterAttach("buttons", () -> calendar.options().setButtons(buttons()));

        assertButtonsJson(sent);
        assertEquals(buttons(), calendar.options().getButtons());
    }

    /** A value type without own conversion, Jackson serializes it as an object. */
    public record Badge(String label, int count) {
    }

    @Test
    void objectInAMap_isSentAsJsonObject_beforeAndAfterAttach() {
        Map<String, Map<String, Object>> buttons = Map.of("inbox", Map.of("text", "Inbox", "badge", new Badge("new", 3)));

        // the typed field has no conversion for such a value, so the raw option carries it
        calendar.options().setRawOption("buttons", buttons);
        JsonNode before = initialClientValue("buttons").get("inbox").get("badge");
        calendar.options().setRawOption("buttons", null); // only a change reaches an attached calendar
        JsonNode after = clientValueAfterAttach("buttons", () -> calendar.options().setRawOption("buttons", buttons))
                .get("inbox").get("badge");

        assertEquals("new", before.get("label").asString());
        assertEquals(3, before.get("count").asInt());
        assertEquals(before, after);
    }

    @Test
    void callbackInAMap_isSentAsMarker_beforeAndAfterAttach() {
        Map<String, ContentValue> elements = Map.of("x", JsCallback.of("function() { return 'x'; }"));

        calendar.options().setToolbarElements(elements);
        JsonNode before = initialClientValue("toolbarElements");
        calendar.options().setToolbarElements(null); // only a change reaches an attached calendar
        JsonNode after = clientValueAfterAttach("toolbarElements",
                () -> calendar.options().setToolbarElements(elements));

        assertEquals("function() { return 'x'; }", before.get("x").get("__jsCallback").asString());
        assertEquals(before, after);
    }

    @Test
    void objectInAMap_isSentAsJsonObject_onReattach() {
        UI ui = UI.getCurrent();
        ui.add(calendar);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        calendar.options().setRawOption("buttons", Map.of("inbox", Map.of("badge", new Badge("new", 3))));
        ui.remove(calendar);
        ui.getInternals().dumpPendingJavaScriptInvocations();

        ui.add(calendar);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        JsonNode restored = ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression().contains("setOptions"))
                .map(invocation -> (JsonNode) invocation.getParameters().getLast())
                .findFirst().orElseGet(() -> fail("no setOptions call"));
        assertEquals("new", restored.get("buttons").get("inbox").get("badge").get("label").asString());
    }

    @Test
    void objectInAMap_isSentAsJsonObject_inViewSpecificOptions() {
        calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setRawOption("buttons",
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
        calendar.options().setButtonDisplay(NativeToolbarButtonDisplay.ICON_TEXT);

        assertEquals("icon-text", initialClientValue("buttonDisplay").asString());
        assertEquals(NativeToolbarButtonDisplay.ICON_TEXT, calendar.options().getButtonDisplay());
    }

    @Test
    void headerAlign_afterAttach_sendsClientValue() {
        JsonNode sent = clientValueAfterAttach("dayHeaderAlign",
                () -> calendar.options().setDayHeaderAlign(HeaderAlign.CENTER));

        assertEquals("center", sent.asString());
        assertEquals(HeaderAlign.CENTER, calendar.options().getDayHeaderAlign());
    }

    // -------------------------------------------------------------------------
    // Converted values of the FullCalendar 7 options
    // -------------------------------------------------------------------------

    @Test
    void visibleRange_sendsStartAndEnd() {
        calendar.options().setVisibleRange(new DateRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 15)));

        JsonNode json = initialClientValue("visibleRange");
        assertEquals("2025-03-01", json.get("start").asString());
        assertEquals("2025-03-15", json.get("end").asString());
    }

    @Test
    void now_sendsIsoDateOrUtcDateTime() {
        calendar.options().setNow(LocalDate.of(2025, 3, 10));
        assertEquals("2025-03-10", initialClientValue("now").asString());

        LocalDateTime now = LocalDateTime.of(2025, 3, 10, 9, 30);
        FullCalendar withTime = new FullCalendar();
        withTime.options().setNow(now);
        assertEquals("2025-03-10T09:30Z", initialClientValue(withTime, "now").asString());
        assertEquals(NowValue.ofDateTime(now), withTime.options().getNow());
    }

    @Test
    void defaultTimedEntryDuration_sendsDurationString() {
        calendar.options().setDefaultTimedEntryDuration(Duration.ofMinutes(90));

        JsonNode json = initialClientValue("defaultTimedEventDuration");
        assertEquals(1, json.get("hours").asInt());
        assertEquals(30, json.get("minutes").asInt());
    }
}
