package eu.toolchain.serializer.processor;

import java.util.Optional;

import lombok.Data;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

@Data
class SerializedFieldType {
    private final SerializedFieldTypeIdentifier identifier;
    private final TypeName fieldType;
    private final FieldSpec fieldSpec;
    private final Optional<ParameterSpec> providedParameterSpec;
}