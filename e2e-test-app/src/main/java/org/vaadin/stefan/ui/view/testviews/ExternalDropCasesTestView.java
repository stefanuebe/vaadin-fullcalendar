package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Locale;

/**
 * External drops onto a time grid: a draggable without entry data (only a DropEvent fires, #454), and the items of a
 * container draggable with an entry data callback (the drop resolves the draggable and carries the callback data,
 * #459), and the items of a container draggable with an entry template. A timed drop without entry data keeps the time
 * of the slot (#459).
 */
@Route(value = "external-drop-cases", layout = TestLayout.class)
@MenuItem(label = "External Drop Cases")
public class ExternalDropCasesTestView extends VerticalLayout {

    public ExternalDropCasesTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("External Drop Cases"));

        Span dropCount = badge("drop-count", "0");
        Span receiveCount = badge("receive-count", "0");
        Span dropDateTime = badge("drop-datetime", "");
        Span dropComponent = badge("drop-component", "");
        Span dropCreatedTitle = badge("drop-created-title", "");
        Span dropCreatedStart = badge("drop-created-start", "");
        Span receiveTitle = badge("receive-title", "");
        add(new Div(
                new Span("dropCount: "), dropCount,
                new Span(" | receiveCount: "), receiveCount,
                new Span(" | dropDateTime: "), dropDateTime,
                new Span(" | dropComponent: "), dropComponent,
                new Span(" | dropCreatedTitle: "), dropCreatedTitle,
                new Span(" | dropCreatedStart: "), dropCreatedStart,
                new Span(" | receiveTitle: "), receiveTitle));

        Div plain = new Div("Plain");
        plain.setId("plain-draggable");

        Div taskList = new Div();
        taskList.setId("task-list");
        Div taskA = new Div("Task A");
        taskA.addClassName("task-item");
        taskA.setId("task-a");
        Div taskB = new Div("Task B");
        taskB.addClassName("task-item");
        taskList.add(taskA, taskB);

        Div templateList = new Div();
        templateList.setId("template-list");
        Div templateItem = new Div("Template item");
        templateItem.addClassName("template-item");
        templateItem.setId("template-item");
        templateList.add(templateItem);

        add(new Div(plain, taskList, templateList));

        FullCalendar calendar = new FullCalendar();
        calendar.options().setLocale(Locale.ENGLISH);
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 10));
        calendar.options().setInitialView(CalendarViewImpl.TIME_GRID_WEEK);
        calendar.options().setScrollTime(Duration.ofHours(9));
        calendar.options().setDroppable(true);
        calendar.options().setTimeZone(Timezone.UTC);

        calendar.addDraggable(new Draggable(plain));
        calendar.addDraggable(new Draggable(taskList)
                .withItemSelector(".task-item")
                .withEventDataCallback(JsCallback.of("function(el) { return { title: el.innerText, duration: '01:00' }; }")));

        Entry template = new Entry();
        template.setTitle("Template");
        calendar.addDraggable(new Draggable(templateList, template).withItemSelector(".template-item"));

        calendar.addDropListener(e -> {
            dropCount.setText(String.valueOf(Integer.parseInt(dropCount.getText()) + 1));
            dropDateTime.setText(e.getDateTime().toString());
            dropComponent.setText(e.getDraggable().map(Draggable::getComponent).flatMap(c -> c.getId()).orElse("none"));
            dropCreatedTitle.setText(String.valueOf(e.getCreatedEntry().getTitle()));
            dropCreatedStart.setText(String.valueOf(e.getCreatedEntry().getStart()));
        });
        calendar.addEntryReceiveListener(e -> {
            receiveCount.setText(String.valueOf(Integer.parseInt(receiveCount.getText()) + 1));
            receiveTitle.setText(String.valueOf(e.getEntry().getTitle()));
        });

        add(calendar);
        setFlexGrow(1, calendar);
    }

    private static Span badge(String id, String text) {
        Span s = new Span(text);
        s.setId(id);
        return s;
    }
}
