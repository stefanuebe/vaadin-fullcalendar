package org.vaadin.stefan.ui.view.demos.tooltip;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import org.vaadin.stefan.fullcalendar.FullCalendarScheduler;

@NpmPackage(value = "tippy.js", version = "6.2.3")
@Tag("full-calendar-with-tooltip")
@JsModule("./full-calendar-with-tooltip.ts")
@CssImport("tippy.js/dist/tippy.css")
@CssImport("tippy.js/themes/light.css")
public class FullCalendarWithTooltip extends FullCalendarScheduler {
	private static final long serialVersionUID = 1L;

    public FullCalendarWithTooltip() {
        super(3);
    }

    public FullCalendarWithTooltip(int entryLimit) {
        super(entryLimit);
    }

    public FullCalendarWithTooltip(JsonObject initialOptions) {
        super(initialOptions);
    }

}
