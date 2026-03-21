package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ResourceTest {

    public static final String DEFAULT_STRING = "test";
    public static final String DEFAULT_ID = DEFAULT_STRING + 1;
    public static final String DEFAULT_TITLE = DEFAULT_STRING + 2;
    public static final String DEFAULT_COLOR = DEFAULT_STRING + 3;
    public static final String PARENT = "p";
    public static final String CHILD = "c";
    public static final String CHILD1 = CHILD + "1";
    public static final String CHILD2 = CHILD + "2";
    public static final String CHILD1_1 = CHILD + "1_1";

    static void assertEmptyDefaults(Resource resource) {
        try {
            //noinspection ResultOfMethodCallIgnored - @IntelliJ
            UUID.fromString(resource.getId());
        } catch (IllegalArgumentException e) {
            Assertions.fail("ID was not a legal UUID as expected", e);
        }

        Assertions.assertNull(resource.getTitle(), "title");
        Assertions.assertNull(resource.getColor(), "color");
        Assertions.assertEquals(0, resource.getChildren().size(), "children");
        Assertions.assertFalse(resource.getParent().isPresent(), "parent");
    }

    @Test
    void testEmptyDefaultsConstructors() {
        assertEmptyDefaults(new Resource());
        assertEmptyDefaults(new Resource(null, null, null));
        assertEmptyDefaults(new Resource(null, null, null, null));
    }

    @Test
    void testAddRemoveOneChildWithOneParent() {
        Resource parent = new Resource();

        Resource child = new Resource();

        parent.addChildren(child);

        Assertions.assertTrue(child.getParent().isPresent(), "Check if parent was set");
        Assertions.assertEquals(parent, child.getParent().get(), "Check if correct parent was set");

        Assertions.assertEquals(1, parent.getChildren().size(), "Check if child was added correctly");
        Assertions.assertEquals(child, parent.getChildren().iterator().next(), "Check if correct child was added");

        parent.removeChildren(child);
        Assertions.assertEquals(0, parent.getChildren().size(), "Check if child was removed correctly");
        Assertions.assertFalse(child.getParent().isPresent(), "Check if parent was removed correctly");
    }

    @Test
    void testAddRemoveMultipleChildrenWithOneParent() {
        Resource parent = new Resource();

        Resource child1 = new Resource();
        Resource child2 = new Resource();
        Resource child3 = new Resource();
        List<Resource> children = Arrays.asList(child1, child2, child3);

        parent.addChildren(children);

        for (int i = 0; i < children.size(); i++) {
            Resource child = children.get(i);
            Assertions.assertTrue(child.getParent().isPresent(), "Check if parent was set: child " + (i + 1));
            Assertions.assertEquals(parent, child.getParent().get(), "Check if correct parent was set child " + (i + 1));
        }

        Assertions.assertEquals(children.size(), parent.getChildren().size(), "Check if children have been added correctly");

        for (int i = 0; i < children.size(); i++) {
            Resource child = children.get(i);
            Assertions.assertTrue(parent.getChildren().contains(child), "Check if child " + (i + 1) + " was added");
        }

        // Remove one child
        parent.removeChildren(child2);
        Assertions.assertEquals(children.size() - 1, parent.getChildren().size(), "Check if child2 was removed correctly");
        Assertions.assertFalse(child2.getParent().isPresent(), "Check if parent was removed correctly");

        for (int i = 0; i < children.size(); i++) {
            Resource child = children.get(i);
            if (child == child2) {
                Assertions.assertFalse(parent.getChildren().contains(child), "Check if child " + (i + 1) + " does not exist");
            } else {
                Assertions.assertTrue(parent.getChildren().contains(child), "Check if child " + (i + 1) + " still exists");
            }
        }

        // remove all remaining children - not existing resources shoult not lead to an exception here
        parent.removeChildren(children);
        Assertions.assertEquals(0, parent.getChildren().size(), "Check if children have been removed correctly");

        for (int i = 0; i < children.size(); i++) {
            Resource child = children.get(i);
            Assertions.assertFalse(child.getParent().isPresent(), "Check if parent was removed correctly: child " + (i + 1));
        }
    }

    @Test
    void testTransferChildBetweenParents() {

        // test with singular API

        Resource parent1 = new Resource();
        Resource parent2 = new Resource();

        Resource child = new Resource();

        parent1.addChildren(child);
        parent2.addChildren(child);

        Assertions.assertTrue(child.getParent().isPresent(), "Check if parent was set");
        Assertions.assertEquals(parent2, child.getParent().get(), "Check if correct parent was set");

        Assertions.assertEquals(0, parent1.getChildren().size(), "Check if child was removed correctly");
        Assertions.assertEquals(1, parent2.getChildren().size(), "Check if child was added correctly");

        Assertions.assertEquals(child, parent2.getChildren().iterator().next(), "Check if correct child was added");

        // test with collection API

        parent1 = new Resource();
        parent2 = new Resource();

        child = new Resource();
        parent1.addChildren(Collections.singleton(child));
        parent2.addChildren(Collections.singleton(child));

        Assertions.assertTrue(child.getParent().isPresent(), "Check if parent was set");
        Assertions.assertEquals(parent2, child.getParent().get(), "Check if correct parent was set");

        Assertions.assertEquals(0, parent1.getChildren().size(), "Check if child was removed correctly");
        Assertions.assertEquals(1, parent2.getChildren().size(), "Check if child was added correctly");

        Assertions.assertEquals(child, parent2.getChildren().iterator().next(), "Check if correct child was added");
    }

    // -------------------------------------------------------------------------
    // getEvents()
    // -------------------------------------------------------------------------

    @Test
    void getEvents_withoutScheduler_returnsEmptySet() {
        Resource resource = new Resource();
        Assertions.assertTrue(resource.getEvents().isEmpty());
    }

    @Test
    void getEvents_returnsEntriesAssignedToResource() {
        FullCalendarScheduler scheduler = new FullCalendarScheduler();
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        scheduler.setEntryProvider(provider);

        Resource resource = new Resource();
        scheduler.addResource(resource);

        ResourceEntry assigned = new ResourceEntry();
        assigned.addResources(resource);
        provider.addEntry(assigned);

        ResourceEntry other = new ResourceEntry(); // no resource assigned
        provider.addEntry(other);

        Set<ResourceEntry> events = resource.getEvents();
        Assertions.assertEquals(1, events.size());
        Assertions.assertTrue(events.contains(assigned));
        Assertions.assertFalse(events.contains(other));
    }

    @Test
    void getEvents_multipleResources_returnsOnlyOwn() {
        FullCalendarScheduler scheduler = new FullCalendarScheduler();
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        scheduler.setEntryProvider(provider);

        Resource r1 = new Resource();
        Resource r2 = new Resource();
        scheduler.addResources(List.of(r1, r2));

        ResourceEntry e1 = new ResourceEntry();
        e1.addResources(r1);
        provider.addEntry(e1);

        ResourceEntry e2 = new ResourceEntry();
        e2.addResources(r2);
        provider.addEntry(e2);

        Assertions.assertEquals(Set.of(e1), r1.getEvents());
        Assertions.assertEquals(Set.of(e2), r2.getEvents());
    }

    // -------------------------------------------------------------------------
    // addExtendedProps / removeExtendedProps
    // -------------------------------------------------------------------------

    @Test
    void addExtendedProps_storesValue() {
        Resource resource = new Resource();
        resource.addExtendedProps("dept", "Engineering");
        Assertions.assertEquals("Engineering", resource.getExtendedProps().get("dept"));
    }

    @Test
    void removeExtendedProps_byKey_removesValue() {
        Resource resource = new Resource();
        resource.addExtendedProps("dept", "Engineering");
        resource.removeExtendedProps("dept");
        Assertions.assertFalse(resource.getExtendedProps().containsKey("dept"));
    }

    @Test
    void removeExtendedProps_byKeyAndValue_removesOnlyOnMatch() {
        Resource resource = new Resource();
        resource.addExtendedProps("dept", "Engineering");
        resource.removeExtendedProps("dept", "HR");           // wrong value — not removed
        Assertions.assertTrue(resource.getExtendedProps().containsKey("dept"));
        resource.removeExtendedProps("dept", "Engineering");  // correct value — removed
        Assertions.assertFalse(resource.getExtendedProps().containsKey("dept"));
    }

    @Test
    void testToJson() {
        Resource parent = new Resource(PARENT + DEFAULT_ID, PARENT + DEFAULT_TITLE, PARENT + DEFAULT_COLOR);

        ObjectNode parentJson = parent.toJson();
        Assertions.assertTrue(parentJson.has("id"), "json has id");
        Assertions.assertTrue(parentJson.has("title"), "json has title");
        Assertions.assertTrue(parentJson.has("eventColor"), "json has eventColor");
        Assertions.assertFalse(parentJson.has("parentId"), "json has not parent");
        Assertions.assertFalse(parentJson.has("children"), "json has no children");

        Assertions.assertEquals(PARENT + DEFAULT_ID, parentJson.get("id").asString(), "json id value");
        Assertions.assertEquals(PARENT + DEFAULT_TITLE, parentJson.get("title").asString(), "json title value");
        Assertions.assertEquals(PARENT + DEFAULT_COLOR, parentJson.get("eventColor").asString(), "json eventColor value");


        // check direct children

        Resource child1 = new Resource(CHILD1 + DEFAULT_ID, CHILD1 + DEFAULT_TITLE, CHILD1 + DEFAULT_COLOR);
        Resource child2 = new Resource(CHILD2 + DEFAULT_ID, CHILD2 + DEFAULT_TITLE, CHILD2 + DEFAULT_COLOR);
        Resource child11 = new Resource(CHILD1_1 + DEFAULT_ID, CHILD1_1 + DEFAULT_TITLE, CHILD1_1 + DEFAULT_COLOR);

        parent.addChildren(Arrays.asList(child1, child2));
        child1.addChildren(child11);

        parentJson = parent.toJson();
        Assertions.assertTrue(parentJson.has("children"), "json has children");

        Assertions.assertTrue(parentJson.get("children") instanceof ArrayNode, "json children is array");

        ArrayNode parentChildrenJson = (ArrayNode) parentJson.get("children");
        Assertions.assertEquals(2, parentChildrenJson.size(), "parent children size");

        for (int i = 0; i < parentChildrenJson.size(); i++) {
            Assertions.assertTrue(parentChildrenJson.get(i) instanceof ObjectNode, "child is ObjectNode");

            ObjectNode childJson = (ObjectNode) parentChildrenJson.get(i);

            Assertions.assertTrue(childJson.has("id"), "child " + (i + 1) + "json has id");
            Assertions.assertTrue(childJson.has("title"), "child " + (i + 1) + "json has title");
            Assertions.assertTrue(childJson.has("eventColor"), "child " + (i + 1) + "json has eventColor");
            Assertions.assertTrue(childJson.has("parentId"), "child " + (i + 1) + "json has parent");

            Assertions.assertEquals(CHILD + (i + 1) + DEFAULT_ID, childJson.get("id").asString(), "child " + (i + 1) + " id value");
            Assertions.assertEquals(CHILD + (i + 1) + DEFAULT_TITLE, childJson.get("title").asString(), "child " + (i + 1) + " title value");
            Assertions.assertEquals(CHILD + (i + 1) + DEFAULT_COLOR, childJson.get("eventColor").asString(), "child " + (i + 1) + " eventColor value");

            Assertions.assertEquals(PARENT + DEFAULT_ID, childJson.get("parentId").asString(), "child " + (i + 1) + " parent id value");

            // child 1 will be checked separately
            if (i != 0) { // I know, not a beautiful solution, but I don't care at this moment :D
                Assertions.assertFalse(childJson.has("children"), "child " + (i + 1) + "json has no children");
            }
        }

        // Check child 1's children

        ObjectNode child1Json = (ObjectNode) parentChildrenJson.get(0);
        Assertions.assertTrue(child1Json.has("children"), "json child 1 has children");
        Assertions.assertTrue(child1Json.get("children") instanceof ArrayNode, "json child 1 children is array");

        ArrayNode child1ChildrenJson = (ArrayNode) child1Json.get("children");
        Assertions.assertEquals(1, child1ChildrenJson.size(), "json child 1 children size");

        Assertions.assertTrue(child1ChildrenJson.get(0) instanceof ObjectNode, "json child 1_1 is ObjectNode");

        ObjectNode child11Json = (ObjectNode) child1ChildrenJson.get(0);

        Assertions.assertTrue(child11Json.has("id"), "child 1_1 json has id");
        Assertions.assertTrue(child11Json.has("title"), "child 1_1 json has title");
        Assertions.assertTrue(child11Json.has("eventColor"), "child 1_1 json has eventColor");
        Assertions.assertTrue(child11Json.has("parentId"), "child 1_1 json has parent");

        Assertions.assertEquals(CHILD1_1 + DEFAULT_ID, child11Json.get("id").asString(), "child 1_1  id value");
        Assertions.assertEquals(CHILD1_1 + DEFAULT_TITLE, child11Json.get("title").asString(), "child 1_1  title value");
        Assertions.assertEquals(CHILD1_1 + DEFAULT_COLOR, child11Json.get("eventColor").asString(), "child 1_1  eventColor value");

        Assertions.assertEquals(CHILD1 + DEFAULT_ID, child11Json.get("parentId").asString(), "child 1_1  parent id value");

        Assertions.assertFalse(child11Json.has("children"), "child 1_1 json has no children");
    }

    // -------------------------------------------------------------------------
    // eventAllow
    // -------------------------------------------------------------------------

    @Test
    void eventAllow_defaultNull() {
        Resource resource = new Resource();
        Assertions.assertNull(resource.getEntryAllow());
    }

    @Test
    void eventAllow_getterSetter() {
        Resource resource = new Resource();
        resource.setEntryAllow("function() { return true; }");
        Assertions.assertNotNull(resource.getEntryAllow());
        Assertions.assertEquals("function() { return true; }", resource.getEntryAllow().getJsFunction());
    }

    @Test
    void eventAllow_inJson_whenSet() {
        Resource resource = new Resource("r1", "Room 1", null);
        resource.setEntryAllow("function() { return false; }");
        ObjectNode json = resource.toJson();
        Assertions.assertTrue(json.has("eventAllow"), "eventAllow should be in JSON when set");
        Assertions.assertTrue(json.get("eventAllow").isObject(), "eventAllow should be a JsCallback marker object");
        Assertions.assertEquals("function() { return false; }", json.get("eventAllow").get("__jsCallback").asString());
    }

    @Test
    void eventAllow_notInJson_whenNull() {
        Resource resource = new Resource("r1", "Room 1", null);
        ObjectNode json = resource.toJson();
        Assertions.assertFalse(json.has("eventAllow"), "eventAllow should not be in JSON when null");
    }
}
