package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for scheduler/resource features:
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
public class SchedulerFeaturesTest {

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
        JsonObject json = col.toJson();

        Assertions.assertTrue(json.hasKey("field"), "json has field");
        Assertions.assertEquals("title", json.get("field").asString(), "field value");
        Assertions.assertFalse(json.hasKey("headerContent"), "no headerContent");
        Assertions.assertFalse(json.hasKey("width"), "no width");
        Assertions.assertFalse(json.hasKey("group"), "no group (false is omitted)");
        Assertions.assertFalse(json.hasKey("headerClassNames"), "no headerClassNames");
        Assertions.assertFalse(json.hasKey("headerDidMount"), "no headerDidMount");
        Assertions.assertFalse(json.hasKey("headerWillUnmount"), "no headerWillUnmount");
    }

    @Test
    void testResourceAreaColumnFull() {
        ResourceAreaColumn col = new ResourceAreaColumn("department", "Department")
                .withWidth("150px")
                .withGroup(true)
                .withHeaderClassNames("function(info) { return ['dept-header']; }")
                .withHeaderDidMount("function(info) { console.log('mount'); }")
                .withHeaderWillUnmount("function(info) { console.log('unmount'); }");

        JsonObject json = col.toJson();

        Assertions.assertEquals("department", json.get("field").asString(), "field");
        Assertions.assertEquals("Department", json.get("headerContent").asString(), "headerContent");
        Assertions.assertEquals("150px", json.get("width").asString(), "width");
        Assertions.assertTrue(json.get("group").asBoolean(), "group is true");
        // headerClassNames is still a plain string (static class name)
        Assertions.assertEquals("function(info) { return ['dept-header']; }", json.get("headerClassNames").asString(), "headerClassNames");
        // headerDidMount and headerWillUnmount are now JsCallback markers
        Assertions.assertEquals("function(info) { console.log('mount'); }", ((JsonObject) json.get("headerDidMount")).get("__jsCallback").asString(), "headerDidMount");
        Assertions.assertEquals("function(info) { console.log('unmount'); }", ((JsonObject) json.get("headerWillUnmount")).get("__jsCallback").asString(), "headerWillUnmount");
    }

    @Test
    void testResourceAreaColumnGroupTrue() {
        ResourceAreaColumn col = new ResourceAreaColumn("category").withGroup(true);
        JsonObject json = col.toJson();

        Assertions.assertTrue(json.hasKey("group"), "group key present when true");
        Assertions.assertTrue(json.get("group").asBoolean(), "group value is true");
    }

    @Test
    void testResourceAreaColumnGroupFalse_NotSerialized() {
        ResourceAreaColumn col = new ResourceAreaColumn("category").withGroup(false);
        JsonObject json = col.toJson();

        Assertions.assertFalse(json.hasKey("group"), "group key absent when false (clean JSON)");
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

        // headerClassNames with String overload stays as String
        Assertions.assertEquals(classNames, col.getHeaderClassNames());
        // headerDidMount/willUnmount with String overload wraps in JsCallback
        Assertions.assertNotNull(col.getHeaderDidMount());
        Assertions.assertEquals(didMount, col.getHeaderDidMount().getJsFunction());
        Assertions.assertNotNull(col.getHeaderWillUnmount());
        Assertions.assertEquals(willUnmount, col.getHeaderWillUnmount().getJsFunction());

        JsonObject json = col.toJson();
        Assertions.assertEquals(classNames, json.get("headerClassNames").asString());
        Assertions.assertEquals(didMount, ((JsonObject) json.get("headerDidMount")).get("__jsCallback").asString());
        Assertions.assertEquals(willUnmount, ((JsonObject) json.get("headerWillUnmount")).get("__jsCallback").asString());
    }

    // -------------------------------------------------------------------------
    // ResourceAreaColumn cell-level render hooks
    // -------------------------------------------------------------------------

    @Test
    void testResourceAreaColumn_cellContent_string() {
        ResourceAreaColumn col = new ResourceAreaColumn("field").withCellContent("static text");
        JsonObject json = col.toJson();
        Assertions.assertEquals("static text", json.get("cellContent").asString());
    }

    @Test
    void testResourceAreaColumn_cellContent_jsCallback() {
        ResourceAreaColumn col = new ResourceAreaColumn("field")
                .withCellContent(JsCallback.of("function(info) { return info.fieldValue; }"));
        JsonObject json = col.toJson();
        Assertions.assertEquals(JsonType.OBJECT, json.get("cellContent").getType());
        Assertions.assertEquals("function(info) { return info.fieldValue; }",
                ((JsonObject) json.get("cellContent")).get("__jsCallback").asString());
    }

    @Test
    void testResourceAreaColumn_cellClassNames_string() {
        ResourceAreaColumn col = new ResourceAreaColumn("field").withCellClassNames("my-class");
        JsonObject json = col.toJson();
        Assertions.assertEquals("my-class", json.get("cellClassNames").asString());
    }

    @Test
    void testResourceAreaColumn_cellClassNames_jsCallback() {
        ResourceAreaColumn col = new ResourceAreaColumn("field")
                .withCellClassNames(JsCallback.of("function(info) { return ['a']; }"));
        JsonObject json = col.toJson();
        Assertions.assertEquals(JsonType.OBJECT, json.get("cellClassNames").getType());
        Assertions.assertEquals("function(info) { return ['a']; }",
                ((JsonObject) json.get("cellClassNames")).get("__jsCallback").asString());
    }

    @Test
    void testResourceAreaColumn_cellDidMount() {
        ResourceAreaColumn col = new ResourceAreaColumn("field")
                .withCellDidMount("function(info) { }");
        JsonObject json = col.toJson();
        Assertions.assertEquals(JsonType.OBJECT, json.get("cellDidMount").getType());
        Assertions.assertNotNull(((JsonObject) json.get("cellDidMount")).get("__jsCallback"));
    }

    @Test
    void testResourceAreaColumn_cellWillUnmount() {
        ResourceAreaColumn col = new ResourceAreaColumn("field")
                .withCellWillUnmount("function(info) { }");
        JsonObject json = col.toJson();
        Assertions.assertEquals(JsonType.OBJECT, json.get("cellWillUnmount").getType());
        Assertions.assertNotNull(((JsonObject) json.get("cellWillUnmount")).get("__jsCallback"));
    }

    @Test
    void testResourceAreaColumn_cellHooks_defaultAbsent() {
        ResourceAreaColumn col = new ResourceAreaColumn("field");
        JsonObject json = col.toJson();
        Assertions.assertFalse(json.hasKey("cellContent"));
        Assertions.assertFalse(json.hasKey("cellClassNames"));
        Assertions.assertFalse(json.hasKey("cellDidMount"));
        Assertions.assertFalse(json.hasKey("cellWillUnmount"));
    }

    // -------------------------------------------------------------------------
    // Scheduler option setter tests
    // -------------------------------------------------------------------------

    @Test
    void testSetResourceGroupField() {
        calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_GROUP_FIELD, "department");

        Optional<Object> option = calendar.getOption("resourceGroupField");
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals("department", option.get());
    }

    @Test
    void testSetDatesAboveResources() {
        calendar.setOption(FullCalendarScheduler.SchedulerOption.DATES_ABOVE_RESOURCES, true);

        Optional<Object> option = calendar.getOption("datesAboveResources");
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(true, option.get());
    }

    @Test
    void testSetDatesAboveResourcesFalse() {
        calendar.setOption(FullCalendarScheduler.SchedulerOption.DATES_ABOVE_RESOURCES, false);

        Optional<Object> option = calendar.getOption("datesAboveResources");
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(false, option.get());
    }

    @Test
    void testSetEntryMinWidth() {
        calendar.setOption(FullCalendarScheduler.SchedulerOption.ENTRY_MIN_WIDTH, 10);

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
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_GROUP_CLASS_NAMES, JsCallback.of("function(info) { return ['g']; }"))
        );
    }

    @Test
    void testSetResourceGroupContentCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_GROUP_CONTENT, JsCallback.of("function(info) { return info.groupValue; }"))
        );
    }

    @Test
    void testSetResourceGroupDidMountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_GROUP_DID_MOUNT, JsCallback.of("function(info) { }"))
        );
    }

    @Test
    void testSetResourceGroupWillUnmountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_GROUP_WILL_UNMOUNT, JsCallback.of("function(info) { }"))
        );
    }

    @Test
    void testSetResourceAreaHeaderClassNamesCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_AREA_HEADER_CLASS_NAMES, JsCallback.of("function(info) { return ['custom-header']; }"))
        );
    }

    @Test
    void testSetResourceAreaHeaderDidMountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_AREA_HEADER_DID_MOUNT, JsCallback.of("function(info) { }"))
        );
    }

    @Test
    void testSetResourceAreaHeaderWillUnmountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_AREA_HEADER_WILL_UNMOUNT, JsCallback.of("function(info) { }"))
        );
    }

    @Test
    void testSetResourceAddCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_ADD, JsCallback.of("function(info) { }"))
        );
    }

    @Test
    void testSetResourceChangeCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_CHANGE, JsCallback.of("function(info) { }"))
        );
    }

    @Test
    void testSetResourceRemoveCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_REMOVE, JsCallback.of("function(info) { }"))
        );
    }

    @Test
    void testSetResourcesSetCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCES_SET, JsCallback.of("function(info) { }"))
        );
    }

    // -------------------------------------------------------------------------
    // Scheduler callback smoke tests — RESOURCE_LABEL, RESOURCE_LANE, RESOURCE_GROUP_LANE
    // -------------------------------------------------------------------------

    @Test
    void testSetResourceLabelClassNamesCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_LABEL_CLASS_NAMES, JsCallback.of("function(arg) { return []; }")));
    }

    @Test
    void testSetResourceLabelContentCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_LABEL_CONTENT, JsCallback.of("function(arg) { return arg.resource.title; }")));
    }

    @Test
    void testSetResourceLabelDidMountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_LABEL_DID_MOUNT, JsCallback.of("function(arg) { }")));
    }

    @Test
    void testSetResourceLabelWillUnmountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_LABEL_WILL_UNMOUNT, JsCallback.of("function(arg) { }")));
    }

    @Test
    void testSetResourceLaneClassNamesCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_LANE_CLASS_NAMES, JsCallback.of("function(arg) { return []; }")));
    }

    @Test
    void testSetResourceLaneContentCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_LANE_CONTENT, JsCallback.of("function(arg) { }")));
    }

    @Test
    void testSetResourceLaneDidMountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_LANE_DID_MOUNT, JsCallback.of("function(arg) { }")));
    }

    @Test
    void testSetResourceLaneWillUnmountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_LANE_WILL_UNMOUNT, JsCallback.of("function(arg) { }")));
    }

    @Test
    void testSetResourceGroupLaneClassNamesCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_GROUP_LANE_CLASS_NAMES, JsCallback.of("function(arg) { return []; }")));
    }

    @Test
    void testSetResourceGroupLaneContentCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_GROUP_LANE_CONTENT, JsCallback.of("function(arg) { }")));
    }

    @Test
    void testSetResourceGroupLaneDidMountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_GROUP_LANE_DID_MOUNT, JsCallback.of("function(arg) { }")));
    }

    @Test
    void testSetResourceGroupLaneWillUnmountCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_GROUP_LANE_WILL_UNMOUNT, JsCallback.of("function(arg) { }")));
    }

    // -------------------------------------------------------------------------
    // Resource mutability (5.10)
    // -------------------------------------------------------------------------

    @Test
    void testResourceSetTitle() {
        Resource resource = new Resource("r1", "Original", null);
        resource.setTitle("Updated");

        Assertions.assertEquals("Updated", resource.getTitle(), "getTitle() returns updated value");

        JsonObject json = resource.toJson();
        Assertions.assertEquals("Updated", json.get("title").asString(), "toJson title is updated");
    }

    @Test
    void testResourceSetColor() {
        Resource resource = new Resource("r1", "Room", "blue");
        resource.setColor("#ff0000");

        Assertions.assertEquals("#ff0000", resource.getColor(), "getColor() returns updated value");

        JsonObject json = resource.toJson();
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
        resource.setEntryBackgroundColor("#aabbcc");

        Assertions.assertEquals("#aabbcc", resource.getEntryBackgroundColor());

        JsonObject json = resource.toJson();
        Assertions.assertTrue(json.hasKey("eventBackgroundColor"), "json has eventBackgroundColor");
        Assertions.assertEquals("#aabbcc", json.get("eventBackgroundColor").asString());
    }

    @Test
    void testResourceEventBackgroundColorNull_NotSerialized() {
        Resource resource = new Resource();
        // eventBackgroundColor is null by default

        JsonObject json = resource.toJson();
        Assertions.assertFalse(json.hasKey("eventBackgroundColor"), "null eventBackgroundColor not serialized");
    }

    @Test
    void testResourceEventBorderColor() {
        Resource resource = new Resource();
        resource.setEntryBorderColor("#001122");

        Assertions.assertEquals("#001122", resource.getEntryBorderColor());

        JsonObject json = resource.toJson();
        Assertions.assertTrue(json.hasKey("eventBorderColor"), "json has eventBorderColor");
        Assertions.assertEquals("#001122", json.get("eventBorderColor").asString());
    }

    @Test
    void testResourceEventTextColor() {
        Resource resource = new Resource();
        resource.setEntryTextColor("white");

        Assertions.assertEquals("white", resource.getEntryTextColor());

        JsonObject json = resource.toJson();
        Assertions.assertTrue(json.hasKey("eventTextColor"), "json has eventTextColor");
        Assertions.assertEquals("white", json.get("eventTextColor").asString());
    }

    @Test
    void testResourceEventConstraint() {
        Resource resource = new Resource();
        resource.setEntryConstraint("businessHours");

        Assertions.assertEquals("businessHours", resource.getEntryConstraint());

        JsonObject json = resource.toJson();
        Assertions.assertTrue(json.hasKey("eventConstraint"), "json has eventConstraint");
        Assertions.assertEquals("businessHours", json.get("eventConstraint").asString());
    }

    @Test
    void testResourceEventOverlapTrue() {
        Resource resource = new Resource();
        resource.setEntryOverlap(true);

        Assertions.assertEquals(Boolean.TRUE, resource.getEntryOverlap());

        JsonObject json = resource.toJson();
        Assertions.assertTrue(json.hasKey("eventOverlap"), "json has eventOverlap");
        Assertions.assertTrue(json.get("eventOverlap").asBoolean(), "eventOverlap is true");
    }

    @Test
    void testResourceEventOverlapFalse() {
        Resource resource = new Resource();
        resource.setEntryOverlap(false);

        Assertions.assertEquals(Boolean.FALSE, resource.getEntryOverlap());

        JsonObject json = resource.toJson();
        Assertions.assertTrue(json.hasKey("eventOverlap"), "json has eventOverlap (false is serialized)");
        Assertions.assertFalse(json.get("eventOverlap").asBoolean(), "eventOverlap is false");
    }

    @Test
    void testResourceEventOverlapNull_NotSerialized() {
        Resource resource = new Resource();
        // eventOverlap is null by default

        Assertions.assertNull(resource.getEntryOverlap());

        JsonObject json = resource.toJson();
        Assertions.assertFalse(json.hasKey("eventOverlap"), "null eventOverlap not serialized");
    }

    @Test
    void testResourceEventClassNames() {
        Resource resource = new Resource();
        Set<String> classNames = new LinkedHashSet<>();
        classNames.add("class-a");
        classNames.add("class-b");
        resource.setEntryClassNames(classNames);

        Set<String> returned = resource.getEntryClassNames();
        Assertions.assertNotNull(returned);
        Assertions.assertTrue(returned.contains("class-a"), "contains class-a");
        Assertions.assertTrue(returned.contains("class-b"), "contains class-b");
        Assertions.assertEquals(2, returned.size());

        JsonObject json = resource.toJson();
        Assertions.assertTrue(json.hasKey("eventClassNames"), "json has eventClassNames");
        JsonArray classNamesJson = (JsonArray) json.get("eventClassNames");
        Assertions.assertEquals(2, classNamesJson.length(), "json eventClassNames has 2 elements");
    }

    @Test
    void testResourceEventClassNamesNull_NotSerialized() {
        Resource resource = new Resource();
        resource.setEntryClassNames(null);

        Assertions.assertNull(resource.getEntryClassNames());

        JsonObject json = resource.toJson();
        Assertions.assertFalse(json.hasKey("eventClassNames"), "null eventClassNames not serialized");
    }

    @Test
    void testResourceEventClassNamesUnmodifiable() {
        Resource resource = new Resource();
        Set<String> classNames = new LinkedHashSet<>();
        classNames.add("readonly-class");
        resource.setEntryClassNames(classNames);

        Set<String> returned = resource.getEntryClassNames();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> returned.add("should-fail"),
                "returned set should be unmodifiable");
    }

}
