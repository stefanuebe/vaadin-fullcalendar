package org.vaadin.stefan.samples;

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
        calendar = new FullCalendar();
        add(calendar);
        setFlexGrow(1, calendar);
        setHorizontalComponentAlignment(Alignment.STRETCH, calendar);

        buildSample(calendar);
    }

    protected abstract void buildSample(FullCalendar calendar);
}
