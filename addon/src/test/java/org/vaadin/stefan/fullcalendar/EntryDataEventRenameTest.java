package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import elemental.json.JsonObject;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the #189 rename on {@link EntryDataEvent}:
 * {@code createCopyBasedOnChanges()} is now deprecated and delegates to
 * the new {@code getChangesAsEntry()}.
 */
@ExtendWith(MockitoExtension.class)
class EntryDataEventRenameTest {

    @Test
    void getChangesAsEntry_exists_andIsPublic() throws NoSuchMethodException {
        Method m = EntryDataEvent.class.getMethod("getChangesAsEntry");
        assertNotNull(m);
        assertTrue(java.lang.reflect.Modifier.isPublic(m.getModifiers()));
        assertEquals(Entry.class, erasureOf(m.getGenericReturnType()));
    }

    @Test
    void createCopyBasedOnChanges_isDeprecated() throws NoSuchMethodException {
        Method m = EntryDataEvent.class.getMethod("createCopyBasedOnChanges");
        Deprecated d = m.getAnnotation(Deprecated.class);
        assertNotNull(d, "createCopyBasedOnChanges must be @Deprecated");
        assertEquals(false, d.forRemoval(), "no removal scheduled — keep in 6.x");
    }

    @Test
    void deprecatedDelegate_returnsSameResultAsNewMethod() {
        // Concrete EntryDataEvent subclass EntryClickedEvent needs a FullCalendar that
        // resolves the id from its fetch cache. Stub that with Mockito so the parent
        // constructor succeeds, then exercise both methods and verify they produce
        // equivalent entries.
        FullCalendar calendar = mock(FullCalendar.class);
        Entry original = new Entry("test-1");
        original.setTitle("Original");
        original.setAllDay(false);
        when(calendar.getCachedEntryFromFetch("test-1")).thenReturn(Optional.of(original));

        // updateFromJson only applies to fields tagged @JsonUpdateAllowed (allDay is one of them)
        JsonObject changes = JsonFactory.createObject();
        changes.put("id", "test-1");
        changes.put("allDay", true);

        EntryClickedEvent event = new EntryClickedEvent(calendar, false, changes);

        Entry fromNew = event.getChangesAsEntry();
        @SuppressWarnings("deprecation")
        Entry fromOld = event.createCopyBasedOnChanges();

        assertEquals("test-1", fromNew.getId());
        assertTrue(fromNew.isAllDay(), "new method must apply JSON changes on the copy");
        assertEquals(fromNew.getId(), fromOld.getId(), "delegate must return same id");
        assertEquals(fromNew.isAllDay(), fromOld.isAllDay(), "delegate must produce same allDay flag");
        assertNotSame(fromNew, fromOld, "each call must return a fresh copy, not the same instance");
        assertNotSame(original, fromNew, "caller must receive a copy, not the original entry");
        assertEquals(false, original.isAllDay(), "original entry must remain unchanged");
    }

    private static Class<?> erasureOf(java.lang.reflect.Type t) {
        if (t instanceof Class<?> c) return c;
        if (t instanceof java.lang.reflect.TypeVariable<?> tv) {
            java.lang.reflect.Type[] bounds = tv.getBounds();
            return bounds.length > 0 ? erasureOf(bounds[0]) : Object.class;
        }
        return Object.class;
    }
}
