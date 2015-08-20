package eu.toolchain.serializer.processor;

import lombok.Data;

@Data
class SerializedField {
    private final SerializedFieldType fieldType;
    private final String fieldName;
    private final String accessor;
}