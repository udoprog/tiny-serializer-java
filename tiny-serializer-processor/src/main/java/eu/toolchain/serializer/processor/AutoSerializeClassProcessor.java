package eu.toolchain.serializer.processor;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import lombok.RequiredArgsConstructor;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.DefaultBuilderType;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.SerializerFramework;

@RequiredArgsConstructor
public class AutoSerializeClassProcessor {
    static final Joiner parameterJoiner = Joiner.on(", ");
    static final Joiner emptyJoiner = Joiner.on("");
    static final ClassName defaultBuilder = ClassName.get(DefaultBuilderType.class);

    final Types types;
    final Elements elements;
    final FrameworkStatements statements;
    final AutoSerializeUtils utils;

    SerializedType process(final Element element) {
        final AutoSerialize autoSerialize = element.getAnnotation(AutoSerialize.class);
        final Optional<AutoSerialize.Builder> elementBuilder = Optional.fromNullable(element
                .getAnnotation(AutoSerialize.Builder.class));

        final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
        final String name = utils.serializedName(element, autoSerialize);

        final SerializedTypeFields serializedType = SerializedTypeFields.build(utils, element, autoSerialize);

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
        generated.addMethod(deserializeMethod(elementType, serializedType, autoSerialize, elementBuilder));

        return new SerializedType(element, packageName, name, generated.build(), elementType, supertype, serializedType);
    }

    MethodSpec constructor(final SerializedTypeFields serialized) {
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

            statements.resolveStatement(utils.boxedIfNeeded(fieldType.getTypeElement().asType()), framework).writeTo(
                    builder);
        }

        return b.build();
    }

    MethodSpec serializeMethod(final TypeName valueType, final SerializedTypeFields serialized) {
        final ParameterSpec buffer = utils.parameter(TypeName.get(SerialWriter.class), "buffer");
        final ParameterSpec value = utils.parameter(valueType, "value");
        final MethodSpec.Builder b = utils.serializeMethod(buffer, value);

        for (final SerializedField field : serialized.getOrderedFields()) {
            b.addStatement("$N.serialize($N, $N.$L())", field.getFieldType().getFieldSpec(), buffer, value,
                    field.getAccessor());
        }

        return b.build();
    }

    MethodSpec deserializeMethod(ClassName returnType, SerializedTypeFields serializedType,
            AutoSerialize autoSerialize,
            Optional<AutoSerialize.Builder> elementBuilder) {
        final ParameterSpec buffer = utils.parameter(TypeName.get(SerialReader.class), "buffer");
        final MethodSpec.Builder b = utils.deserializeMethod(returnType, buffer);

        for (final SerializedField field : serializedType.getOrderedFields()) {
            final TypeName fieldType = field.getFieldType().getFieldType();
            final FieldSpec fieldSpec = field.getFieldType().getFieldSpec();
            b.addStatement("final $T $L = $N.deserialize($N)", fieldType, field.getVariableName(), fieldSpec, buffer);
        }

        final Optional<AutoSerialize.Builder> builder = getSpecifiedBuilder(autoSerialize.builder(), elementBuilder);

        if (builder.isPresent()) {
            deserializeUsingBuilder(returnType, b, serializedType.getFields(), builder.get());
        } else {
            b.addStatement("return new $T($L)", returnType,
                    parameterJoiner.join(serializedType.getConstructorVariables()));
        }

        return b.build();
    }

    private Optional<AutoSerialize.Builder> getSpecifiedBuilder(AutoSerialize.Builder[] builder,
            Optional<AutoSerialize.Builder> elementBuilder) {
        if (builder.length > 0) {
            return Optional.of(builder[0]);
        }

        return elementBuilder;
    }

    void deserializeUsingBuilder(ClassName returnType, Builder b, List<SerializedField> variables,
            AutoSerialize.Builder builder) {
        final ImmutableList.Builder<String> builders = ImmutableList.builder();
        final ImmutableList.Builder<Object> parameters = ImmutableList.builder();

        final TypeName builderType;
        final String builderStatement;

        if (builder.useConstructor()) {
            builderType = getBuilderTypeForConstructor(returnType, builder);
            builderStatement = "new $T()";
        } else {
            builderType = getBuilderTypeForMethod(returnType, builder);
            builderStatement = String.format("$T.%s()", builder.useMethod());
        }

        parameters.add(builderType);

        for (final SerializedField f : variables) {
            final String setter = builderSetter(f, builder);
            builders.add(String.format(".%s($L)", setter));
            parameters.add(f.getVariableName());
        }

        b.addStatement(String.format("return %s%s.build()", builderStatement, emptyJoiner.join(builders.build())),
                parameters.build().toArray());
    }

    TypeName getBuilderTypeForConstructor(ClassName returnType, AutoSerialize.Builder builder) {
        final ClassName builderType = utils.pullMirroredClass(builder::type);

        if (!builderType.equals(defaultBuilder)) {
            return builderType;
        }

        return returnType.nestedClass("Builder");
    }

    TypeName getBuilderTypeForMethod(ClassName returnType, AutoSerialize.Builder builder) {
        final ClassName builderType = utils.pullMirroredClass(builder::type);

        if (!builderType.equals(defaultBuilder)) {
            return builderType;
        }

        return returnType;
    }

    String builderSetter(final SerializedField f, AutoSerialize.Builder builder) {
        if (builder.useSetter()) {
            return "set" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, f.getFieldName());
        }

        return f.getFieldName();
    }
}