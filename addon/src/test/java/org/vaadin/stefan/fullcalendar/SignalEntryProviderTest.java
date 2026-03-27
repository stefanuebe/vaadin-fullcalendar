package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SignalEntryProvider}.
 * <p>
 * {@code SignalEntryProvider} is package-private; this test lives in the same package.
 * <p>
 * Important: {@link ListSignal#peek()} and {@link ListSignal#insertLast(Object)} both call
 * {@code AbstractLocalSignal.checkPreconditions()}, which tries to load
 * {@code jakarta.servlet.http.HttpSessionBindingListener}. Without a servlet container on the
 * classpath (the {@code addon} module has no servlet dependency), any test that exercises
 * {@code peek()} or {@code insertLast()} fails with {@link NoClassDefFoundError}.
 * <p>
 * Tests that require a populated signal are therefore marked {@link Disabled} and covered by
 * Browserless or E2E tests ({@code signal-entry-binding.spec.js}).
 * <p>
 * What CAN be tested without a servlet context:
 * <ul>
 *   <li>Constructor: null guard on the signal argument</li>
 *   <li>{@link SignalEntryProvider#getListSignal()} — simple field accessor, no I/O</li>
 * </ul>
 */
public class SignalEntryProviderTest {

    // ---- Constructor ----

    @Test
    void constructorRejectsNullSignal() {
        assertThrows(NullPointerException.class, () -> new SignalEntryProvider<>(null));
    }

    @Test
    void getListSignalReturnsBoundSignal() {
        // ListSignal no-arg constructor does not call checkPreconditions — safe to use here
        ListSignal<Entry> signal = new ListSignal<>();
        SignalEntryProvider<Entry> provider = new SignalEntryProvider<>(signal);

        assertSame(signal, provider.getListSignal());
    }

    // ---- fetch() — requires servlet context (ListSignal.peek() loads jakarta.servlet) ----

    @Test
    @Disabled("ListSignal.peek() requires jakarta.servlet on classpath (NoClassDefFoundError). Covered by Browserless/E2E tests in signal-entry-binding.spec.js.")
    void fetchReturnsAllNonNullEntries() {
        Entry e1 = new Entry("id-1");
        Entry e2 = new Entry("id-2");
        Entry e3 = new Entry("id-3");

        ListSignal<Entry> signal = new ListSignal<>();
        signal.insertLast(e1);
        signal.insertLast(e2);
        signal.insertLast(e3);

        SignalEntryProvider<Entry> provider = new SignalEntryProvider<>(signal);
        EntryQuery query = new EntryQuery((java.time.LocalDateTime) null, null, EntryQuery.AllDay.BOTH);

        List<Entry> result = provider.fetch(query).collect(Collectors.toList());

        assertEquals(3, result.size());
        assertTrue(result.contains(e1));
        assertTrue(result.contains(e2));
        assertTrue(result.contains(e3));
    }

    @Test
    @Disabled("ListSignal.peek() requires jakarta.servlet on classpath (NoClassDefFoundError). Covered by Browserless/E2E tests in signal-entry-binding.spec.js.")
    void fetchReturnsEmptyStreamWhenSignalIsEmpty() {
        ListSignal<Entry> signal = new ListSignal<>();
        SignalEntryProvider<Entry> provider = new SignalEntryProvider<>(signal);
        EntryQuery query = new EntryQuery((java.time.LocalDateTime) null, null, EntryQuery.AllDay.BOTH);

        List<Entry> result = provider.fetch(query).collect(Collectors.toList());

        assertTrue(result.isEmpty());
    }

    // ---- fetchById() — requires servlet context ----

    @Test
    @Disabled("ListSignal.peek() requires jakarta.servlet on classpath (NoClassDefFoundError). Covered by Browserless/E2E tests in signal-entry-binding.spec.js.")
    void fetchByIdReturnsCorrectEntry() {
        Entry e1 = new Entry("find-me");
        Entry e2 = new Entry("other");

        ListSignal<Entry> signal = new ListSignal<>();
        signal.insertLast(e1);
        signal.insertLast(e2);

        SignalEntryProvider<Entry> provider = new SignalEntryProvider<>(signal);

        Optional<Entry> found = provider.fetchById("find-me");

        assertTrue(found.isPresent());
        assertSame(e1, found.get());
    }

    @Test
    @Disabled("ListSignal.peek() requires jakarta.servlet on classpath (NoClassDefFoundError). Covered by Browserless/E2E tests in signal-entry-binding.spec.js.")
    void fetchByIdReturnsEmptyForUnknownId() {
        Entry e1 = new Entry("known-id");

        ListSignal<Entry> signal = new ListSignal<>();
        signal.insertLast(e1);

        SignalEntryProvider<Entry> provider = new SignalEntryProvider<>(signal);

        Optional<Entry> found = provider.fetchById("does-not-exist");

        assertTrue(found.isEmpty());
    }

    @Test
    @Disabled("ListSignal.peek() requires jakarta.servlet on classpath (NoClassDefFoundError). Covered by Browserless/E2E tests in signal-entry-binding.spec.js.")
    void fetchByIdReturnsEmptyWhenSignalIsEmpty() {
        ListSignal<Entry> signal = new ListSignal<>();
        SignalEntryProvider<Entry> provider = new SignalEntryProvider<>(signal);

        Optional<Entry> found = provider.fetchById("any-id");

        assertTrue(found.isEmpty());
    }

    // ---- findSignalForEntry() — requires servlet context ----

    @Test
    @Disabled("ListSignal.peek() requires jakarta.servlet on classpath (NoClassDefFoundError). Covered by Browserless/E2E tests in signal-entry-binding.spec.js.")
    void findSignalForEntryFindsCorrectSignal() {
        Entry target = new Entry("target-id");
        Entry other = new Entry("other-id");

        ListSignal<Entry> signal = new ListSignal<>();
        signal.insertLast(target);
        signal.insertLast(other);

        SignalEntryProvider<Entry> provider = new SignalEntryProvider<>(signal);

        Optional<ValueSignal<Entry>> found = provider.findSignalForEntry("target-id");

        assertTrue(found.isPresent());
        assertSame(target, found.get().peek());
    }

    @Test
    @Disabled("ListSignal.peek() requires jakarta.servlet on classpath (NoClassDefFoundError). Covered by Browserless/E2E tests in signal-entry-binding.spec.js.")
    void findSignalForEntryReturnsEmptyForMissingId() {
        Entry e = new Entry("present-id");

        ListSignal<Entry> signal = new ListSignal<>();
        signal.insertLast(e);

        SignalEntryProvider<Entry> provider = new SignalEntryProvider<>(signal);

        Optional<ValueSignal<Entry>> found = provider.findSignalForEntry("missing-id");

        assertTrue(found.isEmpty());
    }

    @Test
    @Disabled("ListSignal.peek() requires jakarta.servlet on classpath (NoClassDefFoundError). Covered by Browserless/E2E tests in signal-entry-binding.spec.js.")
    void findSignalForEntryReturnsEmptyWhenSignalIsEmpty() {
        ListSignal<Entry> signal = new ListSignal<>();
        SignalEntryProvider<Entry> provider = new SignalEntryProvider<>(signal);

        Optional<ValueSignal<Entry>> found = provider.findSignalForEntry("any-id");

        assertTrue(found.isEmpty());
    }
}
