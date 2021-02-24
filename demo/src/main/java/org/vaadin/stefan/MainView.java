/*
 * Copyright 2020, Stefan Uebe
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
package org.vaadin.stefan;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLayout;

@Push
@PageTitle("FullCalendar Demo")
@Viewport("width=100vw, height=100vh")
public class MainView extends VerticalLayout implements RouterLayout {
	private static final long serialVersionUID = 1L;

	public MainView() {
        setSpacing(false);
        setPadding(false);
        setMargin(false);
        setSizeFull();

        HorizontalLayout title = new HorizontalLayout();

        String text = "<b>FullCalendar Demo</b> (Vaadin 14.4.8, FullCalendar addon: 3.0.0-SNAPSHOT (uses FC 5.5.0), FullCalendar Scheduler extension: 3.0.0-SNAPSHOT (uses scheduler extension libs 5.5.0)";

        Span span = new Span();
        span.getElement().setProperty("innerHTML", text);
        span.getElement().getStyle().set("text-align", "center");
        span.setWidthFull();

        title.add(span);
        title.setSpacing(false);
        title.setWidthFull();

        add(title);
    }
}
