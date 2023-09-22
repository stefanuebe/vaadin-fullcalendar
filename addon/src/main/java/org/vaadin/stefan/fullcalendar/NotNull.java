package org.vaadin.stefan.fullcalendar;

import javax.validation.Constraint;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is meant as a marker for any parameter or return value, that shall
 * not be null at any point.
 * <br><br>
 * Replacement for the built in NotNull annotation from javax and jakarta to allow the usage of this lib in both
 * environments.
 * @author Stefan Uebe
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
public @interface NotNull {
}
