package org.vaadin.stefan.fullcalendar.spike;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.dom.DomEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification 0.2: Confirms that generic Vaadin components work with the
 * event system at runtime. Tests that:
 * <ul>
 *   <li>A generic component {@code TestComponent<T>} can be instantiated</li>
 *   <li>Events typed as {@code ComponentEvent<TestComponent<String>>} work correctly</li>
 *   <li>{@code event.getSource()} returns the typed component, no ClassCastException</li>
 *   <li>Listeners can be registered and fire correctly</li>
 * </ul>
 *
 * This verifies the feasibility of {@code FullCalendar<T>} without touching real FullCalendar code.
 */
class GenericComponentDomEventTest {

    /**
     * Minimal generic Vaadin component simulating FullCalendar<T>.
     */
    @Tag("test-component")
    static class TestComponent<T> extends Component {

        private T data;

        public void setData(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        /**
         * Simulates addEntryClickedListener — registers a typed event.
         * Uses the low-level ComponentEventBus directly.
         * The unchecked cast mirrors what would happen with FullCalendar<T>:
         * at runtime the generic is erased, so TestComponent<T> IS TestComponent<String>.
         */
        @SuppressWarnings("unchecked")
        public com.vaadin.flow.shared.Registration addTestEventListener(
                ComponentEventListener<TestEvent> listener) {
            return addListener(TestEvent.class, listener);
        }

        /**
         * Simulates addCalendarItemClickedListener — registers a generic event.
         */
        @SuppressWarnings("unchecked")
        public <I> com.vaadin.flow.shared.Registration addGenericTestEventListener(
                ComponentEventListener<GenericTestEvent<I>> listener) {
            var eventClass = (Class<GenericTestEvent<I>>) (Class<?>) GenericTestEvent.class;
            return addListener(eventClass, listener);
        }

        /**
         * Programmatically fires a component event (simulating how Vaadin dispatches @DomEvent).
         * The unchecked cast is safe at runtime due to type erasure.
         */
        @SuppressWarnings("unchecked")
        public void fireTestEvent() {
            ComponentUtil.fireEvent(this, new TestEvent((TestComponent<String>) (TestComponent<?>) this, false));
        }

        @SuppressWarnings("unchecked")
        public <I> void fireGenericTestEvent(I item) {
            ComponentUtil.fireEvent(this, new GenericTestEvent<>((TestComponent<I>) (TestComponent<?>) this, false, item));
        }
    }

    /**
     * Non-generic event: simulates EntryClickedEvent extends ComponentEvent<FullCalendar<Entry>>.
     * After generification, EntryClickedEvent would extend ComponentEvent<FullCalendar<Entry>>.
     * Here we use TestComponent<String> as a stand-in.
     */
    static class TestEvent extends ComponentEvent<TestComponent<String>> {
        public TestEvent(TestComponent<String> source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * Generic event: simulates CalendarItemClickedEvent<T> extends ComponentEvent<TestComponent<T>>.
     */
    static class GenericTestEvent<T> extends ComponentEvent<TestComponent<T>> {
        private final T item;

        public GenericTestEvent(TestComponent<T> source, boolean fromClient, T item) {
            super(source, fromClient);
            this.item = item;
        }

        public T getItem() {
            return item;
        }
    }

    @Test
    @DisplayName("Generic component creation does not throw")
    void genericComponentCreation() {
        var component = new TestComponent<String>();
        component.setData("hello");

        assertEquals("hello", component.getData());
        assertNotNull(component.getElement());
    }

    @Test
    @DisplayName("Non-generic event on generic component fires without ClassCastException")
    void nonGenericEventOnGenericComponent() {
        var component = new TestComponent<String>();
        component.setData("test-data");

        var eventReceived = new boolean[]{false};
        component.addTestEventListener(event -> {
            eventReceived[0] = true;

            // Key assertion: event.getSource() returns the typed component
            TestComponent<String> source = event.getSource();
            assertNotNull(source);
            assertEquals("test-data", source.getData());
        });

        component.fireTestEvent();

        assertTrue(eventReceived[0], "Event should have been received");
    }

    @Test
    @DisplayName("Generic event on generic component fires with typed item access")
    void genericEventOnGenericComponent() {
        var component = new TestComponent<String>();

        var receivedItem = new String[]{null};
        component.addGenericTestEventListener((ComponentEventListener<GenericTestEvent<String>>) event -> {
            receivedItem[0] = event.getItem();

            TestComponent<String> source = event.getSource();
            assertNotNull(source);
        });

        component.fireGenericTestEvent("my-item");

        assertEquals("my-item", receivedItem[0]);
    }

    @Test
    @DisplayName("Multiple listeners on generic component all fire")
    void multipleListeners() {
        var component = new TestComponent<String>();

        var count = new int[]{0};
        component.addTestEventListener(e -> count[0]++);
        component.addTestEventListener(e -> count[0]++);

        component.fireTestEvent();

        assertEquals(2, count[0]);
    }

    @Test
    @DisplayName("Raw type usage of generic component works (backward compatibility)")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void rawTypeBackwardCompatibility() {
        // Simulates existing code using raw FullCalendar (no generic parameter)
        TestComponent rawComponent = new TestComponent();
        rawComponent.setData("raw-data");

        var eventReceived = new boolean[]{false};
        rawComponent.addTestEventListener(event -> {
            eventReceived[0] = true;
            // Access source as raw type
            var source = event.getSource();
            assertNotNull(source);
        });

        rawComponent.fireTestEvent();

        assertTrue(eventReceived[0], "Event should fire on raw-typed component");
    }

    @Test
    @DisplayName("Generic component with different type parameters coexist")
    void differentTypeParameters() {
        var stringComponent = new TestComponent<String>();
        stringComponent.setData("hello");

        var intComponent = new TestComponent<Integer>();
        intComponent.setData(42);

        assertEquals("hello", stringComponent.getData());
        assertEquals(42, (int) intComponent.getData());

        // Both can fire events
        var stringEventFired = new boolean[]{false};
        var intEventFired = new boolean[]{false};

        stringComponent.addTestEventListener(e -> stringEventFired[0] = true);
        intComponent.addTestEventListener(e -> intEventFired[0] = true);

        stringComponent.fireTestEvent();
        intComponent.fireTestEvent();

        assertTrue(stringEventFired[0]);
        assertTrue(intEventFired[0]);
    }
}
