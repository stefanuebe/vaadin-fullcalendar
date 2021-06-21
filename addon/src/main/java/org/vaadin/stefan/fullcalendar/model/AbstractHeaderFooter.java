package org.vaadin.stefan.fullcalendar.model;

import elemental.json.Json;
import elemental.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract base class for FC header and footer
 */
@EqualsAndHashCode
@ToString
public class AbstractHeaderFooter {
    private final Map<HeaderFooterPartPosition, HeaderFooterPart> parts;

    /**
     * Creates a new instance.
     */
    protected AbstractHeaderFooter() {
        this.parts = new HashMap<>();
    }

    /**
     * Creates a new instance with the given parts.
     *
     * @param parts parts
     */
    protected AbstractHeaderFooter(@NotNull Collection<HeaderFooterPart> parts) {
        this.parts = Objects.requireNonNull(parts).stream().collect(Collectors.toMap(HeaderFooterPart::getPosition, Function.identity()));
    }

    /**
     * Returns all parts. Never null
     *
     * @return parts
     */
    public Set<HeaderFooterPart> getParts() {
        return new HashSet<>(parts.values());
    }

    /**
     * Convenience method to get the left part of this instance. Creates a new instance on the first call.
     * @return left part
     */
    public HeaderFooterPart getLeft() {
        return parts.computeIfAbsent(HeaderFooterPartPosition.LEFT, HeaderFooterPart::new);
    }

    /**
     * Convenience method to get the center part of this instance. Creates a new instance on the first call.
     * @return center part
     */
    public HeaderFooterPart getCenter() {
        return parts.computeIfAbsent(HeaderFooterPartPosition.CENTER, HeaderFooterPart::new);
    }

    /**
     * Convenience method to get the right part of this instance. Creates a new instance on the first call.
     * @return right part
     */
    public HeaderFooterPart getRight() {
        return parts.computeIfAbsent(HeaderFooterPartPosition.RIGHT, HeaderFooterPart::new);
    }



    /**
     * Converts the given object into a json object.
     *
     * @return json object
     */
    public JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();

        for (HeaderFooterPart position : parts.values())
            jsonObject.put(
                    position.getPosition().getCode(),
                    position.getItems()
                            .stream()
                            .map(HeaderFooterItem::getCode)
                            .collect(Collectors.joining(",")));

        return jsonObject;
    }

    /**
     * Registers the given part. Overrides any previous set definitions.
     * @param part part
     */
    public void addPart(@NotNull HeaderFooterPart part) {
        parts.put(part.getPosition(), part);
    }
}
