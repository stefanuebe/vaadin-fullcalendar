package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.TestUtils.assertOptionalEquals;

/**
 * Tests for advanced and niche options.
 * Covers buttonIcons, eventConstraint, dateIncrement/dateAlignment, CSP nonce,
 * view-specific options, fixedMirrorParent, dragScrollEls, validRange/selectOverlap callbacks,
 * navigation methods, getCurrentIntervalStart/End, and custom buttons.
 */
public class AdvancedOptionsTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    // -------------------------------------------------------------------------
    // Option enum keys
    // -------------------------------------------------------------------------

    @Test
    void option_buttonIcons_key() {
        assertEquals("buttonIcons", Option.BUTTON_ICONS.getOptionKey());
    }

    @Test
    void option_eventConstraint_key() {
        assertEquals("eventConstraint", Option.EVENT_CONSTRAINT.getOptionKey());
    }

    @Test
    void option_dateIncrement_key() {
        assertEquals("dateIncrement", Option.DATE_INCREMENT.getOptionKey());
    }

    @Test
    void option_dateAlignment_key() {
        assertEquals("dateAlignment", Option.DATE_ALIGNMENT.getOptionKey());
    }

    @Test
    void option_contentSecurityPolicy_key() {
        assertEquals("contentSecurityPolicy", Option.CONTENT_SECURITY_POLICY.getOptionKey());
    }

    @Test
    void option_dragScrollEls_key() {
        assertEquals("dragScrollEls", Option.DRAG_SCROLL_ELS.getOptionKey());
    }

    // -------------------------------------------------------------------------
    // 7.2 buttonIcons
    // -------------------------------------------------------------------------

    @Test
    void setButtonIcons_storesOption() {
        Map<String, String> icons = Map.of("prev", "chevron-left", "next", "chevron-right");
        calendar.setButtonIcons(icons);
        assertOptionalEquals(icons, calendar.getOption(Option.BUTTON_ICONS));
    }

    @Test
    void setButtonIcons_null_clearsOption() {
        calendar.setButtonIcons(Map.of("prev", "chevron-left"));
        calendar.setButtonIcons(null);
        assertTrue(calendar.getOption(Option.BUTTON_ICONS).isEmpty());
    }

    @Test
    void setButtonIcons_emptyMap_storesEmptyOption() {
        calendar.setButtonIcons(Map.of());
        // An empty map should be stored (not treated as null)
        assertOptionalEquals(Map.of(), calendar.getOption(Option.BUTTON_ICONS));
    }

    // -------------------------------------------------------------------------
    // 7.6 eventConstraint
    // -------------------------------------------------------------------------

    @Test
    void setEventConstraint_string_storesOption() {
        calendar.setEventConstraint("myGroup");
        assertOptionalEquals("myGroup", calendar.getOption(Option.EVENT_CONSTRAINT));
    }

    @Test
    void setEventConstraint_businessHours_storesJsonNode() {
        BusinessHours hours = BusinessHours.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
        calendar.setEventConstraint(hours);
        Optional<Object> opt = calendar.getOption(Option.EVENT_CONSTRAINT);
        assertTrue(opt.isPresent());
        // stored as ObjectNode
        assertInstanceOf(ObjectNode.class, opt.get());
    }

    @Test
    void setEventConstraintToBusinessHours_storesString() {
        calendar.setEventConstraintToBusinessHours();
        assertOptionalEquals("businessHours", calendar.getOption(Option.EVENT_CONSTRAINT));
    }

    @Test
    void setEventConstraint_null_clearsOption() {
        calendar.setEventConstraint("myGroup");
        calendar.setEventConstraint((String) null);
        assertTrue(calendar.getOption(Option.EVENT_CONSTRAINT).isEmpty());
    }

    @Test
    void setEventConstraint_businessHours_null_throws() {
        assertThrows(NullPointerException.class, () -> calendar.setEventConstraint((BusinessHours) null));
    }

    // -------------------------------------------------------------------------
    // 7.7 dateIncrement and dateAlignment
    // -------------------------------------------------------------------------

    @Test
    void setDateIncrement_storesOption() {
        calendar.setDateIncrement("P1W");
        assertOptionalEquals("P1W", calendar.getOption(Option.DATE_INCREMENT));
    }

    @Test
    void setDateIncrement_null_clearsOption() {
        calendar.setDateIncrement("P1W");
        calendar.setDateIncrement(null);
        assertTrue(calendar.getOption(Option.DATE_INCREMENT).isEmpty());
    }

    @Test
    void setDateAlignment_storesOption() {
        calendar.setDateAlignment("week");
        assertOptionalEquals("week", calendar.getOption(Option.DATE_ALIGNMENT));
    }

    @Test
    void setDateAlignment_null_clearsOption() {
        calendar.setDateAlignment("week");
        calendar.setDateAlignment(null);
        assertTrue(calendar.getOption(Option.DATE_ALIGNMENT).isEmpty());
    }

    // -------------------------------------------------------------------------
    // 7.12 CSP nonce
    // -------------------------------------------------------------------------

    @Test
    void setContentSecurityPolicyNonce_storesMapOption() {
        calendar.setContentSecurityPolicyNonce("abc123");
        Optional<Object> opt = calendar.getOption(Option.CONTENT_SECURITY_POLICY);
        assertTrue(opt.isPresent());
        Map<?, ?> map = (Map<?, ?>) opt.get();
        assertEquals("abc123", map.get("nonce"));
    }

    @Test
    void setContentSecurityPolicyNonce_null_clearsOption() {
        calendar.setContentSecurityPolicyNonce("abc123");
        calendar.setContentSecurityPolicyNonce(null);
        assertTrue(calendar.getOption(Option.CONTENT_SECURITY_POLICY).isEmpty());
    }

    // -------------------------------------------------------------------------
    // 7.13 setViewSpecificOption
    // -------------------------------------------------------------------------

    @Test
    void setViewSpecificOption_string_key_storesNestedJson() {
        calendar.setViewSpecificOption("dayGridMonth", "dayMaxEventRows", 3);
        Optional<Object> opt = calendar.getOption("views");
        assertTrue(opt.isPresent());
        ObjectNode views = (ObjectNode) opt.get();
        assertTrue(views.has("dayGridMonth"));
        assertEquals(3, views.get("dayGridMonth").get("dayMaxEventRows").asInt());
    }

    @Test
    void setViewSpecificOption_option_enum_storesNestedJson() {
        calendar.setViewSpecificOption("timeGrid", Option.SLOT_DURATION, "00:30:00");
        Optional<Object> opt = calendar.getOption("views");
        assertTrue(opt.isPresent());
        ObjectNode views = (ObjectNode) opt.get();
        assertTrue(views.has("timeGrid"));
        assertEquals("00:30:00", views.get("timeGrid").get("slotDuration").asText());
    }

    @Test
    void setViewSpecificOption_calendarView_enum_storesNestedJson() {
        calendar.setViewSpecificOption(CalendarViewImpl.DAY_GRID_MONTH, Option.NOW_INDICATOR, true);
        Optional<Object> opt = calendar.getOption("views");
        assertTrue(opt.isPresent());
        ObjectNode views = (ObjectNode) opt.get();
        String viewKey = CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue();
        assertTrue(views.has(viewKey));
        assertTrue(views.get(viewKey).get("nowIndicator").asBoolean());
    }

    @Test
    void setViewSpecificOption_multipleViews_storesSeparately() {
        calendar.setViewSpecificOption("dayGrid", "dayMaxEventRows", 3);
        calendar.setViewSpecificOption("timeGrid", "slotDuration", "00:30:00");
        ObjectNode views = (ObjectNode) calendar.getOption("views").orElseThrow();
        assertTrue(views.has("dayGrid"));
        assertTrue(views.has("timeGrid"));
        assertEquals(3, views.get("dayGrid").get("dayMaxEventRows").asInt());
        assertEquals("00:30:00", views.get("timeGrid").get("slotDuration").asText());
    }

    @Test
    void setViewSpecificOptions_map_storesAllEntries() {
        calendar.setViewSpecificOptions("listWeek", Map.of("noEventsText", "None", "listDaySideFormat", "DD"));
        ObjectNode views = (ObjectNode) calendar.getOption("views").orElseThrow();
        assertTrue(views.has("listWeek"));
        assertEquals("None", views.get("listWeek").get("noEventsText").asText());
    }

    @Test
    void setViewSpecificOption_null_value_removesEntireViewNode() {
        // Set an option and then null it — the view node should be cleaned up entirely
        calendar.setViewSpecificOption("dayGrid", "dayMaxEventRows", 3);
        calendar.setViewSpecificOption("dayGrid", "dayMaxEventRows", null);
        // Implementation removes the view node when it becomes empty, so views is empty/absent
        Optional<Object> opt = calendar.getOption("views");
        opt.ifPresent(v -> {
            ObjectNode views = (ObjectNode) v;
            assertFalse(views.has("dayGrid"), "empty dayGrid view node must be removed");
        });
    }

    @Test
    void setViewSpecificOption_multipleKeys_removeOneKeepOther() {
        calendar.setViewSpecificOption("dayGrid", "dayMaxEventRows", 3);
        calendar.setViewSpecificOption("dayGrid", "nowIndicator", true);
        // Remove only one key
        calendar.setViewSpecificOption("dayGrid", "dayMaxEventRows", null);
        ObjectNode views = (ObjectNode) calendar.getOption("views").orElseThrow();
        assertTrue(views.has("dayGrid"), "dayGrid node must remain because nowIndicator is still set");
        assertFalse(views.get("dayGrid").has("dayMaxEventRows"), "removed key must be absent");
        assertTrue(views.get("dayGrid").has("nowIndicator"), "remaining key must still be present");
    }

    @Test
    void setViewSpecificOption_viewType_null_throws() {
        assertThrows(NullPointerException.class,
                () -> calendar.setViewSpecificOption((String) null, "dayMaxEventRows", 3));
    }

    @Test
    void setViewSpecificOption_optionKey_null_throws() {
        assertThrows(NullPointerException.class,
                () -> calendar.setViewSpecificOption("dayGrid", (String) null, 3));
    }

    // -------------------------------------------------------------------------
    // 7.15 dragScrollEls
    // -------------------------------------------------------------------------

    @Test
    void setDragScrollEls_single_storesJoinedString() {
        calendar.setDragScrollEls(".my-scroller");
        assertOptionalEquals(".my-scroller", calendar.getOption(Option.DRAG_SCROLL_ELS));
    }

    @Test
    void setDragScrollEls_multiple_storesJoinedString() {
        calendar.setDragScrollEls(".scroll-a", "body");
        assertOptionalEquals(".scroll-a,body", calendar.getOption(Option.DRAG_SCROLL_ELS));
    }

    @Test
    void setDragScrollEls_null_clearsOption() {
        calendar.setDragScrollEls(".scroller");
        calendar.setDragScrollEls((String[]) null);
        assertTrue(calendar.getOption(Option.DRAG_SCROLL_ELS).isEmpty());
    }

    @Test
    void setDragScrollEls_empty_clearsOption() {
        calendar.setDragScrollEls(".scroller");
        calendar.setDragScrollEls();
        assertTrue(calendar.getOption(Option.DRAG_SCROLL_ELS).isEmpty());
    }

    // -------------------------------------------------------------------------
    // 7.9 getCurrentIntervalStart / getCurrentIntervalEnd
    // -------------------------------------------------------------------------

    @Test
    void getCurrentIntervalStart_emptyBeforeFirstRender() {
        // Fresh calendar: no render event fired yet
        assertTrue(calendar.getCurrentIntervalStart().isEmpty());
    }

    @Test
    void getCurrentIntervalEnd_emptyBeforeFirstRender() {
        assertTrue(calendar.getCurrentIntervalEnd().isEmpty());
    }

    // -------------------------------------------------------------------------
    // 7.1 CustomButton model
    // -------------------------------------------------------------------------

    @Test
    void customButton_toJson_includesAllSetFields() {
        CustomButton btn = new CustomButton("myBtn");
        btn.setText("Click Me");
        btn.setHint("A helpful hint");
        btn.setIcon("fa-star");

        ObjectNode json = btn.toJson();
        assertEquals("Click Me", json.get("text").asText());
        assertEquals("A helpful hint", json.get("hint").asText());
        assertEquals("fa-star", json.get("icon").asText());
        assertFalse(json.has("bootstrapFontAwesome"));
        assertFalse(json.has("themeIcon"));
    }

    @Test
    void customButton_toJson_emptyWhenNoFieldsSet() {
        CustomButton btn = new CustomButton("myBtn");
        ObjectNode json = btn.toJson();
        assertTrue(json.isEmpty());
    }

    @Test
    void customButton_getName_returnsName() {
        assertEquals("myBtn", new CustomButton("myBtn").getName());
    }

    @Test
    void customButton_nullName_throws() {
        assertThrows(NullPointerException.class, () -> new CustomButton(null));
    }

    @Test
    void customButton_equals_byName() {
        CustomButton a = new CustomButton("btn");
        a.setText("Alpha");
        CustomButton b = new CustomButton("btn");
        b.setText("Beta");
        assertEquals(a, b);
    }

    // -------------------------------------------------------------------------
    // 7.1 addCustomButton / removeCustomButton
    // -------------------------------------------------------------------------

    @Test
    void addCustomButton_null_throws() {
        assertThrows(NullPointerException.class, () -> calendar.addCustomButton(null));
    }

    @Test
    void addCustomButtonClickedListener_nullButtonName_throws() {
        assertThrows(NullPointerException.class,
                () -> calendar.addCustomButtonClickedListener(null, e -> {}));
    }

    @Test
    void addCustomButtonClickedListener_nullListener_throws() {
        assertThrows(NullPointerException.class,
                () -> calendar.addCustomButtonClickedListener("btn", null));
    }

    @Test
    void addCustomButton_withListener_registrationRemovesListener() {
        CustomButton btn = new CustomButton("btn");
        AtomicInteger counter = new AtomicInteger();
        Registration reg = calendar.addCustomButton(btn, e -> counter.incrementAndGet());

        // Simulate click (directly call the @ClientCallable method via reflection)
        simulateCustomButtonClick("btn");
        assertEquals(1, counter.get());

        // Remove listener
        reg.remove();
        simulateCustomButtonClick("btn");
        assertEquals(1, counter.get()); // no further increment
    }

    @Test
    void removeCustomButton_removesListeners() {
        CustomButton btn = new CustomButton("btn");
        AtomicInteger counter = new AtomicInteger();
        calendar.addCustomButton(btn, e -> counter.incrementAndGet());

        calendar.removeCustomButton("btn");
        simulateCustomButtonClick("btn");
        assertEquals(0, counter.get());
    }

    @Test
    void removeCustomButton_null_throws() {
        assertThrows(NullPointerException.class, () -> calendar.removeCustomButton(null));
    }

    @Test
    void customButtonClicked_firesAllListeners() {
        CustomButton btn = new CustomButton("btn");
        AtomicInteger counter = new AtomicInteger();
        calendar.addCustomButton(btn);
        calendar.addCustomButtonClickedListener("btn", e -> counter.incrementAndGet());
        calendar.addCustomButtonClickedListener("btn", e -> counter.incrementAndGet());

        simulateCustomButtonClick("btn");
        assertEquals(2, counter.get());
    }

    @Test
    void customButtonClickedListener_selectiveRemoval_removesOnlyThatListener() {
        CustomButton btn = new CustomButton("btn");
        calendar.addCustomButton(btn);
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();
        Registration reg1 = calendar.addCustomButtonClickedListener("btn", e -> first.incrementAndGet());
        calendar.addCustomButtonClickedListener("btn", e -> second.incrementAndGet());

        simulateCustomButtonClick("btn");
        assertEquals(1, first.get());
        assertEquals(1, second.get());

        // Remove only the first listener
        reg1.remove();
        simulateCustomButtonClick("btn");
        assertEquals(1, first.get()); // unchanged
        assertEquals(2, second.get()); // incremented again
    }

    @Test
    void customButtonClicked_unknownName_doesNotFireOtherListeners() {
        CustomButton btn = new CustomButton("btn");
        calendar.addCustomButton(btn);
        List<String> fired = new java.util.ArrayList<>();
        calendar.addCustomButtonClickedListener("btn", e -> fired.add(e.getButtonName()));

        simulateCustomButtonClick("nonExistent");
        assertTrue(fired.isEmpty());
    }

    @Test
    void customButtonClicked_unknownName_noError() {
        // Should not throw even when no button with that name is registered
        assertDoesNotThrow(() -> simulateCustomButtonClick("nonExistent"));
    }

    @Test
    void customButtonClickedEvent_hasCorrectButtonName() {
        CustomButton btn = new CustomButton("myBtn");
        calendar.addCustomButton(btn);
        List<String> received = new java.util.ArrayList<>();
        calendar.addCustomButtonClickedListener("myBtn", e -> received.add(e.getButtonName()));

        simulateCustomButtonClick("myBtn");
        assertEquals(List.of("myBtn"), received);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // navLinkDayClick / navLinkWeekClick callbacks
    // -------------------------------------------------------------------------

    @Test
    void setNavLinkDayClickCallback_doesNotThrow() {
        assertDoesNotThrow(() -> calendar.setNavLinkDayClickCallback("function(date) { alert(date); }"));
    }

    @Test
    void setNavLinkDayClickCallback_storesOption() {
        calendar.setNavLinkDayClickCallback("function(date) {}");
        assertOptionalEquals("function(date) {}", calendar.getOption("navLinkDayClick"));
    }

    @Test
    void setNavLinkWeekClickCallback_doesNotThrow() {
        assertDoesNotThrow(() -> calendar.setNavLinkWeekClickCallback("function(weekStart) {}"));
    }

    @Test
    void setNavLinkWeekClickCallback_storesOption() {
        calendar.setNavLinkWeekClickCallback("function(weekStart) {}");
        assertOptionalEquals("function(weekStart) {}", calendar.getOption("navLinkWeekClick"));
    }

    /**
     * Directly invokes the {@code customButtonClicked} @ClientCallable method on the calendar,
     * simulating a client-side click without a real browser.
     */
    private void simulateCustomButtonClick(String buttonName) {
        try {
            var method = FullCalendar.class.getDeclaredMethod("customButtonClicked", String.class);
            method.setAccessible(true);
            method.invoke(calendar, buttonName);
        } catch (NoSuchMethodException e) {
            org.junit.jupiter.api.Assertions.fail(
                    "customButtonClicked(String) method not found — API may have changed", e);
        } catch (Exception e) {
            throw new RuntimeException("Could not invoke customButtonClicked", e);
        }
    }
}
