package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.dom.JsFunction;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsFunctionParameterNames;

import java.io.Serializable;

/**
 * PROTOTYPE #479: a snippet declared inside the add-on jar, to find out whether the build of an application collects
 * JsDefinition interfaces from a dependency jar. Throwaway, lives only on branch 479-csp-prototype.
 */
@JsDefinition
public interface PrototypeCspAddonSnippets extends Serializable {

    /** A dayCellClassNames callback: FullCalendar calls it synchronously and uses the returned class names. */
    @JsExpression("return info.date.getDay() === 0 ? $0 : '';")
    @JsFunctionParameterNames("info")
    JsFunction sundayClass(String className);
}
