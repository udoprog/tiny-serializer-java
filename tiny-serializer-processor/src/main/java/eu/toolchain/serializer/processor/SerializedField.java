package eu.toolchain.serializer.processor;

import java.util.Optional;

import javax.lang.model.element.Element;

import lombok.Data;

@Data
class SerializedField {
    private final Element element;
    private final SerializedFieldType type;
    private final String fieldName;
    private final String accessor;
    private final String variableName;
    private final Optional<Integer> id;
    private final Optional<Integer> constructorOrder;
}