package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonObject;
import elemental.json.JsonValue;
import org.junit.jupiter.api.Assertions;

public class TestUtils {

    private static boolean inited = false;

    /**
     * Inits the vaadin service and mocks it to find your web components under "frontend://bower_components/XYZ" with
     * a file from "src/main/resources/META-INF/resources/frontend/bower_components/XYZ".
     *
     * @param webComponentUrlParts relative paths of web component html files
     */
    @Deprecated
    public static void initVaadinService(final String... webComponentUrlParts) {
//        if (!inited) {
//
//            VaadinService vaadinService = Mockito.mock(VaadinService.class);
//            VaadinService.setCurrent(vaadinService);
//
//            Mockito.when(vaadinService.getDeploymentConfiguration())
//                    .thenAnswer(invocation -> {
//                        DeploymentConfiguration config = Mockito.mock(DeploymentConfiguration.class);
//                        Mockito.when(config.isProductionMode()).thenReturn(false);
//                        return config;
//                    });
//
//            if (webComponentUrlParts != null) {
//                for (String part : webComponentUrlParts) {
//                    Mockito.when(vaadinService.
//                            getResourceAsStream(ArgumentMatchers.eq("frontend://bower_components/" + part), ArgumentMatchers.any(), ArgumentMatchers.any())).
//                            thenAnswer(invocation -> {
//                                Path path = Paths.get("src/main/resources/META-INF/resources/frontend/bower_components/" + part).toAbsolutePath();
//                                if (!Files.isRegularFile(path)) {
//                                    path = Paths.get("src/test/resources/META-INF/resources/frontend/bower_components/" + part).toAbsolutePath();
//                                }
//                                return Files.newInputStream(path);
//                            });
//
//                }
//            }
//
//            inited = true;
//        }
    }

    public static void assertJsonType(JsonObject object, String key, Class<? extends JsonValue> expectedType) {
        JsonValue jsonValue = object.get(key);
        Assertions.assertNotNull(jsonValue, "Json value for key '" + key + "' returned null, expected a json value being a sub type of " + expectedType);

        Class<? extends JsonValue> aClass = jsonValue.getClass();
        if (!expectedType.isAssignableFrom(aClass)) {
            Assertions.fail("Json value for key '" + key + "': Expected sub type of " + expectedType + ", but got " + aClass);
        }
    }

    public static void assertJsonMissingKey(JsonObject object, String key) {
        if (object.hasKey(key)) {
            Assertions.fail("Expected json object to not have key '" + key + "'");
        }
    }
}
