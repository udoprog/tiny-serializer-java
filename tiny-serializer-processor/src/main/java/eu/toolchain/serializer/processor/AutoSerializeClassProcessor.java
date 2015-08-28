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
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.unverified.Unverified;
import eu.toolchain.serializer.processor.value.Value;
import eu.toolchain.serializer.processor.value.ValueSet;
import eu.toolchain.serializer.processor.value.ValueType;
import eu.toolchain.serializer.processor.value.ValueTypeBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AutoSerializeClassProcessor {
    private static final Joiner parameterJoiner = Joiner.on(",");

    final Types types;
    final Elements elements;
    final FrameworkStatements statements;
    final AutoSerializeUtils utils;

    public Unverified<JavaFile> process(final TypeElement element, final AutoSerializeMirror autoSerialize) {
        final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
        final Set<ElementKind> kinds = getKinds(element);

        final Unverified<Optional<ValueTypeBuilder>> unverifiedBuilder = ValueTypeBuilder.build(utils, element, autoSerialize);
        final Unverified<ValueSet> unverifiedFields = ValueSet.build(utils, element, kinds, autoSerialize);

        final ClassName elementType = (ClassName) TypeName.get(element.asType());
        final TypeName supertype = TypeName.get(utils.serializerFor(element.asType()));
        final String serializerName = statements.serializerName(element);

        final Unverified<?> combineDifferent = Unverified.combineDifferent(unverifiedFields, unverifiedBuilder);

        return combineDifferent.map((o) -> {
            final ValueSet values = unverifiedFields.get();
            final Optional<ValueTypeBuilder> builder = unverifiedBuilder.get();

            final TypeSpec.Builder generated = TypeSpec.classBuilder(serializerName);

            for (final ValueType t : values.getTypes()) {
                generated.addField(t.getFieldSpec());
            }

            generated.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            generated.addSuperinterface(supertype);

            generated.addMethod(constructor(values));
            generated.addMethod(serializeMethod(elementType, values));
            generated.addMethod(deserializeMethod(elementType, values, builder));

            return JavaFile.builder(packageName, generated.build()).skipJavaLangImports(true).indent("    ").build();
        });
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

    MethodSpec constructor(final ValueSet values) {
        final ParameterSpec framework = ParameterSpec.builder(utils.serializerFramework(), "framework")
                .addModifiers(Modifier.FINAL).build();

        final MethodSpec.Builder b = MethodSpec.constructorBuilder();
        b.addModifiers(Modifier.PUBLIC);
        b.addParameter(framework);

        for (final ValueType t : values.getOrderedTypes()) {
            if (t.getProvidedParameterSpec().isPresent()) {
                b.addParameter(t.getProvidedParameterSpec().get());
            }
        }

        for (final ValueType fieldType : values.getOrderedTypes()) {
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

    MethodSpec serializeMethod(final TypeName valueType, final ValueSet serialized) {
        final ParameterSpec buffer = utils.parameter(utils.serialWriter(), "buffer");
        final ParameterSpec value = utils.parameter(valueType, "value");
        final MethodSpec.Builder b = utils.serializeMethod(buffer, value);

        for (final Value field : serialized.getOrderedValues()) {
            b.addStatement("$N.serialize($N, $N.$L())", field.getType().getFieldSpec(), buffer, value,
                    field.getAccessor());
        }

        return b.build();
    }

    MethodSpec deserializeMethod(ClassName returnType, ValueSet serializedType,
            Optional<ValueTypeBuilder> typeBuilder) {
        final ParameterSpec buffer = utils.parameter(utils.serialReader(), "buffer");
        final MethodSpec.Builder b = utils.deserializeMethod(returnType, buffer);

        for (final Value field : serializedType.getOrderedValues()) {
            final TypeName fieldType = field.getType().getTypeName();
            final FieldSpec fieldSpec = field.getType().getFieldSpec();
            b.addStatement("final $T $L = $N.deserialize($N)", fieldType, field.getVariableName(), fieldSpec, buffer);
        }

        if (typeBuilder.isPresent()) {
            typeBuilder.get().writeTo(returnType, b, serializedType.getValues());
        } else {
            b.addStatement("return new $T($L)", returnType,
                    parameterJoiner.join(serializedType.getConstructorVariables()));
        }

        return b.build();
    }
}