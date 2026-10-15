package org.vaadin.stefan.fullcalendar;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. The value of an option that takes CSS class names: either fixed
 * class names or a {@link JsCallback} that returns them. Sealed in this package, because {@link JsCallback} lives
 * here and a sealed type can only permit classes of its own package outside a named module.
 */
public sealed interface ClassNameValue extends java.io.Serializable permits ClassName, JsCallback {
}
