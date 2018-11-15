package org.vaadin.stefan;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("main-view")
@HtmlImport("main-view.html")
@Push
@PageTitle("bakery_12")
@Viewport("width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes")
public class MainView extends PolymerTemplate<TemplateModel> implements RouterLayout {

    @Id("title")
    private HorizontalLayout title;

    public MainView() {
        title.add(new H3("full calendar demo"), new Span("(FullCalendar addon: 1.6.0, FullCalendar Scheduler extension: 1.1.0)"));
        title.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
    }
}
