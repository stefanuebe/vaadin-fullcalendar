package org.vaadin.stefan;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.aura.Aura;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Applies the Vaadin application theme and color scheme named in the query parameters of each navigation, so a test
 * can open any view with Lumo or Aura, light or dark: {@code ?theme=aura&scheme=dark}. Without parameters the app
 * uses Lumo and the system color scheme.
 */
@Component
public class AppTheme implements VaadinServiceInitListener {

    public static final String THEME_PARAMETER = "theme";
    public static final String SCHEME_PARAMETER = "scheme";

    private static void apply(UI ui, QueryParameters parameters) {
        String styleSheet = parameters.getSingleParameter(THEME_PARAMETER)
                .filter("aura"::equalsIgnoreCase)
                .map(aura -> Aura.STYLESHEET)
                .orElse(Lumo.STYLESHEET);

        // the stylesheet of the previous navigation is replaced, not added to
        Applied applied = ComponentUtil.getData(ui, Applied.class);
        if (applied == null || !applied.styleSheet().equals(styleSheet)) {
            if (applied != null) {
                applied.registration().remove();
            }
            ComponentUtil.setData(ui, Applied.class, new Applied(styleSheet, ui.getPage().addStyleSheet(styleSheet)));
        }

        ui.getPage().setColorScheme(parameters.getSingleParameter(SCHEME_PARAMETER)
                .flatMap(scheme -> Arrays.stream(ColorScheme.Value.values())
                        .filter(value -> value.name().equalsIgnoreCase(scheme))
                        .findFirst())
                .orElse(ColorScheme.Value.SYSTEM));
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiInit -> {
            UI ui = uiInit.getUI();
            ui.addBeforeEnterListener(enter -> apply(ui, enter.getLocation().getQueryParameters()));
        });
    }

    private record Applied(String styleSheet, Registration registration) {
    }
}
