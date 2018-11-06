package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinService;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {

    /**
     * Inits the vaadin service and mocks it to find your web components under "frontend://bower_components/XYZ" with
     * a file from "src/main/resources/META-INF/resources/frontend/bower_components/XYZ".
     *
     * @param webComponentUrlParts relative paths of web component html files
     */
    public static void initVaadinService(final String... webComponentUrlParts) {
        VaadinService vaadinService = Mockito.mock(VaadinService.class);
        VaadinService.setCurrent(vaadinService);

        Mockito.when(vaadinService.getDeploymentConfiguration())
                .thenAnswer(invocation -> {
                    DeploymentConfiguration config = Mockito.mock(DeploymentConfiguration.class);
                    Mockito.when(config.isProductionMode()).thenReturn(false);
                    return config;
                });

        if (webComponentUrlParts != null) {
            for (String part : webComponentUrlParts) {
                Mockito.when(vaadinService.
                        getResourceAsStream(ArgumentMatchers.eq("frontend://bower_components/" + part), ArgumentMatchers.any(), ArgumentMatchers.any())).
                        thenAnswer(invocation -> {
                            Path path = Paths.get("src/main/resources/META-INF/resources/frontend/bower_components/" + part).toAbsolutePath();
                            return Files.newInputStream(path);
                        });

            }
        }

    }
}
