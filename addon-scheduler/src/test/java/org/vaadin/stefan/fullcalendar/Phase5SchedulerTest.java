package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for Phase 5 scheduler/resource features:
 * 5.1 resourceAreaColumns typed API
 * 5.2 resourceGroupField typed setter
 * 5.3 Resource group render hooks
 * 5.4 Resource area header render hooks
 * 5.5 datesAboveResources typed setter
 * 5.7 eventMinWidth typed setter
 * 5.8 Resource lifecycle callbacks
 * 5.10 Resource property model improvements (mutable title/color)
 * 5.11 Per-resource event property overrides
 * 5.12 Typo fix setResourceLablelWillUnmountCallback (already done, verified here)
 */
public class Phase5SchedulerTest {

    private FullCalendarScheduler calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendarScheduler();
    }

    // -------------------------------------------------------------------------
    // ResourceAreaColumn tests
    // -------------------------------------------------------------------------

    @Test
    void testResourceAreaColumnMinimal() {
        ResourceAreaColumn col = new ResourceAreaColumn("title");
        ObjectNode json = col.toJson();

        Assertions.assertTrue(json.has("field"), "json has field");
        Assertions.assertEquals("title", json.get("field").asString(), "field value");
        Assertions.assertFalse(json.has("headerContent"), "no headerContent");
        Assertions.assertFalse(json.has("width"), "no width");
        Assertions.assertFalse(json.has("group"), "no group (false is omitted)");
        Assertions.assertFalse(json.has("headerClassNames"), "no headerClassNames");
        Assertions.assertFalse(json.has("headerDidMount"), "no headerDidMount");
        Assertions.assertFalse(json.has("headerWillUnmount"), "no headerWillUnmount");
    }

    @Test
    void testResourceAreaColumnFull() {
        ResourceAreaColumn col = new ResourceAreaColumn("department", "Department")
                .withWidth("150px")
                .withGroup(true)
                .withHeaderClassNames("function(info) { return ['dept-header']; }")
                .withHeaderDidMount("function(info) { console.log('mount'); }")
                .withHeaderWillUnmount("function(info) { console.log('unmount'); }");

        ObjectNode json = col.toJson();

        Assertions.assertEquals("department", json.get("field").asString(), "field");
        Assertions.assertEquals("Department", json.get("headerContent").asString(), "headerContent");
        Assertions.assertEquals("150px", json.get("width").asString(), "width");
        Assertions.assertTrue(json.get("group").asBoolean(), "group is true");
        Assertions.assertEquals("function(info) { return ['dept-header']; }", json.get("headerClassNames").asString(), "headerClassNames");
        Assertions.assertEquals("function(info) { console.log('mount'); }", json.get("headerDidMount").asString(), "headerDidMount");
        Assertions.assertEquals("function(info) { console.log('unmount'); }", json.get("headerWillUnmount").asString(), "headerWillUnmount");
    }

    @Test
    void testResourceAreaColumnGroupTrue() {
        ResourceAreaColumn col = new ResourceAreaColumn("category").withGroup(true);
        ObjectNode json = col.toJson();

        Assertions.assertTrue(json.has("group"), "group key present when true");
        Assertions.assertTrue(json.get("group").asBoolean(), "group value is true");
    }

    @Test
    void testResourceAreaColumnGroupFalse_NotSerialized() {
        ResourceAreaColumn col = new ResourceAreaColumn("category").withGroup(false);
        ObjectNode json = col.toJson();

        Assertions.assertFalse(json.has("group"), "group key absent when false (clean JSON)");
    }

    @Test
    void testResourceAreaColumnRenderHooks() {
        String classNames = "function(info) { return ['h1', 'h2']; }";
        String didMount = "function(info) { /* mount */ }";
        String willUnmount = "function(info) { /* unmount */ }";

        ResourceAreaColumn col = new ResourceAreaColumn("capacity")
                .withHeaderClassNames(classNames)
                .withHeaderDidMount(didMount)
                .withHeaderWillUnmount(willUnmount);

        Assertions.assertEquals(classNames, col.getHeaderClassNames());
        Assertions.assertEquals(didMount, col.getHeaderDidMount());
        Assertions.assertEquals(willUnmount, col.getHeaderWillUnmount());

        ObjectNode json = col.toJson();
        Assertions.assertEquals(classNames, json.get("headerClassNames").asString());
        Assertions.assertEquals(didMount, json.get("headerDidMount").asString());
        Assertions.assertEquals(willUnmount, json.get("headerWillUnmount").asString());
    }

    // -------------------------------------------------------------------------
    // Scheduler option setter tests
    // -------------------------------------------------------------------------

    @Test
    void testSetResourceGroupField() {
        calendar.setResourceGroupField("department");

        Optional<Object> option = calendar.getOption("resourceGroupField");
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals("department", option.get());
    }

    @Test
    void testSetDatesAboveResources() {
        calendar.setDatesAboveResources(true);

        Optional<Object> option = calendar.getOption("datesAboveResources");
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(true, option.get());
    }

    @Test
    void testSetDatesAboveResourcesFalse() {
        calendar.setDatesAboveResources(false);

        Optional<Object> option = calendar.getOption("datesAboveResources");
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(false, option.get());
    }

    @Test
    void testSetEventMinWidth() {
        calendar.setEventMinWidth(10);

        Optional<Object> option = calendar.getOption("eventMinWidth");
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(10, option.get());
    }

    @Test
    void testSetResourceAreaColumns() {
        List<ResourceAreaColumn> columns = List.of(
                new ResourceAreaColumn("title", "Resource").withWidth("200px"),
                new ResourceAreaColumn("department", "Department").withWidth("150px").withGroup(true)
        );

        calendar.setResourceAreaColumns(columns);

        Optional<Object> option = calendar.getOption("resourceAreaColumns");
        Assertions.assertTrue(option.isPresent(), "resourceAreaColumns option is set");
        // The server-side value stored is the original List
        Assertions.assertSame(columns, option.get(), "server-side value is the original list");
    }

    @Test
    void testSetResourceAreaColumnsVarargs() {
        ResourceAreaColumn col1 = new ResourceAreaColumn("title", "Title");
        ResourceAreaColumn col2 = new ResourceAreaColumn("eventColor", "Color");

        calendar.setResourceAreaColumns(col1, col2);

        Optional<Object> option = calendar.getOption("resourceAreaColumns");
        Assertions.assertTrue(option.isPresent(), "resourceAreaColumns option is set via varargs");
    }

    // -------------------------------------------------------------------------
    // JS callback tests — verify no exception is thrown (client-side state
    // cannot be verified in unit tests without a running browser)
    // -------------------------------------------------------------------------

    @Test
    void testSetResourceGroupClassNamesCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourceGroupClassNamesCallback("function(info) { return ['g']; }")
        );
    }

    @Test
    void testSetResourceGroupContentCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourceGroupContentCallback("function(info) { return info.groupValue; }")
        );
    }

    @Test
    void testSetResourceGroupDidMountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourceGroupDidMountCallback("function(info) { }")
        );
    }

    @Test
    void testSetResourceGroupWillUnmountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourceGroupWillUnmountCallback("function(info) { }")
        );
    }

    @Test
    void testSetResourceAreaHeaderClassNamesCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourceAreaHeaderClassNamesCallback("function(info) { return ['custom-header']; }")
        );
    }

    @Test
    void testSetResourceAreaHeaderDidMountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourceAreaHeaderDidMountCallback("function(info) { }")
        );
    }

    @Test
    void testSetResourceAreaHeaderWillUnmountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourceAreaHeaderWillUnmountCallback("function(info) { }")
        );
    }

    @Test
    void testSetResourceAddCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourceAddCallback("function(info) { }")
        );
    }

    @Test
    void testSetResourceChangeCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourceChangeCallback("function(info) { }")
        );
    }

    @Test
    void testSetResourceRemoveCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourceRemoveCallback("function(info) { }")
        );
    }

    @Test
    void testSetResourcesSetCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourcesSetCallback("function(info) { }")
        );
    }

    // -------------------------------------------------------------------------
    // Resource mutability (5.10)
    // -------------------------------------------------------------------------

    @Test
    void testResourceSetTitle() {
        Resource resource = new Resource("r1", "Original", null);
        resource.setTitle("Updated");

        Assertions.assertEquals("Updated", resource.getTitle(), "getTitle() returns updated value");

        ObjectNode json = resource.toJson();
        Assertions.assertEquals("Updated", json.get("title").asString(), "toJson title is updated");
    }

    @Test
    void testResourceSetColor() {
        Resource resource = new Resource("r1", "Room", "blue");
        resource.setColor("#ff0000");

        Assertions.assertEquals("#ff0000", resource.getColor(), "getColor() returns updated value");

        ObjectNode json = resource.toJson();
        Assertions.assertEquals("#ff0000", json.get("eventColor").asString(), "toJson eventColor is updated");
    }

    @Test
    void testResourceSetTitleNullDoesNotThrow() {
        Resource resource = new Resource("r1", "Title", null);
        Assertions.assertDoesNotThrow(() -> resource.setTitle(null));
        Assertions.assertNull(resource.getTitle());
    }

    @Test
    void testUpdateResourceNoExceptionWhenNotAttached() {
        // setTitle/setColor on unattached resource should not throw
        Resource resource = new Resource("r1", "Title", "red");
        Assertions.assertDoesNotThrow(() -> resource.setTitle("New Title"));
        Assertions.assertDoesNotThrow(() -> resource.setColor("green"));
    }

    // -------------------------------------------------------------------------
    // Per-resource event property overrides (5.11)
    // -------------------------------------------------------------------------

    @Test
    void testResourceEventBackgroundColor() {
        Resource resource = new Resource();
        resource.setEventBackgroundColor("#aabbcc");

        Assertions.assertEquals("#aabbcc", resource.getEventBackgroundColor());

        ObjectNode json = resource.toJson();
        Assertions.assertTrue(json.has("eventBackgroundColor"), "json has eventBackgroundColor");
        Assertions.assertEquals("#aabbcc", json.get("eventBackgroundColor").asString());
    }

    @Test
    void testResourceEventBackgroundColorNull_NotSerialized() {
        Resource resource = new Resource();
        // eventBackgroundColor is null by default

        ObjectNode json = resource.toJson();
        Assertions.assertFalse(json.has("eventBackgroundColor"), "null eventBackgroundColor not serialized");
    }

    @Test
    void testResourceEventBorderColor() {
        Resource resource = new Resource();
        resource.setEventBorderColor("#001122");

        Assertions.assertEquals("#001122", resource.getEventBorderColor());

        ObjectNode json = resource.toJson();
        Assertions.assertTrue(json.has("eventBorderColor"), "json has eventBorderColor");
        Assertions.assertEquals("#001122", json.get("eventBorderColor").asString());
    }

    @Test
    void testResourceEventTextColor() {
        Resource resource = new Resource();
        resource.setEventTextColor("white");

        Assertions.assertEquals("white", resource.getEventTextColor());

        ObjectNode json = resource.toJson();
        Assertions.assertTrue(json.has("eventTextColor"), "json has eventTextColor");
        Assertions.assertEquals("white", json.get("eventTextColor").asString());
    }

    @Test
    void testResourceEventConstraint() {
        Resource resource = new Resource();
        resource.setEventConstraint("businessHours");

        Assertions.assertEquals("businessHours", resource.getEventConstraint());

        ObjectNode json = resource.toJson();
        Assertions.assertTrue(json.has("eventConstraint"), "json has eventConstraint");
        Assertions.assertEquals("businessHours", json.get("eventConstraint").asString());
    }

    @Test
    void testResourceEventOverlapTrue() {
        Resource resource = new Resource();
        resource.setEventOverlap(true);

        Assertions.assertEquals(Boolean.TRUE, resource.getEventOverlap());

        ObjectNode json = resource.toJson();
        Assertions.assertTrue(json.has("eventOverlap"), "json has eventOverlap");
        Assertions.assertTrue(json.get("eventOverlap").asBoolean(), "eventOverlap is true");
    }

    @Test
    void testResourceEventOverlapFalse() {
        Resource resource = new Resource();
        resource.setEventOverlap(false);

        Assertions.assertEquals(Boolean.FALSE, resource.getEventOverlap());

        ObjectNode json = resource.toJson();
        Assertions.assertTrue(json.has("eventOverlap"), "json has eventOverlap (false is serialized)");
        Assertions.assertFalse(json.get("eventOverlap").asBoolean(), "eventOverlap is false");
    }

    @Test
    void testResourceEventOverlapNull_NotSerialized() {
        Resource resource = new Resource();
        // eventOverlap is null by default

        Assertions.assertNull(resource.getEventOverlap());

        ObjectNode json = resource.toJson();
        Assertions.assertFalse(json.has("eventOverlap"), "null eventOverlap not serialized");
    }

    @Test
    void testResourceEventClassNames() {
        Resource resource = new Resource();
        Set<String> classNames = new LinkedHashSet<>();
        classNames.add("class-a");
        classNames.add("class-b");
        resource.setEventClassNames(classNames);

        Set<String> returned = resource.getEventClassNames();
        Assertions.assertNotNull(returned);
        Assertions.assertTrue(returned.contains("class-a"), "contains class-a");
        Assertions.assertTrue(returned.contains("class-b"), "contains class-b");
        Assertions.assertEquals(2, returned.size());

        ObjectNode json = resource.toJson();
        Assertions.assertTrue(json.has("eventClassNames"), "json has eventClassNames");
        ArrayNode classNamesJson = (ArrayNode) json.get("eventClassNames");
        Assertions.assertEquals(2, classNamesJson.size(), "json eventClassNames has 2 elements");
    }

    @Test
    void testResourceEventClassNamesNull_NotSerialized() {
        Resource resource = new Resource();
        resource.setEventClassNames(null);

        Assertions.assertNull(resource.getEventClassNames());

        ObjectNode json = resource.toJson();
        Assertions.assertFalse(json.has("eventClassNames"), "null eventClassNames not serialized");
    }

    @Test
    void testResourceEventClassNamesUnmodifiable() {
        Resource resource = new Resource();
        Set<String> classNames = new LinkedHashSet<>();
        classNames.add("readonly-class");
        resource.setEventClassNames(classNames);

        Set<String> returned = resource.getEventClassNames();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> returned.add("should-fail"),
                "returned set should be unmodifiable");
    }

    // -------------------------------------------------------------------------
    // Typo fix (5.12) — deprecated method delegates correctly
    // -------------------------------------------------------------------------

    @Test
    void testDeprecatedTypoMethod() {
        // setResourceLablelWillUnmountCallback (typo) should not throw and should delegate
        Assertions.assertDoesNotThrow(() ->
                calendar.setResourceLablelWillUnmountCallback("function(info) { }")
        );
    }
}
