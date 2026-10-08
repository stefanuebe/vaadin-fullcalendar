package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    // getEntries()
    // -------------------------------------------------------------------------

    @Test
    void getEntries_withoutScheduler_returnsEmptySet() {
        Resource resource = new Resource();
        Assertions.assertTrue(resource.getEntries().isEmpty());
    }

    @Test
    void getEntries_returnsEntriesAssignedToResource() {
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

        Set<ResourceEntry> events = resource.getEntries();
        Assertions.assertEquals(1, events.size());
        Assertions.assertTrue(events.contains(assigned));
        Assertions.assertFalse(events.contains(other));
    }

    @Test
    void getEntries_multipleResources_returnsOnlyOwn() {
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

        Assertions.assertEquals(Set.of(e1), r1.getEntries());
        Assertions.assertEquals(Set.of(e2), r2.getEntries());
    }

    // -------------------------------------------------------------------------
    // setExtendedProp / removeExtendedProp
    // -------------------------------------------------------------------------

    @Test
    void setExtendedProp_storesValue() {
        Resource resource = new Resource();
        resource.setExtendedProp("dept", "Engineering");
        Assertions.assertEquals("Engineering", resource.getExtendedProps().get("dept"));
    }

    @Test
    void removeExtendedProp_removesValue() {
        Resource resource = new Resource();
        resource.setExtendedProp("dept", "Engineering");
        resource.removeExtendedProp("dept");
        Assertions.assertFalse(resource.getExtendedProps().containsKey("dept"));
    }

    @Test
    void setExtendedProps_replacesAll() {
        Resource resource = new Resource();
        resource.setExtendedProp("dept", "Engineering");

        Map<String, Object> props = new HashMap<>();
        props.put("floor", 3);
        resource.setExtendedProps(props);
        props.put("later", "ignored"); // the given map is copied
        Assertions.assertEquals(Map.of("floor", 3), resource.getExtendedProps());

        resource.setExtendedProps(null);
        Assertions.assertTrue(resource.getExtendedProps().isEmpty());
    }

    @Test
    void setExtendedProps_nullKey_throwsNPEAndKeepsProps() {
        Resource resource = new Resource();
        resource.setExtendedProp("dept", "Engineering");

        Map<String, Object> props = new HashMap<>();
        props.put(null, 1);
        Assertions.assertThrows(NullPointerException.class, () -> resource.setExtendedProps(props));
        Assertions.assertEquals(Map.of("dept", "Engineering"), resource.getExtendedProps());
    }

    @Test
    void hasExtendedProp_alsoForNullValue() {
        Resource resource = new Resource();
        Assertions.assertFalse(resource.hasExtendedProp("dept"));

        resource.setExtendedProp("dept", null);
        Assertions.assertTrue(resource.hasExtendedProp("dept"));

        resource.removeExtendedProp("dept");
        Assertions.assertFalse(resource.hasExtendedProp("dept"));
    }

    @Test
    void getExtendedProps_isReadOnly() {
        Resource resource = new Resource();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> resource.getExtendedProps().put("dept", "x"));
    }

    @Test
    void setExtendedProp_nullKey_throwsNPE() {
        Resource resource = new Resource();
        Assertions.assertThrows(NullPointerException.class, () -> resource.setExtendedProp(null, "x"));
    }

    @Test
    void removeExtendedProp_nullKey_throwsNPE() {
        Resource resource = new Resource();
        Assertions.assertThrows(NullPointerException.class, () -> resource.removeExtendedProp(null));
    }

    /**
     * FullCalendar spreads every non-standard key of the entry JSON into extendedProps, where it would overwrite an
     * extended prop of the same name. The keys ResourceEntry adds to Entry's must be ones FullCalendar parses itself.
     * Entry's own keys are checked in the core add-on's EntryTest.
     */
    @Test
    void resourceEntry_addsOnlyFullCalendarEventKeys() {
        Set<String> entryKeys = jsonNames(new Entry());
        Set<String> resourceEntryKeys = jsonNames(new ResourceEntry());
        resourceEntryKeys.removeAll(entryKeys);

        Assertions.assertEquals(Set.of("resourceIds", "resourceEditable"), resourceEntryKeys);
    }

    // Issue #269. An unset resourceEditable must not override the calendar-level eventResourceEditable

    @Test
    void resourceEditable_default_isTrueButNotInJson() {
        ResourceEntry entry = new ResourceEntry();
        Assertions.assertTrue(entry.isResourceEditable());
        Assertions.assertFalse(entry.toJson().has("resourceEditable"));
    }

    @Test
    void resourceEditable_explicitValues_areInJson() {
        ResourceEntry entry = new ResourceEntry();
        entry.setResourceEditable(false);
        Assertions.assertFalse(entry.isResourceEditable());
        Assertions.assertFalse(entry.toJson().get("resourceEditable").asBoolean());

        entry.setResourceEditable(true);
        Assertions.assertTrue(entry.toJson().get("resourceEditable").asBoolean());

        entry.setResourceEditable(null);
        Assertions.assertFalse(entry.toJson().has("resourceEditable"));
    }

    private static Set<String> jsonNames(Entry entry) {
        return entry.streamProperties()
                .filter(def -> !def.isJsonIgnored())
                .map(BeanProperties::getJsonName)
                .collect(Collectors.toSet());
    }

    @Test
    void setExtendedProp_serializesObjectValuesWithJackson() {
        Resource resource = new Resource();
        resource.setExtendedProp("room", new Room("Atlas", 3));
        Assertions.assertEquals("Atlas", resource.toJson().get("room").get("name").asString());
    }

    record Room(String name, int floor) {
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

    /**
     * Safety net for issue #230: the client-side {@code updateResource} fix relies on extended props
     * being serialized as top-level JSON keys (that matches the FullCalendar Resource constructor shape).
     * Without this invariant, the new {@code setExtendedProp} loop in
     * {@code full-calendar-scheduler.ts} would silently miss the props.
     */
    @Test
    void extendedProps_inJson_asTopLevelKeys() {
        Resource resource = new Resource("r1", "Room 1", null);
        resource.setExtendedProp("department", "Engineering");
        resource.setExtendedProp("capacity", 42);

        ObjectNode json = resource.toJson();

        Assertions.assertTrue(json.has("department"), "extended prop 'department' should be a top-level key");
        Assertions.assertEquals("Engineering", json.get("department").asString());
        Assertions.assertTrue(json.has("capacity"), "extended prop 'capacity' should be a top-level key");
        Assertions.assertEquals(42, json.get("capacity").asInt());
    }
}
