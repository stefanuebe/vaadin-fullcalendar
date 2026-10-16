/*
 * Copyright 2026, Stefan Uebe
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

import lombok.Getter;
import lombok.Setter;

/**
 * The scheduler options that can apply per view, besides those of {@link ViewOptions}. Null means not set. This class
 * is not public API. Users reach the fields through {@link FullCalendarScheduler#options()}.
 */
@Getter
@Setter
class SchedulerViewOptions extends ViewOptions {

    // FC-UPDATE: check the scheduler options of the new library version against these fields: new, renamed, removed
    //  or retyped options, and options that became view-specific or global. A renamed option changes only @JsonName.
    //  The test resource option-names.txt of this module lists the supported scheduler names as global or per view.

    /**
     * Groups the resources by a field each resource has, with a divider at the top of each group. The value is the
     * name of that field, for example {@code "groupId"}. The text of the divider comes from
     * {@code resourceGroupHeaderContent}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceGroupField">resourceGroupField</a>
     */
    private String resourceGroupField;

    /**
     * Whether child resources are expanded when the view loads. Set it to {@code false} to start with them collapsed.
     * Only the timeline view supports it.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourcesInitiallyExpanded">resourcesInitiallyExpanded</a>
     */
    private Boolean resourcesInitiallyExpanded;
}
