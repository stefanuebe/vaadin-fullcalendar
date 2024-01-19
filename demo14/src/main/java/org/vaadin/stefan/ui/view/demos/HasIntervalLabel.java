package org.vaadin.stefan.ui.view.demos;

import java.time.LocalDate;
import java.util.Locale;

/**
 * @author Stefan Uebe
 */
@FunctionalInterface
public interface HasIntervalLabel {

    String formatIntervalLabel(LocalDate intervalStart, Locale locale);

}
