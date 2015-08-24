package eu.toolchain.serializer.processor.value;

import java.util.Optional;

import lombok.Data;

import com.squareup.javapoet.TypeName;

@Data
class ValueTypeIdentifier {
    private final TypeName fieldType;
    private final boolean provided;
    private final Optional<String> providerName;
}