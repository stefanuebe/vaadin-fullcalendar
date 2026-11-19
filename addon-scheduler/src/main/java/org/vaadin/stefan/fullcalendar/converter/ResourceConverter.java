package org.vaadin.stefan.fullcalendar.converter;

import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import tools.jackson.databind.JsonNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Stefan Uebe
 */
public class ResourceConverter implements JsonItemPropertyConverter<Set<Resource>, ResourceEntry> {
    @Override
    public boolean supports(Object type) {
        return type instanceof Set && ((Set<?>) type).stream().allMatch(Resource.class::isInstance) ;
    }

    @Override
    public JsonNode toClientModel(Set<Resource> serverValue, ResourceEntry currentInstance) {
        List<String> ids = serverValue.stream()
                .map(Resource::getId)
                .collect(Collectors.toList());

        return JsonUtils.toJsonNode(ids);
    }
}
