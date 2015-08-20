package eu.toolchain.serializer.processor;

import java.util.Optional;

import lombok.Data;

@Data
class SerializedField {
    private final SerializedFieldType fieldType;
    private final String fieldName;
    private final String accessor;
    private final String variableName;
    private final Optional<Integer> id;
    private final Optional<Integer> constructorOrder;
}