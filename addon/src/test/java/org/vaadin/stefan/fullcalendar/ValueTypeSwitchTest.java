package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.DateRange;
import org.vaadin.stefan.fullcalendar.option.DateRangeValue;
import org.vaadin.stefan.fullcalendar.option.Order;
import org.vaadin.stefan.fullcalendar.option.OrderValue;
import org.vaadin.stefan.fullcalendar.option.SlotHeaderFormatValue;
import org.vaadin.stefan.fullcalendar.option.TextValue;
import org.vaadin.stefan.fullcalendar.option.WeekNumberCalculation;
import org.vaadin.stefan.fullcalendar.option.WeekNumberCalculationValue;
import org.vaadin.stefan.fullcalendar.option.WidthValue;
import org.vaadin.stefan.fullcalendar.option.AutoBoolean;
import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.BusinessHoursValue;
import org.vaadin.stefan.fullcalendar.option.ClassName;
import org.vaadin.stefan.fullcalendar.option.ClassNameValue;
import org.vaadin.stefan.fullcalendar.option.ConstraintValue;
import org.vaadin.stefan.fullcalendar.option.ContentValue;
import org.vaadin.stefan.fullcalendar.option.CssSelector;
import org.vaadin.stefan.fullcalendar.option.CssSelectorValue;
import org.vaadin.stefan.fullcalendar.option.DateFormat;
import org.vaadin.stefan.fullcalendar.option.DateFormatValue;
import org.vaadin.stefan.fullcalendar.option.EntryLimitValue;
import org.vaadin.stefan.fullcalendar.option.HeaderAlign;
import org.vaadin.stefan.fullcalendar.option.HeaderAlignValue;
import org.vaadin.stefan.fullcalendar.option.HeightValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.ListDayFormatValue;
import org.vaadin.stefan.fullcalendar.option.ListTextValue;
import org.vaadin.stefan.fullcalendar.option.ToolbarValue;
import org.vaadin.stefan.fullcalendar.option.NoneValue;
import org.vaadin.stefan.fullcalendar.option.InitialDateValue;
import org.vaadin.stefan.fullcalendar.option.LocaleValue;
import org.vaadin.stefan.fullcalendar.option.NowValue;
import org.vaadin.stefan.fullcalendar.option.Overlap;
import org.vaadin.stefan.fullcalendar.option.OverlapValue;
import org.vaadin.stefan.fullcalendar.option.StickyValue;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import org.vaadin.stefan.fullcalendar.option.ViewName;
import org.vaadin.stefan.fullcalendar.option.ViewNameValue;
import org.vaadin.stefan.fullcalendar.option.MoreLinkClickValue;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Each value type can be switched over without a {@code default} branch. The test fails to compile when a value type
 * gets a form these switches do not name.
 */
class ValueTypeSwitchTest {

    private static String form(AutoBoolean value) {
        return switch (value) {
            case TRUE -> "true";
            case FALSE -> "false";
            case AUTO -> "auto";
        };
    }

    private static String form(BusinessHoursValue value) {
        return switch (value) {
            case NoneValue.None ignored -> "none";
            case BusinessHoursValue.Default ignored -> "default";
            case BusinessHoursValue.Hours hours -> "hours";
        };
    }

    private static String form(ConstraintValue value) {
        return switch (value) {
            case ConstraintValue.GroupId groupId -> groupId.groupId();
            case ConstraintValue.CalendarBusinessHours ignored -> "businessHours";
            case ConstraintValue.Windows windows -> windows.windows().stream().map(window -> switch (window) {
                case BusinessHours hours -> "hours";
                case ConstraintValue.RRuleWindow rule -> "rule";
                case ConstraintValue.DateSpan span -> "dates";
                case ConstraintValue.DateTimeSpan span -> "date times";
            }).toList() + " " + windows.resourceIds();
            case ConstraintValue.Resources resources -> resources.resourceIds().toString();
        };
    }

    private static String form(DateFormatValue value) {
        return switch (value) {
            case DateFormat format -> "format";
            case JsCallback callback -> "callback";
        };
    }

    private static String form(DateRangeValue value) {
        return switch (value) {
            case DateRange range -> "range";
            case JsCallback callback -> "callback";
        };
    }

    private static String form(OrderValue value) {
        return switch (value) {
            case Order order -> order.keys().stream().map(key -> switch (key) {
                case Order.Property property -> property.name();
                case JsCallback callback -> "callback";
            }).toList().toString();
            case JsCallback callback -> "callback";
        };
    }

    private static String form(EntryLimitValue value) {
        return switch (value) {
            case NoneValue.None ignored -> "none";
            case EntryLimitValue.CellHeight ignored -> "cell height";
            case EntryLimitValue.Count count -> String.valueOf(count.count());
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
            case ContentValue.Html html -> html.html();
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
            case NoneValue.None ignored -> "none";
        };
    }

    private static String form(SlotHeaderFormatValue value) {
        return switch (value) {
            case DateFormat format -> "format";
            case JsCallback callback -> "callback";
            case SlotHeaderFormatValue.Tiers tiers -> "tiers";
        };
    }

    private static String form(ListTextValue value) {
        return switch (value) {
            case TextContent text -> text.text();
            case NoneValue.None ignored -> "none";
        };
    }

    private static String form(ToolbarValue value) {
        return switch (value) {
            case ToolbarValue.StartEnd sections -> sections.start() + "|" + sections.center() + "|" + sections.end();
            case ToolbarValue.LeftRight sections -> sections.left() + "|" + sections.center() + "|" + sections.right();
            case NoneValue.None ignored -> "none";
        };
    }

    private static String form(InitialDateValue value) {
        return switch (value) {
            case InitialDateValue.Day day -> day.date().toString();
            case InitialDateValue.DateTime dateTime -> dateTime.dateTime().toString();
        };
    }

    private static String form(LocaleValue value) {
        return switch (value) {
            case LocaleValue.Code code -> code.locale().toString();
            case LocaleValue.Custom custom -> custom.locale() + " " + custom.options();
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

    private static String form(TextValue value) {
        return switch (value) {
            case TextContent text -> text.text();
            case JsCallback callback -> "callback";
        };
    }

    private static String form(WeekNumberCalculationValue value) {
        return switch (value) {
            case WeekNumberCalculation calculation -> calculation.name();
            case JsCallback callback -> "callback";
        };
    }

    private static String form(WidthValue value) {
        return switch (value) {
            case WidthValue.Pixels pixels -> String.valueOf(pixels.pixels());
            case WidthValue.Percent percent -> percent.percent() + "%";
        };
    }

    private static String form(MoreLinkClickValue value) {
        return switch (value) {
            case MoreLinkClickValue.Action action -> action.name();
            case ViewName name -> name.name();
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
    void everyValueTypeHasASwitch_thatNamesEachSample() throws ReflectiveOperationException {
        for (var entry : ConverterTest.VALUE_TYPES.entrySet()) {
            Method form;
            try {
                form = ValueTypeSwitchTest.class.getDeclaredMethod("form", entry.getKey());
            } catch (NoSuchMethodException e) {
                throw new AssertionError("Add a form(" + entry.getKey().getSimpleName() + ") switch", e);
            }
            for (Object sample : entry.getValue().samples().keySet()) {
                assertNotNull(form.invoke(null, sample), entry.getKey().getSimpleName() + ": " + sample);
            }
        }
    }
}
