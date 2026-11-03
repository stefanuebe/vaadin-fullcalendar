package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Setting an entry provider on a shown calendar makes the client fetch from the new entry provider (#447).
 */
public class SetEntryProviderRefreshTest extends BrowserlessTest {

    private final FullCalendar calendar = new FullCalendar();

    /** Runs what the server does before its response and counts the "refresh all" calls sent to the client. */
    private long respondAndCountRefreshAll() {
        UI ui = UI.getCurrent();
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression().contains("refreshAllEvents"))
                .count();
    }

    @Test
    void setEntryProvider_afterAttach_refreshesTheClient() {
        UI.getCurrent().add(calendar);
        respondAndCountRefreshAll();

        calendar.setEntryProvider(EntryProvider.inMemoryFrom(new Entry()));

        assertEquals(1, respondAndCountRefreshAll());
    }

    @Test
    void setEntryProvider_inTheAttachRoundTrip_sendsNoRefresh() {
        UI.getCurrent().add(calendar);
        calendar.setEntryProvider(EntryProvider.inMemoryFrom(new Entry()));

        assertEquals(0, respondAndCountRefreshAll());
    }

    @Test
    void setEntryProvider_whileDetachedInOneRoundTrip_refreshesAfterReattach() {
        UI.getCurrent().add(calendar);
        respondAndCountRefreshAll();

        UI.getCurrent().remove(calendar);
        calendar.setEntryProvider(EntryProvider.inMemoryFrom(new Entry()));
        UI.getCurrent().add(calendar);

        assertEquals(1, respondAndCountRefreshAll());
    }

    @Test
    void setEntryProvider_afterTheClientElementWasRemoved_sendsNoRefresh() {
        UI.getCurrent().add(calendar);
        respondAndCountRefreshAll();
        UI.getCurrent().remove(calendar);
        respondAndCountRefreshAll();

        calendar.setEntryProvider(EntryProvider.inMemoryFrom(new Entry()));
        UI.getCurrent().add(calendar);

        assertEquals(0, respondAndCountRefreshAll());
    }

    @Test
    void setEntryProvider_followedByRefreshAll_refreshesOnce() {
        UI.getCurrent().add(calendar);
        respondAndCountRefreshAll();

        calendar.setEntryProvider(EntryProvider.inMemoryFrom(new Entry()));
        calendar.getEntryProvider().refreshAll();

        assertEquals(1, respondAndCountRefreshAll());
    }

    @Test
    void setEntryProvider_beforeAttach_sendsNoRefresh() {
        calendar.setEntryProvider(EntryProvider.inMemoryFrom(new Entry()));
        UI.getCurrent().add(calendar);

        assertEquals(0, respondAndCountRefreshAll());
    }
}
