package eu.toolchain.serializer.processor;

import java.util.Optional;

import javax.lang.model.element.Element;

import lombok.Data;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

@Data
class SerializedFieldType {
    private final SerializedFieldTypeIdentifier identifier;
    private final Element typeElement;
    private final TypeName fieldType;
    private final FieldSpec fieldSpec;
    private final Optional<ParameterSpec> providedParameterSpec;
    private final Optional<Integer> id;
}