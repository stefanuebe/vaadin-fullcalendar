package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for scheduler/resource features:
 * 5.1 resourceColumns typed API
 * 5.2 resourceGroupField typed setter
 * 5.5 datesAboveResources typed setter
 * 5.7 eventMinWidth typed setter
 * 5.8 Resource lifecycle callbacks
 * 5.10 Resource property model improvements (mutable title/color)
 * 5.11 Per-resource event property overrides
 */
public class SchedulerFeaturesTest {

    private FullCalendarScheduler calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendarScheduler();
    }

    // -------------------------------------------------------------------------
    // ResourceColumn tests
    // -------------------------------------------------------------------------

    @Test
    void testResourceColumnMinimal() {
        ResourceColumn col = new ResourceColumn("title");
        ObjectNode json = col.toJson();

        Assertions.assertTrue(json.has("field"), "json has field");
        Assertions.assertEquals("title", json.get("field").asString(), "field value");
        Assertions.assertFalse(json.has("headerContent"), "no headerContent");
        Assertions.assertFalse(json.has("width"), "no width");
        Assertions.assertFalse(json.has("group"), "no group (false is omitted)");
        Assertions.assertFalse(json.has("headerClass"), "no headerClass");
        Assertions.assertFalse(json.has("headerDidMount"), "no headerDidMount");
        Assertions.assertFalse(json.has("headerWillUnmount"), "no headerWillUnmount");
    }

    @Test
    void testResourceColumnFull() {
        ResourceColumn col = new ResourceColumn("department", "Department")
                .withWidth("150px")
                .withGroup(true)
                .withHeaderClass("dept-header")
                .withHeaderDidMount("function(info) { console.log('mount'); }")
                .withHeaderWillUnmount("function(info) { console.log('unmount'); }");

        ObjectNode json = col.toJson();

        Assertions.assertEquals("department", json.get("field").asString(), "field");
        Assertions.assertEquals("Department", json.get("headerContent").asString(), "headerContent");
        Assertions.assertEquals("150px", json.get("width").asString(), "width");
        Assertions.assertTrue(json.get("group").asBoolean(), "group is true");
        Assertions.assertEquals("dept-header", json.get("headerClass").asString(), "headerClass");
        // headerDidMount and headerWillUnmount are now JsCallback markers
        Assertions.assertEquals("function(info) { console.log('mount'); }", json.get("headerDidMount").get("__jsCallback").asString(), "headerDidMount");
        Assertions.assertEquals("function(info) { console.log('unmount'); }", json.get("headerWillUnmount").get("__jsCallback").asString(), "headerWillUnmount");
    }

    @Test
    void testResourceColumnGroupTrue() {
        ResourceColumn col = new ResourceColumn("category").withGroup(true);
        ObjectNode json = col.toJson();

        Assertions.assertTrue(json.has("group"), "group key present when true");
        Assertions.assertTrue(json.get("group").asBoolean(), "group value is true");
    }

    @Test
    void testResourceColumnGroupFalse_NotSerialized() {
        ResourceColumn col = new ResourceColumn("category").withGroup(false);
        ObjectNode json = col.toJson();

        Assertions.assertFalse(json.has("group"), "group key absent when false (clean JSON)");
    }

    @Test
    void testResourceColumnRenderHooks() {
        String classNames = "h1 h2";
        String didMount = "function(info) { /* mount */ }";
        String willUnmount = "function(info) { /* unmount */ }";

        ResourceColumn col = new ResourceColumn("capacity")
                .withHeaderClass(classNames)
                .withHeaderDidMount(didMount)
                .withHeaderWillUnmount(willUnmount);

        // headerClass with String overload stays as String
        Assertions.assertEquals(classNames, col.getHeaderClass());
        // headerDidMount/willUnmount with String overload wraps in JsCallback
        Assertions.assertNotNull(col.getHeaderDidMount());
        Assertions.assertEquals(didMount, col.getHeaderDidMount().getJsFunction());
        Assertions.assertNotNull(col.getHeaderWillUnmount());
        Assertions.assertEquals(willUnmount, col.getHeaderWillUnmount().getJsFunction());

        ObjectNode json = col.toJson();
        Assertions.assertEquals(classNames, json.get("headerClass").asString());
        Assertions.assertEquals(didMount, json.get("headerDidMount").get("__jsCallback").asString());
        Assertions.assertEquals(willUnmount, json.get("headerWillUnmount").get("__jsCallback").asString());
    }

    // -------------------------------------------------------------------------
    // ResourceColumn cell-level render hooks
    // -------------------------------------------------------------------------

    @Test
    void testResourceColumn_cellContent_string() {
        ResourceColumn col = new ResourceColumn("field").withCellContent("static text");
        ObjectNode json = col.toJson();
        Assertions.assertEquals("static text", json.get("cellContent").asString());
    }

    @Test
    void testResourceColumn_cellContent_jsCallback() {
        ResourceColumn col = new ResourceColumn("field")
                .withCellContent(JsCallback.of("function(info) { return info.fieldValue; }"));
        ObjectNode json = col.toJson();
        Assertions.assertTrue(json.get("cellContent").isObject());
        Assertions.assertEquals("function(info) { return info.fieldValue; }",
                json.get("cellContent").get("__jsCallback").asString());
    }

    @Test
    void testResourceColumn_cellClass_string() {
        ResourceColumn col = new ResourceColumn("field").withCellClass("my-class");
        ObjectNode json = col.toJson();
        Assertions.assertEquals("my-class", json.get("cellClass").asString());
    }

    @Test
    void testResourceColumn_cellClass_jsCallback() {
        ResourceColumn col = new ResourceColumn("field")
                .withCellClass(JsCallback.of("function(info) { return 'a'; }"));
        ObjectNode json = col.toJson();
        Assertions.assertTrue(json.get("cellClass").isObject());
        Assertions.assertEquals("function(info) { return 'a'; }",
                json.get("cellClass").get("__jsCallback").asString());
    }

    @Test
    void testResourceColumn_cellDidMount() {
        ResourceColumn col = new ResourceColumn("field")
                .withCellDidMount("function(info) { }");
        ObjectNode json = col.toJson();
        Assertions.assertTrue(json.get("cellDidMount").isObject());
        Assertions.assertNotNull(json.get("cellDidMount").get("__jsCallback"));
    }

    @Test
    void testResourceColumn_cellWillUnmount() {
        ResourceColumn col = new ResourceColumn("field")
                .withCellWillUnmount("function(info) { }");
        ObjectNode json = col.toJson();
        Assertions.assertTrue(json.get("cellWillUnmount").isObject());
        Assertions.assertNotNull(json.get("cellWillUnmount").get("__jsCallback"));
    }

    @Test
    void testResourceColumn_cellHooks_defaultAbsent() {
        ResourceColumn col = new ResourceColumn("field");
        ObjectNode json = col.toJson();
        Assertions.assertFalse(json.has("cellContent"));
        Assertions.assertFalse(json.has("cellClass"));
        Assertions.assertFalse(json.has("cellDidMount"));
        Assertions.assertFalse(json.has("cellWillUnmount"));
    }

    // -------------------------------------------------------------------------
    // Scheduler option setter tests
    // -------------------------------------------------------------------------

    @Test
    void testSetResourceGroupField() {
        calendar.setOption(SchedulerOption.RESOURCE_GROUP_FIELD, "department");

        Optional<Object> option = calendar.getOption("resourceGroupField");
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals("department", option.get());
    }

    @Test
    void testSetDatesAboveResources() {
        calendar.setOption(SchedulerOption.DATES_ABOVE_RESOURCES, true);

        Optional<Object> option = calendar.getOption("datesAboveResources");
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(true, option.get());
    }

    @Test
    void testSetDatesAboveResourcesFalse() {
        calendar.setOption(SchedulerOption.DATES_ABOVE_RESOURCES, false);

        Optional<Object> option = calendar.getOption("datesAboveResources");
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(false, option.get());
    }

    @Test
    void testSetEntryMinWidth() {
        calendar.setOption(SchedulerOption.ENTRY_MIN_WIDTH, 10);

        Optional<Object> option = calendar.getOption("eventMinWidth");
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(10, option.get());
    }

    @Test
    @SuppressWarnings("removal")
    void deprecatedResourceAreaColumn_keepsItsFluentTypeAndOldClassSetters() {
        ResourceAreaColumn col = new ResourceAreaColumn("dept", "Dept")
                .withWidth("100px")
                .withHeaderClassNames("h")
                .withCellClassNames("c");

        ObjectNode json = col.toJson();
        Assertions.assertEquals("h", json.get("headerClass").asString());
        Assertions.assertEquals("c", json.get("cellClass").asString());
        Assertions.assertEquals("h", col.getHeaderClassNames());
    }

    @Test
    @SuppressWarnings("removal")
    void deprecatedComponentResourceAreaColumn_keepsItsFluentType() {
        ComponentResourceAreaColumn<Span> col = new ComponentResourceAreaColumn<Span>("status", resource -> new Span())
                .withWidth("80px")
                .withCellClassNames("c");

        Assertions.assertEquals("c", col.toJson().get("cellClass").asString());
    }

    @Test
    @SuppressWarnings("removal")
    void deprecatedSetResourceAreaColumns_setsTheResourceColumnsOption() {
        calendar.setResourceAreaColumns(new ResourceAreaColumn("title", "Title"));

        Assertions.assertTrue(calendar.getOption(SchedulerOption.RESOURCE_COLUMNS).isPresent());
    }

    @Test
    void testSetResourceColumns() {
        List<ResourceColumn> columns = List.of(
                new ResourceColumn("title", "Resource").withWidth("200px"),
                new ResourceColumn("department", "Department").withWidth("150px").withGroup(true)
        );

        calendar.setResourceColumns(columns);

        Optional<Object> option = calendar.getOption("resourceColumns");
        Assertions.assertTrue(option.isPresent(), "resourceColumns option is set");
        // The server-side value stored is the original List
        Assertions.assertSame(columns, option.get(), "server-side value is the original list");
    }

    @Test
    void testSetResourceColumnsVarargs() {
        ResourceColumn col1 = new ResourceColumn("title", "Title");
        ResourceColumn col2 = new ResourceColumn("eventColor", "Color");

        calendar.setResourceColumns(col1, col2);

        Optional<Object> option = calendar.getOption("resourceColumns");
        Assertions.assertTrue(option.isPresent(), "resourceColumns option is set via varargs");
    }

    // -------------------------------------------------------------------------
    // Resource lifecycle callbacks: setting them does not throw. Their keys are checked in
    // SchedulerOptionsTest, the client side needs a browser.
    // -------------------------------------------------------------------------

    @Test
    void testSetResourceAddCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(SchedulerOption.RESOURCE_ADD, JsCallback.of("function(info) { }"))
        );
    }

    @Test
    void testSetResourceChangeCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(SchedulerOption.RESOURCE_CHANGE, JsCallback.of("function(info) { }"))
        );
    }

    @Test
    void testSetResourceRemoveCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(SchedulerOption.RESOURCE_REMOVE, JsCallback.of("function(info) { }"))
        );
    }

    @Test
    void testSetResourcesSetCallback() {
        Assertions.assertDoesNotThrow(() ->
                calendar.setOption(SchedulerOption.RESOURCES_SET, JsCallback.of("function(info) { }"))
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
    void testResourceEventContrastColor() {
        Resource resource = new Resource();
        resource.setEntryContrastColor("white");

        Assertions.assertEquals("white", resource.getEntryContrastColor());

        ObjectNode json = resource.toJson();
        Assertions.assertEquals("white", json.get("eventContrastColor").asString());
        Assertions.assertFalse(json.has("eventTextColor"), "FullCalendar 7 ignores eventTextColor");
    }

    @Test
    void testResourceEventContrastColorNull_NotSerialized() {
        Assertions.assertFalse(new Resource().toJson().has("eventContrastColor"));
    }

    @Test
    @SuppressWarnings("removal")
    void testResourceEntryTextColor_isAliasOfContrastColor() {
        Resource resource = new Resource();
        resource.setEntryTextColor("white");

        Assertions.assertEquals("white", resource.getEntryContrastColor());
        Assertions.assertEquals("white", resource.getEntryTextColor());
    }

    @Test
    void testResourceEventConstraint() {
        Resource resource = new Resource();
        resource.setEntryConstraint("businessHours");

        Assertions.assertEquals("businessHours", resource.getEntryConstraint());

        ObjectNode json = resource.toJson();
        Assertions.assertTrue(json.has("eventConstraint"), "json has eventConstraint");
        Assertions.assertEquals("businessHours", json.get("eventConstraint").asString());
    }

    @Test
    void testResourceEventOverlapTrue() {
        Resource resource = new Resource();
        resource.setEntryOverlap(true);

        Assertions.assertEquals(Boolean.TRUE, resource.getEntryOverlap());

        ObjectNode json = resource.toJson();
        Assertions.assertTrue(json.has("eventOverlap"), "json has eventOverlap");
        Assertions.assertTrue(json.get("eventOverlap").asBoolean(), "eventOverlap is true");
    }

    @Test
    void testResourceEventOverlapFalse() {
        Resource resource = new Resource();
        resource.setEntryOverlap(false);

        Assertions.assertEquals(Boolean.FALSE, resource.getEntryOverlap());

        ObjectNode json = resource.toJson();
        Assertions.assertTrue(json.has("eventOverlap"), "json has eventOverlap (false is serialized)");
        Assertions.assertFalse(json.get("eventOverlap").asBoolean(), "eventOverlap is false");
    }

    @Test
    void testResourceEventOverlapNull_NotSerialized() {
        Resource resource = new Resource();
        // eventOverlap is null by default

        Assertions.assertNull(resource.getEntryOverlap());

        ObjectNode json = resource.toJson();
        Assertions.assertFalse(json.has("eventOverlap"), "null eventOverlap not serialized");
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

        ObjectNode json = resource.toJson();
        Assertions.assertEquals("class-a class-b", json.get("eventClass").asString(),
                "FullCalendar 7 takes eventClass as one space-separated string");
        Assertions.assertFalse(json.has("eventClassNames"));
    }

    @Test
    void testResourceEventClassNamesNull_NotSerialized() {
        Resource resource = new Resource();
        resource.setEntryClassNames(null);

        Assertions.assertNull(resource.getEntryClassNames());

        ObjectNode json = resource.toJson();
        Assertions.assertFalse(json.has("eventClass"), "null class names not serialized");
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
