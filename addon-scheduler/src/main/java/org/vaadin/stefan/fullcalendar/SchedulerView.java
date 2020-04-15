/*
 * Copyright 2018, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar;

/**
 * Enumeration of possible scheduler views.
 */
public enum SchedulerView implements CalendarView {

    /**  Day timeline **/
    TIMELINE_DAY("timelineDay"),
    /**  Week timeline **/
    TIMELINE_WEEK("timelineWeek"),
    /**  Month timeline **/
    TIMELINE_MONTH("timelineMonth"),
    /**  Year timeline **/
    TIMELINE_YEAR("timelineYear"),

    /** Day timeline showing also resources **/
    RESOURCE_TIMELINE_DAY("resourceTimelineDay"),
    /** Week timeline showing also resources **/
    RESOURCE_TIMELINE_WEEK("resourceTimelineWeek"),
    /** Month timeline showing also resources **/
    RESOURCE_TIMELINE_MONTH("resourceTimelineMonth"),
    /** Year timeline showing also resources **/
    RESOURCE_TIMELINE_YEAR("resourceTimelineYear"),

    /** Day timegrid showing also resources */
    RESOURCE_TIME_GRID_DAY("resourceTimeGridDay"), // was AGENDA_DAY
    /** Week timegrid showing also resources */
    RESOURCE_TIME_GRID_WEEK("resourceTimeGridWeek"), // was AGENDA_WEEK

    ;

    private final String clientSideName;

    SchedulerView(String clientSideName) {
        this.clientSideName = clientSideName;
    }

    @Override
    public String getClientSideValue() {
        return clientSideName;
    }

    @Override
    public String getName() {
        return name() + " (S)";
    }
}
