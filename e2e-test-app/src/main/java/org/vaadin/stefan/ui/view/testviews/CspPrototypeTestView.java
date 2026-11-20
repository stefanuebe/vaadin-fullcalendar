package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.JsFunction;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsFunctionParameterNames;
import com.vaadin.flow.js.JsSnippets;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.PrototypeCspAddonSnippets;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * PROTOTYPE #479: callbacks as JsDefinition snippets instead of JsCallback strings, run under vaadin.csp=strict.
 * Throwaway, lives only on branch 479-csp-prototype.
 * <p>
 * Route: /test/csp-prototype. The query parameter {@code noAutoId} turns off the add-on's own eventDidMount callback.
 */
@Route(value = "csp-prototype", layout = TestLayout.class)
public class CspPrototypeTestView extends VerticalLayout {

    /** A snippet declared in the application. FullCalendar calls it with the event info and uses the result. */
    @JsDefinition
    public interface AppSnippets extends Serializable {
        @JsExpression("return $0 + info.event.title.length;")
        @JsFunctionParameterNames("info")
        JsFunction titleLengthClass(String prefix);
    }

    private final FullCalendar calendar = new FullCalendar();

    public CspPrototypeTestView() {
        setSizeFull();
        add(new H2("CSP prototype"));
        calendar.getElement().setAttribute("data-testid", "calendar");
        Entry entry = new Entry();
        entry.setTitle("Hello");
        entry.setStart(LocalDate.now().atTime(10, 0));
        calendar.setEntryProvider(new InMemoryEntryProvider<>(List.of(entry)));
        add(calendar);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (attachEvent.getUI().getInternals().getActiveViewLocation().getQueryParameters()
                .getParameters().containsKey("noAutoId")) {
            calendar.setAutoProvideEntryIdOnClient(false);
        }
        // after the first render, setOptionFunction needs the calendar object
        attachEvent.getUI().beforeClientResponse(calendar, ctx -> {
            calendar.getElement().callJsFunction("setOptionFunction", "eventClass",
                    JsSnippets.of(AppSnippets.class).titleLengthClass("csp-app-len-"));
            calendar.getElement().callJsFunction("setOptionFunction", "dayCellClass",
                    JsSnippets.of(PrototypeCspAddonSnippets.class).sundayClass("csp-addon-sunday"));
        });
    }
}
