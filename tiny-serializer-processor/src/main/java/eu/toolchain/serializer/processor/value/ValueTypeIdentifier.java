package eu.toolchain.serializer.processor.value;

import com.squareup.javapoet.TypeName;
import lombok.Data;

import java.util.Optional;

@Data
class ValueTypeIdentifier {
    private final TypeName fieldType;
    private final boolean provided;
    private final Optional<String> providerName;
}
