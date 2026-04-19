package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the #189 / 7.2.0 rename on {@link EntryDataEvent}:
 * {@code createCopyBasedOnChanges()} is now deprecated and delegates to
 * the new {@code getChangesAsEntry()}.
 */
class EntryDataEventRenameTest {

    @Test
    void getChangesAsEntry_exists_andIsPublic() throws NoSuchMethodException {
        Method m = EntryDataEvent.class.getMethod("getChangesAsEntry");
        assertNotNull(m);
        assertTrue(java.lang.reflect.Modifier.isPublic(m.getModifiers()));
        assertEquals(Entry.class, erasureOf(m.getGenericReturnType()));
    }

    @Test
    void createCopyBasedOnChanges_isDeprecatedSince720() throws NoSuchMethodException {
        Method m = EntryDataEvent.class.getMethod("createCopyBasedOnChanges");
        Deprecated d = m.getAnnotation(Deprecated.class);
        assertNotNull(d, "createCopyBasedOnChanges must be @Deprecated");
        assertEquals("7.2.0", d.since());
        assertEquals(false, d.forRemoval(), "no removal scheduled — keep in 7.x");
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
