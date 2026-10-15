package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Each value type can be switched over without a {@code default} branch. The test fails to compile when a value type
 * gets a form these switches do not name.
 */
class ValueTypeSwitchTest {

    private static final JsCallback CALLBACK = JsCallback.of("() => {}");

    private static String form(AutoBoolean value) {
        return switch (value) {
            case TRUE -> "true";
            case FALSE -> "false";
            case AUTO -> "auto";
        };
    }

    private static String form(BusinessHoursValue value) {
        return switch (value) {
            case BusinessHoursValue.None ignored -> "none";
            case BusinessHoursValue.Default ignored -> "default";
            case BusinessHoursValue.Hours hours -> "hours";
        };
    }

    private static String form(ConstraintValue value) {
        return switch (value) {
            case ConstraintValue.GroupId groupId -> groupId.groupId();
            case ConstraintValue.CalendarBusinessHours ignored -> "businessHours";
            case ConstraintValue.Windows windows -> "windows";
        };
    }

    private static String form(DateFormatValue value) {
        return switch (value) {
            case DateFormat format -> "format";
            case JsCallback callback -> "callback";
        };
    }

    private static String form(EntryLimit value) {
        return switch (value) {
            case EntryLimit.None ignored -> "none";
            case EntryLimit.CellHeight ignored -> "cell height";
            case EntryLimit.Count count -> String.valueOf(count.count());
        };
    }

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

    private static String form(HeightValue value) {
        return switch (value) {
            case HeightValue.Auto ignored -> "auto";
            case HeightValue.Pixels pixels -> String.valueOf(pixels.pixels());
            case HeightValue.Css css -> css.length();
        };
    }

    private static String form(ListDayFormatValue value) {
        return switch (value) {
            case DateFormat format -> "format";
            case JsCallback callback -> "callback";
            case Hidden hidden -> "hidden";
        };
    }

    private static String form(ListTextValue value) {
        return switch (value) {
            case TextContent text -> text.text();
            case Hidden hidden -> "hidden";
        };
    }

    private static String form(NowValue value) {
        return switch (value) {
            case NowValue.Day day -> day.date().toString();
            case NowValue.DateTime dateTime -> dateTime.dateTime().toString();
            case JsCallback callback -> "callback";
        };
    }

    private static String form(OverlapValue value) {
        return switch (value) {
            case Overlap overlap -> overlap.name();
            case JsCallback callback -> "callback";
        };
    }

    private static String form(StickyValue value) {
        return switch (value) {
            case StickyValue.NotSticky ignored -> "not sticky";
            case StickyValue.Sticky ignored -> "sticky";
            case StickyValue.Offset offset -> offset.offset();
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
        for (Class<?> valueType : ConverterTest.VALUE_TYPES.keySet()) {
            ValueTypeSwitchTest.class.getDeclaredMethod("form", valueType);
        }
    }

    @Test
    void everyFormIsNamed() {
        assertEquals("none", form(BusinessHoursValue.NONE));
        assertEquals("auto", form(AutoBoolean.AUTO));
        assertEquals("format", form((DateFormatValue) DateFormat.of(Map.of("day", "numeric"))));
        assertEquals("callback", form((DateFormatValue) CALLBACK));
        assertEquals("auto", form(HeightValue.AUTO));
        assertEquals("650", form(HeightValue.ofPixels(650)));
        assertEquals("50vh", form(HeightValue.ofCss("50vh")));
        assertEquals("format", form((ListDayFormatValue) DateFormat.of(Map.of("day", "numeric"))));
        assertEquals("callback", form((ListDayFormatValue) CALLBACK));
        assertEquals("hidden", form((ListDayFormatValue) Hidden.INSTANCE));
        assertEquals("Agenda", form((ListTextValue) TextContent.of("Agenda")));
        assertEquals("hidden", form((ListTextValue) Hidden.INSTANCE));
        assertEquals("2026-10-15", form(NowValue.of(LocalDate.of(2026, 10, 15))));
        assertEquals("callback", form((NowValue) CALLBACK));
        assertEquals("not sticky", form(StickyValue.NOT_STICKY));
        assertEquals("2em", form(StickyValue.offset("2em")));
        assertEquals("default", form(BusinessHoursValue.DEFAULT));
        assertEquals("hours", form(BusinessHoursValue.of(BusinessHours.businessWeek())));
        assertEquals("meetings", form(ConstraintValue.groupId("meetings")));
        assertEquals("businessHours", form(ConstraintValue.BUSINESS_HOURS));
        assertEquals("windows", form(ConstraintValue.windows(BusinessHours.businessWeek())));
        assertEquals("none", form(EntryLimit.NONE));
        assertEquals("cell height", form(EntryLimit.CELL_HEIGHT));
        assertEquals("3", form(EntryLimit.of(3)));
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
