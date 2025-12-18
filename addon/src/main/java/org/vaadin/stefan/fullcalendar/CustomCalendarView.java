package org.vaadin.stefan.fullcalendar;


import tools.jackson.databind.node.ObjectNode;

/**
 * Interface for custom calendar views. This is used to create custom views for the calendar.
 * @author Stefan Uebe
 */
public interface CustomCalendarView extends CalendarView {

    /**
     * Returns the view settings as a json object. This is used to initialize the view.
     * The returned json object will be placed inside the view object, e.g.:
     * <pre>
     *  views: {
     *      customViewName: {
     *          ... here goes the content of the returned json object
     *      }
     *  }
     * </pre>
     * The {@code customViewName} is taken from this instance's {@link #getClientSideValue()} method.
     * <br><br>
     * A json object with key "days" for an instance with the client side value "my-view" will result in a view setup like this:
     * <pre>
     *  views: {
     *      my-view: {
     *          days: ...
     *      }
     *  }
     * </pre>
     *
     * See the <a href="https://fullcalendar.io/docs/custom-view-with-settings">FullCalendar docs</a> for details.
     * @return view settings
     */
    ObjectNode getViewSettings();

    @Override
    default String getName() {
        return getClientSideValue();
    }

    /**
     * This class will be used by the FullCalender, when the initial options contains any views objects. This
     * shall prevent any issues with unknown views.
     */
    class AnonymousCustomCalendarView implements CustomCalendarView {
        private final String name;
        private final ObjectNode viewSettings;

        public AnonymousCustomCalendarView(String name, ObjectNode viewSettings) {
            this.name = name;
            this.viewSettings = viewSettings;
        }

        @Override
        public String getClientSideValue() {
            return name;
        }

        @Override
        public ObjectNode getViewSettings() {
            return viewSettings;
        }
    }
}
