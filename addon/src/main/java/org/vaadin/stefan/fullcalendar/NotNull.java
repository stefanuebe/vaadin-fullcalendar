package org.vaadin.stefan.fullcalendar;

import javax.validation.Constraint;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Replacement for the NotNull annotation from javax, which has no real replacement in Jakarta
 * (Vaadin 24 / Spring Boot 3). This annotation is meant as a marker for any parameter or return value, that shall
 * not be null at any point.
 * @author Stefan Uebe
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { })
public @interface NotNull {
}
