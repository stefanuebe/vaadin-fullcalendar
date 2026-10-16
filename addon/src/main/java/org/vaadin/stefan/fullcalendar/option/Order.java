/*
 * Copyright 2026, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar.option;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The order of items, for an option that takes an {@link OrderValue}: a list of sort keys, each a property of the
 * items (an entry or a resource, depending on the option) or a compare function. The library compares by the first key
 * and goes to the next on a tie.
 * <pre>{@code
 * Order.of("title", "-start");   // by title, then by start, latest first
 * Order.of(Order.ascending("title"), JsCallback.of("(a, b) => a.extendedProps.rank - b.extendedProps.rank"));
 * }</pre>
 *
 * @param keys the sort keys, at least one
 */
public record Order(List<Key> keys) implements OrderValue {

    /**
     * Creates the order. The list is copied, so a later change to the passed list does not change the order.
     *
     * @throws NullPointerException     when null is passed or contained
     * @throws IllegalArgumentException when no key is passed
     */
    public Order {
        keys = List.copyOf(Objects.requireNonNull(keys));
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("At least one sort key is needed");
        }
    }

    /**
     * Creates the order from property names, as the library writes them: a leading {@code -} sorts descending.
     *
     * @param properties the property names, such as {@code "title"} or {@code "-start"}, at least one
     * @return the order
     * @throws NullPointerException     when null is passed or contained
     * @throws IllegalArgumentException when no name is passed or a name is empty
     */
    public static Order of(String... properties) {
        return new Order(Arrays.stream(properties)
                .<Key>map(name -> name.startsWith("-") ? descending(name.substring(1)) : ascending(name))
                .toList());
    }

    /**
     * Creates the order from sort keys, which may mix properties and compare functions.
     *
     * @param keys the sort keys, at least one
     * @return the order
     * @throws NullPointerException     when null is passed or contained
     * @throws IllegalArgumentException when no key is passed
     */
    public static Order of(Key... keys) {
        return new Order(List.of(keys));
    }

    /**
     * Creates a sort key that sorts by a property, ascending.
     *
     * @param property the property name, such as {@code "title"}
     * @return the key
     * @throws NullPointerException     when null is passed
     * @throws IllegalArgumentException when the name is empty or starts with {@code -}
     */
    public static Property ascending(String property) {
        return new Property(property, false);
    }

    /**
     * Creates a sort key that sorts by a property, descending.
     *
     * @param property the property name, such as {@code "start"}
     * @return the key
     * @throws NullPointerException     when null is passed
     * @throws IllegalArgumentException when the name is empty or starts with {@code -}
     */
    public static Property descending(String property) {
        return new Property(property, true);
    }

    /**
     * A sort key: a property or a {@link JsCallback} that compares two items and returns a negative number, zero or a
     * positive number. A {@code switch} over it needs no {@code default} branch.
     */
    public sealed interface Key extends Serializable permits Property, JsCallback {
    }

    /**
     * A sort key that sorts by a property of the items.
     *
     * @param name       the property name, such as {@code "title"}
     * @param descending whether it sorts descending
     */
    public record Property(String name, boolean descending) implements Key {

        public Property {
            Objects.requireNonNull(name);
            if (name.isEmpty() || name.startsWith("-")) {
                throw new IllegalArgumentException("Not a property name: \"" + name + "\". Use Order.descending "
                        + "to sort descending.");
            }
        }
    }
}
