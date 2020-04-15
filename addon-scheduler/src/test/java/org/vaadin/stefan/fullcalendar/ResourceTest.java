package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    @BeforeAll
    static void beforeAll() {
        TestUtils.initVaadinService(FullCalendarSchedulerTest.COMPONENT_HTMLS);
    }

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

    @Test
    void testToJson() {
        Resource parent = new Resource(PARENT + DEFAULT_ID, PARENT + DEFAULT_TITLE, PARENT + DEFAULT_COLOR);

        JsonObject parentJson = parent.toJson();
        Assertions.assertTrue(parentJson.hasKey("id"), "json has id");
        Assertions.assertTrue(parentJson.hasKey("title"), "json has title");
        Assertions.assertTrue(parentJson.hasKey("eventColor"), "json has eventColor");
        Assertions.assertFalse(parentJson.hasKey("parentId"), "json has not parent");
        Assertions.assertFalse(parentJson.hasKey("children"), "json has no children");

        Assertions.assertEquals(PARENT + DEFAULT_ID, parentJson.getString("id"), "json id value");
        Assertions.assertEquals(PARENT + DEFAULT_TITLE, parentJson.getString("title"), "json title value");
        Assertions.assertEquals(PARENT + DEFAULT_COLOR, parentJson.getString("eventColor"), "json eventColor value");


        // check direct children

        Resource child1 = new Resource(CHILD1 + DEFAULT_ID, CHILD1 + DEFAULT_TITLE, CHILD1 + DEFAULT_COLOR);
        Resource child2 = new Resource(CHILD2 + DEFAULT_ID, CHILD2 + DEFAULT_TITLE, CHILD2 + DEFAULT_COLOR);
        Resource child11 = new Resource(CHILD1_1 + DEFAULT_ID, CHILD1_1 + DEFAULT_TITLE, CHILD1_1 + DEFAULT_COLOR);

        parent.addChildren(Arrays.asList(child1, child2));
        child1.addChildren(child11);

        parentJson = parent.toJson();
        Assertions.assertTrue(parentJson.hasKey("children"), "json has children");

        Assertions.assertTrue(parentJson.get("children") instanceof JsonArray, "json children is array");

        JsonArray parentChildrenJson = parentJson.getArray("children");
        Assertions.assertEquals(2, parentChildrenJson.length(), "parent children size");

        for (int i = 0; i < parentChildrenJson.length(); i++) {
            Assertions.assertTrue(parentChildrenJson.get(i) instanceof JsonObject, "child is JsonObject");

            JsonObject childJson = parentChildrenJson.get(i);

            Assertions.assertTrue(childJson.hasKey("id"), "child " + (i + 1) + "json has id");
            Assertions.assertTrue(childJson.hasKey("title"), "child " + (i + 1) + "json has title");
            Assertions.assertTrue(childJson.hasKey("eventColor"), "child " + (i + 1) + "json has eventColor");
            Assertions.assertTrue(childJson.hasKey("parentId"), "child " + (i + 1) + "json has parent");

            Assertions.assertEquals(CHILD + (i + 1) + DEFAULT_ID, childJson.getString("id"), "child " + (i + 1) + " id value");
            Assertions.assertEquals(CHILD + (i + 1) + DEFAULT_TITLE, childJson.getString("title"), "child " + (i + 1) + " title value");
            Assertions.assertEquals(CHILD + (i + 1) + DEFAULT_COLOR, childJson.getString("eventColor"), "child " + (i + 1) + " eventColor value");

            Assertions.assertEquals(PARENT + DEFAULT_ID, childJson.getString("parentId"), "child " + (i + 1) + " parent id value");

            // child 1 will be checked separately
            if (i != 0) { // I know, not a beautiful solution, but I don't care at this moment :D
                Assertions.assertFalse(childJson.hasKey("children"), "child " + (i + 1) + "json has no children");
            }
        }

        // Check child 1's children

        JsonObject child1Json = parentChildrenJson.get(0);
        Assertions.assertTrue(child1Json.hasKey("children"), "json child 1 has children");
        Assertions.assertTrue(child1Json.get("children") instanceof JsonArray, "json child 1 children is array");

        JsonArray child1ChildrenJson = child1Json.getArray("children");
        Assertions.assertEquals(1, child1ChildrenJson.length(), "json child 1 children size");

        Assertions.assertTrue(child1ChildrenJson.get(0) instanceof JsonObject, "json child 1_1 is JsonObject");

        JsonObject child11Json = child1ChildrenJson.get(0);

        Assertions.assertTrue(child11Json.hasKey("id"), "child 1_1 json has id");
        Assertions.assertTrue(child11Json.hasKey("title"), "child 1_1 json has title");
        Assertions.assertTrue(child11Json.hasKey("eventColor"), "child 1_1 json has eventColor");
        Assertions.assertTrue(child11Json.hasKey("parentId"), "child 1_1 json has parent");

        Assertions.assertEquals(CHILD1_1 + DEFAULT_ID, child11Json.getString("id"), "child 1_1  id value");
        Assertions.assertEquals(CHILD1_1 + DEFAULT_TITLE, child11Json.getString("title"), "child 1_1  title value");
        Assertions.assertEquals(CHILD1_1 + DEFAULT_COLOR, child11Json.getString("eventColor"), "child 1_1  eventColor value");

        Assertions.assertEquals(CHILD1 + DEFAULT_ID, child11Json.getString("parentId"), "child 1_1  parent id value");

        Assertions.assertFalse(child11Json.hasKey("children"), "child 1_1 json has no children");
    }
}
