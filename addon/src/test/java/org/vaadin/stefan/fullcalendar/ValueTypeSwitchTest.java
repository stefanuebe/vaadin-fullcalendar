package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Each value type can be switched over without a {@code default} branch. The test fails to compile when a value type
 * gets a form these switches do not name.
 */
class ValueTypeSwitchTest {

    private static final JsCallback CALLBACK = JsCallback.of("() => {}");

    private static String form(ClassNameValue value) {
        return switch (value) {
            case ClassName className -> className.names();
            case JsCallback callback -> "callback";
        };
    }

    private static String form(ContentValue value) {
        return switch (value) {
            case TextContent content -> content.text();
            case JsCallback callback -> "callback";
        };
    }

    private static String form(CssSelectorValue value) {
        return switch (value) {
            case CssSelector selector -> selector.selector();
            case JsCallback callback -> "callback";
        };
    }

    private static String form(HeaderAlignValue value) {
        return switch (value) {
            case HeaderAlign align -> align.getClientSideValue();
            case JsCallback callback -> "callback";
        };
    }

    private static String form(OverlapValue value) {
        return switch (value) {
            case Overlap overlap -> overlap.name();
            case JsCallback callback -> "callback";
        };
    }

    private static String form(ViewNameValue value) {
        return switch (value) {
            case ViewName name -> name.name();
            case JsCallback callback -> "callback";
        };
    }

    @Test
    void everyValueTypeHasASwitch() throws NoSuchMethodException {
        for (Class<?> valueType : JsCallback.class.getInterfaces()) {
            ValueTypeSwitchTest.class.getDeclaredMethod("form", valueType);
        }
    }

    @Test
    void everyFormIsNamed() {
        assertEquals("a", form((ClassNameValue) ClassName.of("a")));
        assertEquals("callback", form((ClassNameValue) CALLBACK));
        assertEquals("text", form((ContentValue) TextContent.of("text")));
        assertEquals("callback", form((ContentValue) CALLBACK));
        assertEquals(".item", form((CssSelectorValue) CssSelector.of(".item")));
        assertEquals("callback", form((CssSelectorValue) CALLBACK));
        assertEquals("end", form((HeaderAlignValue) HeaderAlign.END));
        assertEquals("callback", form((HeaderAlignValue) CALLBACK));
        assertEquals("FORBIDDEN", form((OverlapValue) Overlap.of(false)));
        assertEquals("callback", form((OverlapValue) CALLBACK));
        assertEquals("dayGridMonth", form((ViewNameValue) ViewName.of(CalendarViewImpl.DAY_GRID_MONTH)));
        assertEquals("callback", form((ViewNameValue) CALLBACK));
    }
}
