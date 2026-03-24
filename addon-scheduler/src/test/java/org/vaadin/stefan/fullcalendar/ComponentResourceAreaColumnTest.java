package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import elemental.json.JsonObject;

import java.util.*;

/**
 * Unit tests for {@link ComponentResourceAreaColumn}.
 */
public class ComponentResourceAreaColumnTest {

    private FullCalendarScheduler calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendarScheduler();
    }

    // ---- Constructor & Factory ----

    @Test
    void testNullCallbackThrowsNPE() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new ComponentResourceAreaColumn<>("field", "Header", null));
    }

    @Test
    void testNullFieldThrowsNPE() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new ComponentResourceAreaColumn<>(null, resource -> new Span()));
    }

    @Test
    void testCallbackInvocationAndMapManagement() {
        var col = new ComponentResourceAreaColumn<Span>("col", "Col",
                resource -> new Span(resource.getTitle()));

        calendar.setResourceAreaColumns(List.of(col));

        Resource res = new Resource(null, "Room A", null);
        calendar.addResource(res);

        Optional<Span> component = col.getComponent(res);
        Assertions.assertTrue(component.isPresent(), "component should exist");
        Assertions.assertEquals("Room A", component.get().getText());
    }

    @Test
    void testCallbackReturnsNullThrowsISE() {
        var col = new ComponentResourceAreaColumn<Span>("col", "Col", resource -> null);

        Assertions.assertThrows(IllegalStateException.class,
                () -> col.createComponent(new Resource(null, "Test", null)));
    }

    @Test
    void testCallbackReturnsAlreadyAttachedThrowsISE() {
        Span shared = new Span();
        // Simulate an attached component by adding it to a parent
        var parent = new com.vaadin.flow.component.html.Div();
        parent.add(shared);

        var col = new ComponentResourceAreaColumn<Span>("col", "Col", resource -> shared);
        col.bind(calendar);

        Assertions.assertThrows(IllegalStateException.class,
                () -> col.createComponent(new Resource(null, "Test", null)));
    }

    @Test
    void testCallbackThrowsExceptionResourceHasNoComponent() {
        var col = new ComponentResourceAreaColumn<Span>("col", "Col",
                resource -> { throw new RuntimeException("factory error"); });
        col.bind(calendar);

        Resource res = new Resource(null, "Test", null);
        // should not throw — exception is caught internally
        col.createComponent(res);

        Assertions.assertTrue(col.getComponent(res).isEmpty(),
                "resource should have no component when callback throws");
    }

    // ---- Bind / Unbind ----

    @Test
    void testDoubleBind_DifferentCalendar_ThrowsISE() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        var calendar2 = new FullCalendarScheduler();

        col.bind(calendar);
        Assertions.assertThrows(IllegalStateException.class, () -> col.bind(calendar2));
    }

    @Test
    void testDoubleBind_SameCalendar_OK() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        col.bind(calendar);
        Assertions.assertDoesNotThrow(() -> col.bind(calendar));
    }

    @Test
    void testUnbindAndReuse() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        var calendar2 = new FullCalendarScheduler();

        col.bind(calendar);
        col.unbind();
        Assertions.assertFalse(col.isBound());

        Assertions.assertDoesNotThrow(() -> col.bind(calendar2));
        Assertions.assertTrue(col.isBound());
    }

    // ---- getComponents ----

    @Test
    void testGetComponentsReturnsUnmodifiableMap() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        col.bind(calendar);

        Map<String, Span> components = col.getComponents();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> components.put("x", new Span()));
    }

    @Test
    void testGetComponentReturnsTypedOptional() {
        var col = new ComponentResourceAreaColumn<Span>("col", "Col",
                resource -> new Span("test"));
        col.bind(calendar);

        Resource res = new Resource(null, "Room", null);
        col.createComponent(res);

        Optional<Span> result = col.getComponent(res);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertInstanceOf(Span.class, result.get());
    }

    @Test
    void testGetComponentNullThrowsNPE() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        Assertions.assertThrows(NullPointerException.class, () -> col.getComponent(null));
    }

    // ---- Refresh ----

    @Test
    void testRefreshUnregisteredResourceIsNoop() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        col.bind(calendar);

        Resource unregistered = new Resource(null, "Ghost", null);
        // should not throw
        col.refresh(unregistered);
    }

    @Test
    void testRefreshNullThrowsNPE() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        Assertions.assertThrows(NullPointerException.class, () -> col.refresh(null));
    }

    @Test
    void testRefreshAllWithNoResourcesIsNoop() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        col.bind(calendar);
        // should not throw
        col.refreshAll();
    }

    // ---- Fluent methods ----

    @Test
    void testFluentMethodsReturnComponentResourceAreaColumn() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());

        Assertions.assertInstanceOf(ComponentResourceAreaColumn.class, col.withWidth("100px"));
        Assertions.assertInstanceOf(ComponentResourceAreaColumn.class, col.withGroup(true));
        Assertions.assertInstanceOf(ComponentResourceAreaColumn.class, col.withHeaderClassNames("cls"));
        Assertions.assertInstanceOf(ComponentResourceAreaColumn.class,
                col.withHeaderClassNames(JsCallback.of("function() { return []; }")));
        Assertions.assertInstanceOf(ComponentResourceAreaColumn.class,
                col.withHeaderDidMount("function() {}"));
        Assertions.assertInstanceOf(ComponentResourceAreaColumn.class,
                col.withHeaderDidMount(JsCallback.of("function() {}")));
        Assertions.assertInstanceOf(ComponentResourceAreaColumn.class,
                col.withHeaderWillUnmount("function() {}"));
        Assertions.assertInstanceOf(ComponentResourceAreaColumn.class,
                col.withHeaderWillUnmount(JsCallback.of("function() {}")));
        Assertions.assertInstanceOf(ComponentResourceAreaColumn.class, col.withCellClassNames("cls"));
        Assertions.assertInstanceOf(ComponentResourceAreaColumn.class,
                col.withCellClassNames(JsCallback.of("function() { return []; }")));
    }

    // ---- Blocked methods (UnsupportedOperationException) ----

    @Test
    void testWithCellContentStringThrows() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> col.withCellContent("text"));
    }

    @Test
    void testWithCellContentCallbackThrows() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> col.withCellContent(JsCallback.of("function() {}")));
    }

    @Test
    void testWithCellDidMountStringThrows() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> col.withCellDidMount("function() {}"));
    }

    @Test
    void testWithCellDidMountCallbackThrows() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> col.withCellDidMount(JsCallback.of("function() {}")));
    }

    @Test
    void testWithCellWillUnmountStringThrows() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> col.withCellWillUnmount("function() {}"));
    }

    @Test
    void testWithCellWillUnmountCallbackThrows() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> col.withCellWillUnmount(JsCallback.of("function() {}")));
    }

    // ---- toJson ----

    @Test
    void testToJsonContainsAutoGeneratedCallbacks() {
        var col = new ComponentResourceAreaColumn<Span>("deadline", "Deadline",
                resource -> new Span());

        JsonObject json = col.toJson();

        // field and headerContent from parent
        Assertions.assertEquals("deadline", json.get("field").asString());
        Assertions.assertEquals("Deadline", json.get("headerContent").asString());

        // auto-generated cellContent
        Assertions.assertTrue(json.hasKey("cellContent"), "should have cellContent");
        JsonObject cellContent = (JsonObject) json.get("cellContent");
        Assertions.assertTrue(cellContent.hasKey("__jsCallback"), "cellContent should be a JsCallback marker");
        Assertions.assertTrue(cellContent.get("__jsCallback").asString().contains("domNodes"));

        // auto-generated cellDidMount
        Assertions.assertTrue(json.hasKey("cellDidMount"), "should have cellDidMount");
        JsonObject cellDidMount = (JsonObject) json.get("cellDidMount");
        Assertions.assertTrue(cellDidMount.hasKey("__jsCallback"), "cellDidMount should be a JsCallback marker");
        String didMountJs = cellDidMount.get("__jsCallback").asString();
        Assertions.assertTrue(didMountJs.contains("vaadin-full-calendar-scheduler"),
                "should use correct element tag");
        Assertions.assertTrue(didMountJs.contains("data-rc-resource-id"),
                "should reference component data attribute");
        Assertions.assertTrue(didMountJs.contains("CSS.escape"),
                "should escape resource IDs");
        Assertions.assertTrue(didMountJs.contains("deadline"),
                "should contain the column key");

        // auto-generated cellWillUnmount
        Assertions.assertTrue(json.hasKey("cellWillUnmount"), "should have cellWillUnmount");
        JsonObject cellWillUnmount = (JsonObject) json.get("cellWillUnmount");
        Assertions.assertTrue(cellWillUnmount.hasKey("__jsCallback"), "cellWillUnmount should be a JsCallback marker");
        String willUnmountJs = cellWillUnmount.get("__jsCallback").asString();
        Assertions.assertTrue(willUnmountJs.contains("vaadin-full-calendar-scheduler"),
                "should use correct element tag");
    }

    @Test
    void testToJsonInheritsParentProperties() {
        var col = new ComponentResourceAreaColumn<Span>("col", "Header", resource -> new Span())
                .withWidth("200px")
                .withGroup(true)
                .withHeaderClassNames("my-class");

        JsonObject json = col.toJson();

        Assertions.assertEquals("200px", json.get("width").asString());
        Assertions.assertTrue(json.get("group").asBoolean());
        Assertions.assertEquals("my-class", json.get("headerClassNames").asString());
    }

    // ---- Scheduler integration ----

    @Test
    void testAddResourceTriggersComponentCreation() {
        var col = new ComponentResourceAreaColumn<Span>("col", "Col",
                resource -> new Span(resource.getTitle()));

        calendar.setResourceAreaColumns(List.of(
                new ResourceAreaColumn("title", "Name"),
                col
        ));

        Resource res = new Resource(null, "Room A", null);
        calendar.addResource(res);

        Assertions.assertTrue(col.getComponent(res).isPresent());
    }

    @Test
    void testRemoveResourceDestroysComponent() {
        var col = new ComponentResourceAreaColumn<Span>("col", "Col",
                resource -> new Span(resource.getTitle()));

        calendar.setResourceAreaColumns(List.of(col));

        Resource res = new Resource(null, "Room A", null);
        calendar.addResource(res);
        Assertions.assertTrue(col.getComponent(res).isPresent());

        calendar.removeResource(res);
        Assertions.assertTrue(col.getComponent(res).isEmpty());
    }

    @Test
    void testRemoveResourceWithChildrenDestroysRecursively() {
        var col = new ComponentResourceAreaColumn<Span>("col", "Col",
                resource -> new Span(resource.getTitle()));

        calendar.setResourceAreaColumns(List.of(col));

        Resource parent = new Resource(null, "Building", null);
        Resource child = new Resource(null, "Floor 1", null);
        parent.addChild(child);
        calendar.addResource(parent);

        Assertions.assertTrue(col.getComponent(parent).isPresent());
        Assertions.assertTrue(col.getComponent(child).isPresent());

        calendar.removeResource(parent);
        Assertions.assertTrue(col.getComponent(parent).isEmpty());
        Assertions.assertTrue(col.getComponent(child).isEmpty());
    }

    @Test
    void testRemoveAllResourcesDestroysAllComponents() {
        var col = new ComponentResourceAreaColumn<Span>("col", "Col",
                resource -> new Span(resource.getTitle()));

        calendar.setResourceAreaColumns(List.of(col));

        calendar.addResource(new Resource(null, "A", null));
        calendar.addResource(new Resource(null, "B", null));
        Assertions.assertEquals(2, col.getComponents().size());

        calendar.removeAllResources();
        Assertions.assertEquals(0, col.getComponents().size());
    }

    @Test
    void testSetResourceAreaColumnsReplacingCleansUp() {
        var col1 = new ComponentResourceAreaColumn<Span>("col1", resource -> new Span());
        calendar.setResourceAreaColumns(List.of(col1));

        calendar.addResource(new Resource(null, "A", null));
        Assertions.assertEquals(1, col1.getComponents().size());
        Assertions.assertTrue(col1.isBound());

        // Replace with different columns
        var col2 = new ComponentResourceAreaColumn<Span>("col2", resource -> new Span());
        calendar.setResourceAreaColumns(List.of(col2));

        Assertions.assertEquals(0, col1.getComponents().size(), "old column components should be destroyed");
        Assertions.assertFalse(col1.isBound(), "old column should be unbound");
        Assertions.assertTrue(col2.isBound(), "new column should be bound");
        Assertions.assertEquals(1, col2.getComponents().size(), "new column should have components for existing resources");
    }

    @Test
    void testSetResourceAreaColumnsEmptyListCleansUp() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        calendar.setResourceAreaColumns(List.of(col));
        calendar.addResource(new Resource(null, "A", null));

        calendar.setResourceAreaColumns(List.of());

        Assertions.assertEquals(0, col.getComponents().size());
        Assertions.assertFalse(col.isBound());
    }

    @Test
    void testDuplicateColumnFieldKeysThrowsIAE() {
        var col1 = new ResourceAreaColumn("samefield", "Header 1");
        var col2 = new ResourceAreaColumn("samefield", "Header 2");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> calendar.setResourceAreaColumns(List.of(col1, col2)));
    }

    @Test
    void testMixedColumnsProduceCorrectJson() {
        var regular = new ResourceAreaColumn("title", "Name").withWidth("200px");
        var component = new ComponentResourceAreaColumn<Span>("status", "Status",
                resource -> new Span());

        // Both should be serializable without error
        JsonObject regularJson = regular.toJson();
        JsonObject componentJson = component.toJson();

        Assertions.assertFalse(regularJson.hasKey("cellContent"), "regular column has no cellContent by default");
        Assertions.assertTrue(componentJson.hasKey("cellContent"), "component column has auto-generated cellContent");
        Assertions.assertTrue(componentJson.hasKey("cellDidMount"), "component column has auto-generated cellDidMount");
    }

    @Test
    void testUpdateResourceDoesNotReCreateComponent() {
        var col = new ComponentResourceAreaColumn<Span>("col", "Col",
                resource -> new Span(resource.getTitle()));

        calendar.setResourceAreaColumns(List.of(col));

        Resource res = new Resource(null, "Room A", null);
        calendar.addResource(res);

        Span original = col.getComponent(res).orElseThrow();

        res.setTitle("Room B");
        calendar.updateResource(res);

        Span afterUpdate = col.getComponent(res).orElseThrow();
        Assertions.assertSame(original, afterUpdate, "component instance should not change on updateResource");
    }

    @Test
    void testSetColumnsThenAddResourcesThenSetColumnsAgain() {
        var col = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        calendar.setResourceAreaColumns(List.of(col));

        calendar.addResource(new Resource(null, "A", null));
        calendar.addResource(new Resource(null, "B", null));
        Assertions.assertEquals(2, col.getComponents().size());

        // Re-set same column config — should re-create components
        var col2 = new ComponentResourceAreaColumn<Span>("col", resource -> new Span());
        calendar.setResourceAreaColumns(List.of(col2));

        Assertions.assertFalse(col.isBound());
        Assertions.assertEquals(0, col.getComponents().size());
        Assertions.assertTrue(col2.isBound());
        Assertions.assertEquals(2, col2.getComponents().size());
    }

    @Test
    void testChildResourceGetsComponent() {
        var col = new ComponentResourceAreaColumn<Span>("col", "Col",
                resource -> new Span(resource.getTitle()));

        calendar.setResourceAreaColumns(List.of(col));

        Resource parent = new Resource(null, "Building", null);
        Resource child = new Resource(null, "Floor 1", null);
        parent.addChild(child);
        calendar.addResource(parent);

        Assertions.assertTrue(col.getComponent(parent).isPresent());
        Assertions.assertTrue(col.getComponent(child).isPresent());
    }

    @Test
    void testCallbackReceivesCorrectResourceInstance() {
        Resource[] captured = new Resource[1];
        var col = new ComponentResourceAreaColumn<Span>("col", "Col", resource -> {
            captured[0] = resource;
            return new Span();
        });

        calendar.setResourceAreaColumns(List.of(col));

        Resource res = new Resource(null, "Room A", null);
        res.addExtendedProps("custom", "value");
        calendar.addResource(res);

        Assertions.assertSame(res, captured[0], "callback should receive the same Resource object");
    }
}
