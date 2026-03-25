package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.html.Div;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DraggableTest {

    @Test
    void constructorRequiresNonNullComponent() {
        assertThrows(NullPointerException.class, () -> new Draggable(null));
    }

    @Test
    void constructorWithComponentOnly() {
        Div div = new Div();
        Draggable draggable = new Draggable(div);

        assertSame(div, draggable.getComponent());
        assertTrue(draggable.getEntryData().isEmpty());
    }

    @Test
    void constructorWithComponentAndEntry() {
        Div div = new Div();
        Entry entry = new Entry();
        entry.setTitle("Test Entry");

        Draggable draggable = new Draggable(div, entry);

        assertSame(div, draggable.getComponent());
        assertTrue(draggable.getEntryData().isPresent());
        assertEquals("Test Entry", draggable.getEntryData().get().getTitle());
    }

    @Test
    void constructorWithNullEntry() {
        Div div = new Div();
        Draggable draggable = new Draggable(div, null);

        assertSame(div, draggable.getComponent());
        assertTrue(draggable.getEntryData().isEmpty());
    }

    @Test
    void resolveDraggableWithNullReturnsEmpty() {
        FullCalendar calendar = new FullCalendar();
        assertTrue(calendar.resolveDraggable(null).isEmpty());
    }

    @Test
    void resolveDraggableWithBlankReturnsEmpty() {
        FullCalendar calendar = new FullCalendar();
        assertTrue(calendar.resolveDraggable("").isEmpty());
        assertTrue(calendar.resolveDraggable("   ").isEmpty());
    }

    @Test
    void resolveDraggableWithUnknownIdReturnsEmpty() {
        FullCalendar calendar = new FullCalendar();
        assertTrue(calendar.resolveDraggable("unknown-id").isEmpty());
    }
}
