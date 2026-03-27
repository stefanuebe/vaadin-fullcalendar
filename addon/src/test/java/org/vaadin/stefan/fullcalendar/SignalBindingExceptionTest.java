package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all {@link BindingActiveException} and related exception scenarios in signal binding.
 * <p>
 * Tests that require an active Vaadin UI session (because they need {@code Signal.effect()} to
 * complete via {@code bindEntries()}) are marked {@link Disabled} with an explanation.
 * Those scenarios are covered by E2E tests ({@code signal-entry-binding.spec.js}).
 */
public class SignalBindingExceptionTest {

    private FullCalendar createCalendar() {
        return new FullCalendar();
    }

    // ---- bindEntries() → BindingActiveException: non-default provider is active ----

    @Test
    void bindEntriesThrowsWhenCallbackProviderActive() {
        FullCalendar calendar = createCalendar();
        calendar.setEntryProvider(EntryProvider.fromCallbacks(
                q -> java.util.stream.Stream.empty(), id -> null));

        ListSignal<Entry> signal = new ListSignal<>();
        assertThrows(BindingActiveException.class, () -> calendar.bindEntries(signal));
    }

    @Test
    @Disabled("Signal.effect() throws MissingSignalUsageException when the bound ListSignal is empty (no signals read). The guard itself does not throw — covered by Browserless/E2E tests.")
    void bindEntriesDoesNotThrowWhenInMemoryProviderActive() {
        // InMemoryEntryProvider is the "default" provider — the guard in bindEntries allows it.
        // However, Signal.effect() (called inside startObserving) requires at least one signal
        // read during the effect body. An empty ListSignal causes MissingSignalUsageException.
        FullCalendar calendar = createCalendar();
        calendar.setEntryProvider(new InMemoryEntryProvider<>());

        ListSignal<Entry> signal = new ListSignal<>();
        assertDoesNotThrow(() -> calendar.bindEntries(signal));
    }

    // ---- bindEntries() → IllegalStateException: autoRevert is false ----

    @Test
    void bindEntriesThrowsIllegalStateWhenAutoRevertDisabled() {
        FullCalendar calendar = createCalendar();
        calendar.setAutoRevertUnappliedEntryChanges(false);

        ListSignal<Entry> signal = new ListSignal<>();
        // Note: this throws IllegalStateException, NOT BindingActiveException
        assertThrows(IllegalStateException.class, () -> calendar.bindEntries(signal));
    }

    @Test
    void bindEntriesAutoRevertExceptionIsNotBindingActiveException() {
        // Verify the exact exception type — it must be IllegalStateException,
        // not BindingActiveException, so callers can distinguish the two failure modes.
        FullCalendar calendar = createCalendar();
        calendar.setAutoRevertUnappliedEntryChanges(false);

        ListSignal<Entry> signal = new ListSignal<>();
        Exception thrown = assertThrows(RuntimeException.class, () -> calendar.bindEntries(signal));
        assertFalse(thrown instanceof BindingActiveException,
                "autoRevert guard must throw IllegalStateException, not BindingActiveException");
    }

    // ---- setEntryProvider() → BindingActiveException when signal binding is active ----

    @Test
    @Disabled("Requires Vaadin UI session — bindEntries() calls Signal.effect() which needs a UI; covered by E2E tests in signal-entry-binding.spec.js")
    void setEntryProviderThrowsWhenSignalBindingActive() {
        FullCalendar calendar = createCalendar();
        calendar.bindEntries(new ListSignal<>()); // needs UI session

        assertThrows(BindingActiveException.class,
                () -> calendar.setEntryProvider(new InMemoryEntryProvider<>()));
    }

    // ---- setAutoRevertUnappliedEntryChanges(false) → BindingActiveException when active ----

    @Test
    @Disabled("Requires Vaadin UI session — bindEntries() calls Signal.effect() which needs a UI; covered by E2E tests in signal-entry-binding.spec.js")
    void setAutoRevertFalseThrowsWhenSignalBindingActive() {
        FullCalendar calendar = createCalendar();
        calendar.bindEntries(new ListSignal<>()); // needs UI session

        assertThrows(BindingActiveException.class,
                () -> calendar.setAutoRevertUnappliedEntryChanges(false));
    }

    // ---- FullCalendarBuilder mutual exclusion ----

    @Test
    void builderThrowsWhenSignalBindingSetAfterEntryProvider() {
        assertThrows(BindingActiveException.class, () ->
                FullCalendarBuilder.create()
                        .withEntryProvider(new InMemoryEntryProvider<>())
                        .withSignalBinding(new ListSignal<Entry>()));
    }

    @Test
    void builderThrowsWhenEntryProviderSetAfterSignalBinding() {
        assertThrows(BindingActiveException.class, () ->
                FullCalendarBuilder.create()
                        .withSignalBinding(new ListSignal<Entry>())
                        .withEntryProvider(new InMemoryEntryProvider<>()));
    }

    // ---- Null signal on unbound calendar is always a no-op ----

    @Test
    void bindEntriesNullOnUnboundCalendarDoesNotThrow() {
        FullCalendar calendar = createCalendar();
        assertDoesNotThrow(() -> calendar.bindEntries(null));
    }
}
