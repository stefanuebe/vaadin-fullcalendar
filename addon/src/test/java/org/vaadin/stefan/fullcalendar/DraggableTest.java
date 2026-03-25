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
    void resolveDraggableWithNullReturnsNull() {
        FullCalendar calendar = new FullCalendar();
        assertNull(calendar.resolveDraggable(null));
    }

    @Test
    void resolveDraggableWithBlankReturnsNull() {
        FullCalendar calendar = new FullCalendar();
        assertNull(calendar.resolveDraggable(""));
        assertNull(calendar.resolveDraggable("   "));
    }

    @Test
    void resolveDraggableWithUnknownIdReturnsNull() {
        FullCalendar calendar = new FullCalendar();
        assertNull(calendar.resolveDraggable("unknown-id"));
    }
}
