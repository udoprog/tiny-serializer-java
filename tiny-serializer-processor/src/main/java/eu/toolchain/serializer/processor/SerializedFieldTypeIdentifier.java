package eu.toolchain.serializer.processor;

import java.util.Optional;

import lombok.Data;

import com.squareup.javapoet.TypeName;

@Data
class SerializedFieldTypeIdentifier {
    private final TypeName fieldType;
    private final boolean provided;
    private final Optional<String> providerName;
}