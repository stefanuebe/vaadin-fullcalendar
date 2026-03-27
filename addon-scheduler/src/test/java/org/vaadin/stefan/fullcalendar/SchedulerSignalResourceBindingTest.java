package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FullCalendarScheduler} resource signal binding guard conditions.
 * <p>
 * {@code addResources}, {@code removeResources}, and {@code removeAllResources} throw
 * {@link BindingActiveException} when a resource signal is active. Reaching that state
 * requires {@code bindResources()} to complete — which internally calls
 * {@code Signal.effect()}, requiring an active Vaadin UI session. Those tests are
 * therefore marked {@link Disabled} and are covered by E2E tests
 * ({@code signal-resource-binding.spec.js}).
 * <p>
 * What can be tested without a UI session:
 * <ul>
 *   <li>{@code isResourceBindingActive()} default is {@code false}</li>
 *   <li>{@code bindResources(null)} when nothing is bound is a no-op (no exception)</li>
 *   <li>Normal {@code addResources} / {@code removeResources} / {@code removeAllResources}
 *       work when no signal binding is active</li>
 * </ul>
 */
public class SchedulerSignalResourceBindingTest {

    private FullCalendarScheduler calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendarScheduler();
    }

    // ---- Default state ----

    @Test
    void resourceBindingIsInactiveByDefault() {
        assertFalse(calendar.isResourceBindingActive());
    }

    // ---- bindResources(null) on unbound calendar is a no-op ----

    @Test
    void bindResourcesNullWhenNotBoundIsNoOp() {
        assertDoesNotThrow(() -> calendar.bindResources(null));
        assertFalse(calendar.isResourceBindingActive());
    }

    // ---- Normal resource operations work without a signal binding ----

    @Test
    void addResourcesWorksWhenNoSignalBindingActive() {
        Resource r = new Resource("r1", "Room 1", null);
        assertDoesNotThrow(() -> calendar.addResources(r));
        assertTrue(calendar.getResources().contains(r));
    }

    @Test
    void removeResourcesWorksWhenNoSignalBindingActive() {
        Resource r = new Resource("r2", "Room 2", null);
        calendar.addResources(r);
        assertDoesNotThrow(() -> calendar.removeResources(r));
        assertFalse(calendar.getResources().contains(r));
    }

    @Test
    void removeAllResourcesWorksWhenNoSignalBindingActive() {
        Resource r1 = new Resource("r3", "Room 3", null);
        Resource r2 = new Resource("r4", "Room 4", null);
        calendar.addResources(r1, r2);
        assertDoesNotThrow(() -> calendar.removeAllResources());
        assertTrue(calendar.getResources().isEmpty());
    }

    // ---- Guards: addResources/removeResources/removeAllResources throw when signal active ----
    // These guards (resourceSignal != null) are only reachable after bindResources() completes,
    // which calls Signal.effect() and therefore requires a Vaadin UI session.

    @Test
    @Disabled("Requires Vaadin UI session — bindResources() calls Signal.effect(); covered by E2E tests in signal-resource-binding.spec.js")
    void addResourcesThrowsWhenSignalBindingActive() {
        calendar.bindResources(new ListSignal<Resource>()); // needs UI session
        Resource r = new Resource("rx", "Room X", null);
        assertThrows(BindingActiveException.class, () -> calendar.addResources(r));
    }

    @Test
    @Disabled("Requires Vaadin UI session — bindResources() calls Signal.effect(); covered by E2E tests in signal-resource-binding.spec.js")
    void removeResourcesThrowsWhenSignalBindingActive() {
        calendar.bindResources(new ListSignal<Resource>()); // needs UI session
        Resource r = new Resource("ry", "Room Y", null);
        assertThrows(BindingActiveException.class, () -> calendar.removeResources(r));
    }

    @Test
    @Disabled("Requires Vaadin UI session — bindResources() calls Signal.effect(); covered by E2E tests in signal-resource-binding.spec.js")
    void removeAllResourcesThrowsWhenSignalBindingActive() {
        calendar.bindResources(new ListSignal<Resource>()); // needs UI session
        assertThrows(BindingActiveException.class, () -> calendar.removeAllResources());
    }

    @Test
    @Disabled("Requires Vaadin UI session — bindResources() calls Signal.effect(); covered by E2E tests in signal-resource-binding.spec.js")
    void bindResourcesNullUnbindsActiveBinding() {
        calendar.bindResources(new ListSignal<Resource>()); // needs UI session
        assertTrue(calendar.isResourceBindingActive());

        calendar.bindResources(null);
        assertFalse(calendar.isResourceBindingActive());
    }
}
