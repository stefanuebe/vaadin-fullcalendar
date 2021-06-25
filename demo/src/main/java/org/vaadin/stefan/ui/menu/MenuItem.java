package org.vaadin.stefan.ui.menu;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.vaadin.flow.component.icon.VaadinIcon;

@Retention(RetentionPolicy.RUNTIME)
public @interface MenuItem {
	String label();

	//VaadinIcon icon();
}
