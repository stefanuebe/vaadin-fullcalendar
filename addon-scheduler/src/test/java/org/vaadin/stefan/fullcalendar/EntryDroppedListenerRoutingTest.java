package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drop listeners on the scheduler (#306). A listener added with {@code addEntryDroppedListener} receives the
 * scheduler event, and one drop makes one auto-revert decision for all listeners.
 * <p>
 * The client sends one {@code eventDrop} per registered event class. The tests fire both event classes for one drop,
 * like the client would if both classes had a listener. A class without a listener receives nothing.
 */
class EntryDroppedListenerRoutingTest extends BrowserlessTest {

    private FullCalendarScheduler calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendarScheduler();
        ResourceEntry entry = new ResourceEntry("e1");
        entry.setStart(LocalDateTime.of(2025, 3, 3, 9, 0));
        entry.setEnd(LocalDateTime.of(2025, 3, 3, 10, 0));
        calendar.getEntryProvider().asInMemory().addEntry(entry);
        UI.getCurrent().add(calendar);
        calendar.fetchEntriesFromServer(JsonFactory.createObject()); // fills the fetch cache the events read
        flush();
    }

    private List<PendingJavaScriptInvocation> flush() {
        UI ui = UI.getCurrent();
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations();
    }

    private ObjectNode droppedEntryJson() {
        ObjectNode json = JsonFactory.createObject();
        json.put("id", "e1");
        json.put("start", "2025-03-04T09:00:00Z");
        json.put("end", "2025-03-04T10:00:00Z");
        return json;
    }

    private ObjectNode oneDayDelta() {
        ObjectNode delta = JsonFactory.createObject();
        delta.put("days", 1);
        delta.put("milliseconds", 0);
        return delta;
    }

    /** One client drop, as far as the server sees it. Returns the revert related client calls of the response. */
    private List<String> drop() {
        ObjectNode json = droppedEntryJson();
        ObjectNode delta = oneDayDelta();
        ComponentUtil.fireEvent(calendar, new EntryDroppedEvent(calendar, true, json, delta));
        ComponentUtil.fireEvent(calendar, new EntryDroppedSchedulerEvent(calendar, true, json, delta));

        List<String> calls = new ArrayList<>();
        for (PendingJavaScriptInvocation invocation : flush()) {
            String expression = invocation.getInvocation().getExpression();
            if (expression.contains("revertEntry")) {
                calls.add("revertEntry");
            } else if (expression.contains("clearPendingRevert")) {
                calls.add("clearPendingRevert");
            }
        }
        return calls;
    }

    @Test
    void entryDroppedListener_receivesSchedulerEvent() {
        List<EntryDroppedEvent> received = new ArrayList<>();
        calendar.addEntryDroppedListener(received::add);

        drop();

        assertEquals(1, received.size());
        assertInstanceOf(EntryDroppedSchedulerEvent.class, received.get(0));
    }

    @Test
    void bothListenerKinds_eachReceivesTheDropOnce() {
        List<EntryDroppedEvent> plain = new ArrayList<>();
        List<EntryDroppedSchedulerEvent> scheduler = new ArrayList<>();
        calendar.addEntryDroppedListener(plain::add);
        calendar.addEntryDroppedSchedulerListener(scheduler::add);

        drop();

        assertEquals(1, plain.size());
        assertEquals(1, scheduler.size());
    }

    @Test
    void oneListenerApplies_otherDoesNot_entryIsNotReverted_plainFirst() {
        calendar.addEntryDroppedListener(e -> { /* does not apply */ });
        calendar.addEntryDroppedSchedulerListener(EntryDroppedSchedulerEvent::applyChangesOnEntry);

        List<String> calls = drop();

        assertFalse(calls.contains("revertEntry"), "one listener applied the drop, so no revert, got " + calls);
        assertEquals(List.of("clearPendingRevert"), calls);
    }

    @Test
    void oneListenerApplies_otherDoesNot_entryIsNotReverted_schedulerFirst() {
        calendar.addEntryDroppedSchedulerListener(e -> { /* does not apply */ });
        calendar.addEntryDroppedListener(EntryDroppedEvent::applyChangesOnEntry);

        List<String> calls = drop();

        assertFalse(calls.contains("revertEntry"), "one listener applied the drop, so no revert, got " + calls);
        assertEquals(List.of("clearPendingRevert"), calls);
    }

    @Test
    void noListenerApplies_entryIsRevertedOnce() {
        calendar.addEntryDroppedListener(e -> { });
        calendar.addEntryDroppedSchedulerListener(e -> { });

        assertEquals(List.of("revertEntry"), drop());
    }

    /**
     * Documents the listener signatures at compile time. The plain methods take a listener for exactly the base event,
     * so a listener typed for a scheduler event does not compile there. The scheduler keeps its own listener methods
     * for the scheduler events. The assertions only show that each registration succeeds.
     */
    @Test
    void timeslotListenerSignatures_plainTakesBaseEvent_schedulerKeepsItsOwn() {
        ComponentEventListener<TimeslotClickedEvent> plainClick = e -> { };
        ComponentEventListener<TimeslotsSelectedEvent> plainSelect = e -> { };
        ComponentEventListener<TimeslotClickedSchedulerEvent> schedulerClick = e -> { };
        ComponentEventListener<TimeslotsSelectedSchedulerEvent> schedulerSelect = e -> { };

        FullCalendar plain = new FullCalendar();
        assertNotNull(plain.addTimeslotClickedListener(plainClick));
        assertNotNull(plain.addTimeslotsSelectedListener(plainSelect));
        assertNotNull(calendar.addTimeslotClickedListener(plainClick));
        assertNotNull(calendar.addTimeslotsSelectedListener(plainSelect));
        assertNotNull(calendar.addTimeslotClickedSchedulerListener(schedulerClick));
        assertNotNull(calendar.addTimeslotsSelectedSchedulerListener(schedulerSelect));
    }
}
