package eu.toolchain.serializer.processor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.SerializerFramework;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AutoSerializeClassProcessor {
    static final Joiner parameterJoiner = Joiner.on(", ");

    final Types types;
    final Elements elements;
    final FrameworkStatements statements;
    final AutoSerializeUtils utils;

    public SerializedType process(final TypeElement element) throws ElementException {
        final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
        final String name = utils.serializedName(element);

        final Optional<SerializedTypeBuilder> builder = SerializedTypeBuilder.build(utils, element, packageName);
        final Set<ElementKind> kinds = getKinds(element);
        final SerializedFields serializedType = SerializedFields.build(utils, element, kinds);

        final ClassName elementType = (ClassName) TypeName.get(element.asType());
        final TypeName supertype = TypeName.get(utils.serializerFor(element.asType()));

        final TypeSpec.Builder generated = TypeSpec.classBuilder(statements.serializerName(element));

        for (final SerializedFieldType t : serializedType.getFieldTypes()) {
            generated.addField(t.getFieldSpec());
        }

        generated.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        generated.addSuperinterface(supertype);

        generated.addMethod(constructor(serializedType));
        generated.addMethod(serializeMethod(elementType, serializedType));
        generated.addMethod(deserializeMethod(elementType, serializedType, builder));

        return new SerializedType(element, packageName, name, generated.build(), elementType, supertype, serializedType);
    }


    /**
     * Get the set of supported element kinds that make up the total set of fields for this type.
     *
     * @param element
     * @return
     */
    Set<ElementKind> getKinds(TypeElement element) {
        final ImmutableSet.Builder<ElementKind> kinds = ImmutableSet.builder();

        if (element.getKind() == ElementKind.INTERFACE) {
            kinds.add(ElementKind.METHOD);
        }

        if (element.getKind() == ElementKind.CLASS) {
            kinds.add(ElementKind.FIELD);

            if (element.getModifiers().contains(Modifier.ABSTRACT)) {
                kinds.add(ElementKind.METHOD);
            }
        }

        return kinds.build();
    }

    MethodSpec constructor(final SerializedFields serialized) {
        final ParameterSpec framework = ParameterSpec.builder(SerializerFramework.class, "framework")
                .addModifiers(Modifier.FINAL).build();

        final MethodSpec.Builder b = MethodSpec.constructorBuilder();
        b.addModifiers(Modifier.PUBLIC);
        b.addParameter(framework);

        for (final SerializedFieldType t : serialized.getOrderedFieldTypes()) {
            if (t.getProvidedParameterSpec().isPresent()) {
                b.addParameter(t.getProvidedParameterSpec().get());
            }
        }

        for (final SerializedFieldType fieldType : serialized.getOrderedFieldTypes()) {
            if (fieldType.getProvidedParameterSpec().isPresent()) {
                b.addStatement("$N = $N", fieldType.getFieldSpec(), fieldType.getProvidedParameterSpec().get());
                continue;
            }

            final FrameworkMethodBuilder builder = new FrameworkMethodBuilder() {
                @Override
                public void assign(final String statement, final List<Object> arguments) {
                    b.addStatement(String.format("$N = %s", statement),
                            ImmutableList.builder().add(fieldType.getFieldSpec()).addAll(arguments).build().toArray());
                }
            };

            statements.resolveStatement(utils.boxedIfNeeded(fieldType.getTypeMirror()), framework).writeTo(
                    builder);
        }

        return b.build();
    }

    MethodSpec serializeMethod(final TypeName valueType, final SerializedFields serialized) {
        final ParameterSpec buffer = utils.parameter(TypeName.get(SerialWriter.class), "buffer");
        final ParameterSpec value = utils.parameter(valueType, "value");
        final MethodSpec.Builder b = utils.serializeMethod(buffer, value);

        for (final SerializedField field : serialized.getOrderedFields()) {
            b.addStatement("$N.serialize($N, $N.$L())", field.getType().getFieldSpec(), buffer, value,
                    field.getAccessor());
        }

        return b.build();
    }

    MethodSpec deserializeMethod(ClassName returnType, SerializedFields serializedType,
            Optional<SerializedTypeBuilder> typeBuilder) {
        final ParameterSpec buffer = utils.parameter(TypeName.get(SerialReader.class), "buffer");
        final MethodSpec.Builder b = utils.deserializeMethod(returnType, buffer);

        for (final SerializedField field : serializedType.getOrderedFields()) {
            final TypeName fieldType = field.getType().getTypeName();
            final FieldSpec fieldSpec = field.getType().getFieldSpec();
            b.addStatement("final $T $L = $N.deserialize($N)", fieldType, field.getVariableName(), fieldSpec, buffer);
        }

        if (typeBuilder.isPresent()) {
            typeBuilder.get().writeTo(returnType, b, serializedType.getFields());
        } else {
            b.addStatement("return new $T($L)", returnType,
                    parameterJoiner.join(serializedType.getConstructorVariables()));
        }

        return b.build();
    }
}