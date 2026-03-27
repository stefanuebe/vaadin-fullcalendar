package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the signal binding lifecycle on {@link FullCalendar}.
 * <p>
 * This class tests guard conditions that are verifiable without a Vaadin UI session.
 * Tests that exercise the full bind/unbind lifecycle (which invokes {@code Signal.effect()})
 * require a Vaadin UI session and are therefore covered by Browserless or E2E tests
 * ({@code signal-entry-binding.spec.js}).
 */
public class FullCalendarSignalBindingTest {

    private FullCalendar createCalendar() {
        return new FullCalendar();
    }

    // ---- Default state ----

    @Test
    void signalBindingIsInactiveByDefault() {
        FullCalendar calendar = createCalendar();
        assertFalse(calendar.isSignalBindingActive());
    }

    @Test
    void autoRevertIsEnabledByDefault() {
        FullCalendar calendar = createCalendar();
        assertTrue(calendar.isAutoRevertUnappliedEntryChanges());
    }

    // ---- bindEntries(null) on unbound calendar — no-op ----

    @Test
    void bindEntriesNullWhenNotBoundIsNoOp() {
        FullCalendar calendar = createCalendar();
        assertDoesNotThrow(() -> calendar.bindEntries(null));
        assertFalse(calendar.isSignalBindingActive());
    }

    // ---- Guard: autoRevert must be true ----

    @Test
    void bindEntriesThrowsIllegalStateWhenAutoRevertDisabled() {
        FullCalendar calendar = createCalendar();
        calendar.setAutoRevertUnappliedEntryChanges(false);

        ListSignal<Entry> signal = new ListSignal<>();
        assertThrows(IllegalStateException.class, () -> calendar.bindEntries(signal));
    }

    @Test
    @Disabled("Signal.effect() throws MissingSignalUsageException when the bound ListSignal is empty (no signals read during effect). Covered by Browserless/E2E tests.")
    void bindEntriesDoesNotThrowWhenAutoRevertIsEnabled() {
        // autoRevert is true by default — binding with a signal should succeed
        // but Signal.effect() requires the effect body to read at least one signal.
        // An empty ListSignal causes MissingSignalUsageException.
        FullCalendar calendar = createCalendar();
        assertTrue(calendar.isAutoRevertUnappliedEntryChanges());

        ListSignal<Entry> signal = new ListSignal<>();
        assertDoesNotThrow(() -> calendar.bindEntries(signal));
    }

    // ---- Guard: custom (non-InMemory) entry provider is active ----

    @Test
    void bindEntriesThrowsWhenCallbackProviderIsActive() {
        FullCalendar calendar = createCalendar();
        calendar.setEntryProvider(EntryProvider.fromCallbacks(
                q -> java.util.stream.Stream.empty(), id -> null));

        ListSignal<Entry> signal = new ListSignal<>();
        assertThrows(BindingActiveException.class, () -> calendar.bindEntries(signal));
    }

    @Test
    @Disabled("Signal.effect() throws MissingSignalUsageException when the bound ListSignal is empty. Covered by Browserless/E2E tests.")
    void bindEntriesDoesNotThrowWhenInMemoryProviderIsActive() {
        // InMemoryEntryProvider is the default; the guard in bindEntries allows it.
        // However, Signal.effect() requires at least one signal read — an empty ListSignal
        // causes MissingSignalUsageException.
        FullCalendar calendar = createCalendar();

        calendar.setEntryProvider(new InMemoryEntryProvider<>());

        ListSignal<Entry> signal = new ListSignal<>();
        assertDoesNotThrow(() -> calendar.bindEntries(signal));
    }

    // ---- Guard: setEntryProvider blocked when signal binding is active ----

    @Test
    @Disabled("Requires Vaadin UI session — Signal.effect() is called inside bindEntries(); covered by E2E tests in signal-entry-binding.spec.js")
    void setEntryProviderThrowsWhenSignalBindingActive() {
        // To reach this state, bindEntries() must have completed (including startObserving()),
        // which calls Signal.effect() and requires an active Vaadin UI session.
        FullCalendar calendar = createCalendar();
        ListSignal<Entry> signal = new ListSignal<>();
        calendar.bindEntries(signal); // requires UI session

        assertThrows(BindingActiveException.class,
                () -> calendar.setEntryProvider(new InMemoryEntryProvider<>()));
    }

    // ---- Guard: disabling autoRevert blocked when signal binding is active ----

    @Test
    @Disabled("Requires Vaadin UI session — Signal.effect() is called inside bindEntries(); covered by E2E tests in signal-entry-binding.spec.js")
    void setAutoRevertFalseThrowsWhenSignalBindingActive() {
        FullCalendar calendar = createCalendar();
        ListSignal<Entry> signal = new ListSignal<>();
        calendar.bindEntries(signal); // requires UI session

        assertThrows(BindingActiveException.class,
                () -> calendar.setAutoRevertUnappliedEntryChanges(false));
    }

    // ---- Builder mutual exclusion ----

    @Test
    void builderWithSignalBindingAfterEntryProviderThrows() {
        assertThrows(BindingActiveException.class, () ->
                FullCalendarBuilder.create()
                        .withEntryProvider(new InMemoryEntryProvider<>())
                        .withSignalBinding(new ListSignal<Entry>()));
    }

    @Test
    void builderWithEntryProviderAfterSignalBindingThrows() {
        assertThrows(BindingActiveException.class, () ->
                FullCalendarBuilder.create()
                        .withSignalBinding(new ListSignal<Entry>())
                        .withEntryProvider(new InMemoryEntryProvider<>()));
    }

    @Test
    void builderWithOnlyEntryProviderDoesNotThrow() {
        assertDoesNotThrow(() ->
                FullCalendarBuilder.create()
                        .withEntryProvider(new InMemoryEntryProvider<>())
                        .build());
    }
}
