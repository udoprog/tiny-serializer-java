package eu.toolchain.serializer.processor;

import java.util.Optional;

import javax.lang.model.type.TypeMirror;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import lombok.Data;

@Data
class SerializedFieldType {
    private final SerializedFieldTypeIdentifier identifier;
    private final TypeMirror fieldTypeMirror;
    private final TypeName fieldType;
    private final FieldSpec fieldSpec;
    private final Optional<ParameterSpec> providedParameterSpec;
    private final Optional<Integer> id;
}