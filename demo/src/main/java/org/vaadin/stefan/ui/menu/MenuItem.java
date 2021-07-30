package org.vaadin.stefan.ui.menu;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MenuItem {
	String label();

	//VaadinIcon icon();
}
