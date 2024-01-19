package org.vaadin.stefan.ui.view.samples;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import org.vaadin.stefan.fullcalendar.FullCalendar;

/**
 * @author Stefan Uebe
 */
@Getter
public abstract class AbstractSample extends VerticalLayout {

    private final FullCalendar calendar;

    public AbstractSample() {
        calendar = createCalendar();
        add(calendar);
        setFlexGrow(1, calendar);
        setHorizontalComponentAlignment(Alignment.STRETCH, calendar);

        buildSample(calendar);
    }

    protected FullCalendar createCalendar() {
        return new FullCalendar();
    }

    protected abstract void buildSample(FullCalendar calendar);
}
