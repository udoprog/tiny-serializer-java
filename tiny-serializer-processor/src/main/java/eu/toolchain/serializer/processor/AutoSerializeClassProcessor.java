package eu.toolchain.serializer.processor;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.field.Field;
import eu.toolchain.serializer.processor.field.FieldSet;
import eu.toolchain.serializer.processor.field.FieldSetBuilder;
import eu.toolchain.serializer.processor.field.FieldType;
import eu.toolchain.serializer.processor.field.FieldTypeBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AutoSerializeClassProcessor {
  private static final Joiner parameterJoiner = Joiner.on(",");

  final Types types;
  final Elements elements;
  final FrameworkStatements statements;
  final AutoSerializeUtils utils;

  public JavaFile process(
    final TypeElement element, final AutoSerializeMirror autoSerialize
  ) {
    final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
    final Set<ElementKind> kinds = getKinds(element);

    final FieldSetBuilder fieldSetBuilder =
      new FieldSetBuilder(utils, element, kinds, autoSerialize.isUseGetter());

    for (final Element child : element.getEnclosedElements()) {
      fieldSetBuilder.add(child);
    }

    final FieldSet values =
      fieldSetBuilder.build(autoSerialize.isOrderById(), autoSerialize.isOrderConstructorById());

    final ClassName elementType = (ClassName) TypeName.get(element.asType());
    final TypeName supertype = TypeName.get(utils.serializerFor(element.asType()));
    final String serializerName = utils.serializerName(element);

    final boolean fieldBased = autoSerialize.isFieldBased();
    final boolean failOnMissing = autoSerialize.isFailOnMissing();

    final TypeElement stringType = elements.getTypeElement(String.class.getCanonicalName());
    final TypeElement integerType = elements.getTypeElement(Integer.class.getCanonicalName());

    final TypeSpec.Builder generated = TypeSpec.classBuilder(serializerName);

    final AnnotationSpec generatedAnnotation = AnnotationSpec
      .builder(Generated.class)
      .addMember("value", "$S", AutoSerializeProcessor.class.getCanonicalName())
      .build();

    generated.addAnnotation(generatedAnnotation);

    final FieldSpec count = FieldSpec
      .builder(TypeName.get(utils.serializerFor(integerType.asType())), "count", Modifier.FINAL)
      .build();

    final FieldSpec name = FieldSpec
      .builder(TypeName.get(utils.serializerFor(stringType.asType())), "name", Modifier.FINAL)
      .build();

    if (fieldBased) {
      generated.addField(count);
      generated.addField(name);
    }

    for (final FieldType t : values.getTypes()) {
      generated.addField(t.getFieldSpec());
    }

    generated.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    generated.addSuperinterface(supertype);

    final Optional<FieldTypeBuilder> fieldTypeBuilder =
      utils.builder(element).map(Optional::of).orElseGet(autoSerialize::getBuilder).map(method -> {
        return new FieldTypeBuilder(method, method.shouldUseConstructor(), method.isUseSetter(),
          method.getMethodName());
      });

    if (fieldBased) {
      generated.addMethod(fieldConstructor(values, count, name));
      generated.addMethod(fieldSerializeMethod(elementType, values, count, name));
      generated.addMethod(
        fieldDeserializeMethod(elementType, values, fieldTypeBuilder, count, name, failOnMissing));
    } else {
      generated.addMethod(serialConstructor(values));
      generated.addMethod(serialSerializeMethod(elementType, values));
      generated.addMethod(serialDeserializeMethod(elementType, values, fieldTypeBuilder));
    }

    return JavaFile
      .builder(packageName, generated.build())
      .skipJavaLangImports(true)
      .indent("    ")
      .build();
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

  MethodSpec serialConstructor(final FieldSet values) {
    final ParameterSpec framework = ParameterSpec
      .builder(utils.serializerFramework(), "framework")
      .addModifiers(Modifier.FINAL)
      .build();

    final MethodSpec.Builder b = MethodSpec.constructorBuilder();
    b.addModifiers(Modifier.PUBLIC);
    b.addParameter(framework);

    for (final FieldType t : values.getOrderedTypes()) {
      if (t.getProvidedParameterSpec().isPresent()) {
        b.addParameter(t.getProvidedParameterSpec().get());
      }
    }

    for (final FieldType fieldType : values.getOrderedTypes()) {
      if (fieldType.getProvidedParameterSpec().isPresent()) {
        b.addStatement("$N = $N", fieldType.getFieldSpec(),
          fieldType.getProvidedParameterSpec().get());
        continue;
      }

      final FrameworkMethodBuilder builder = new FrameworkMethodBuilder() {
        @Override
        public void assign(final String statement, final List<Object> arguments) {
          b.addStatement(String.format("$N = %s", statement), ImmutableList
            .builder()
            .add(fieldType.getFieldSpec())
            .addAll(arguments)
            .build()
            .toArray());
        }
      };

      statements
        .resolveStatement(utils.boxedIfNeeded(fieldType.getTypeMirror()), framework)
        .writeTo(builder);
    }

    return b.build();
  }

  MethodSpec fieldConstructor(
    final FieldSet values, final FieldSpec count, final FieldSpec name
  ) {
    final ParameterSpec framework = ParameterSpec
      .builder(utils.serializerFramework(), "framework")
      .addModifiers(Modifier.FINAL)
      .build();

    final MethodSpec.Builder b = MethodSpec.constructorBuilder();
    b.addModifiers(Modifier.PUBLIC);
    b.addParameter(framework);

    b.addStatement("$N = $N.variableInteger()", count, framework);
    b.addStatement("$N = $N.string()", name, framework);

    for (final FieldType t : values.getOrderedTypes()) {
      if (t.getProvidedParameterSpec().isPresent()) {
        b.addParameter(t.getProvidedParameterSpec().get());
      }
    }

    for (final FieldType fieldType : values.getOrderedTypes()) {
      if (fieldType.getProvidedParameterSpec().isPresent()) {
        b.addStatement("$N = $N", fieldType.getFieldSpec(),
          fieldType.getProvidedParameterSpec().get());
        continue;
      }

      final FrameworkMethodBuilder builder = new FrameworkMethodBuilder() {
        @Override
        public void assign(final String statement, final List<Object> arguments) {
          b.addStatement(String.format("$N = %s", statement), ImmutableList
            .builder()
            .add(fieldType.getFieldSpec())
            .addAll(arguments)
            .build()
            .toArray());
        }
      };

      statements
        .resolveStatement(utils.boxedIfNeeded(fieldType.getTypeMirror()), framework)
        .writeTo(builder);
    }

    return b.build();
  }

  MethodSpec fieldSerializeMethod(
    final TypeName valueType, final FieldSet serialized, final FieldSpec count, final FieldSpec name
  ) {
    final ParameterSpec buffer = utils.parameter(utils.serialWriter(), "buffer");
    final ParameterSpec value = utils.parameter(valueType, "value");
    final MethodSpec.Builder b = utils.serializeMethod(buffer, value);

    b.addStatement("$N.serialize($N, $L)", count, buffer, serialized.getFields().size());

    for (final Field field : serialized.getOrderedValues()) {
      if (field.getType().isOptional()) {
        b.addStatement("final $T $N = $N.$L()", field.getType().getTypeName(),
          field.getVariableName(), value, field.getAccessor());
        b.beginControlFlow("if ($N.isPresent())", field.getVariableName());

        b.addStatement("$N.serialize($N, $S)", name, buffer, field.getName());

        b.beginControlFlow("try (final $T w = $N.scope())", utils.serialWriter(), buffer);
        b.addStatement("$N.serialize(w, $N)", field.getType().getFieldSpec(),
          field.getVariableName());
        b.endControlFlow();

        b.endControlFlow();
      } else {
        b.addStatement("$N.serialize($N, $S)", name, buffer, field.getName());

        b.beginControlFlow("try (final $T w = $N.scope())", utils.serialWriter(), buffer);
        b.addStatement("$N.serialize(w, $N.$L())", field.getType().getFieldSpec(), value,
          field.getAccessor());
        b.endControlFlow();
      }
    }

    return b.build();
  }

  MethodSpec fieldDeserializeMethod(
    final ClassName returnType, final FieldSet serializedType,
    final Optional<FieldTypeBuilder> typeBuilder, final FieldSpec count, final FieldSpec name,
    final boolean failOnMissing
  ) {
    final ParameterSpec buffer = utils.parameter(utils.serialReader(), "buffer");
    final MethodSpec.Builder b = utils.deserializeMethod(returnType, buffer);

    for (final Field field : serializedType.getOrderedValues()) {
      final TypeName fieldType = field.getType().getTypeName();

      if (field.getType().isOptional()) {
        b.addStatement("$T $L = $T.empty()", fieldType, field.getVariableName(), utils.optional());
      } else {
        b.addStatement("$T $L = $L", fieldType, field.getVariableName(),
          utils.initLiteral(field.getType().getTypeMirror()));
        b.addStatement("boolean $L = false", field.getIsSetVariableName());
      }
    }

    b.addStatement("final int total = $N.deserialize($N)", count, buffer);

    b.addStatement("int i = 0");

    b.beginControlFlow("while (i++ < total)");

    b.addStatement("final String fieldName = $N.deserialize($N)", name, buffer);

    b.beginControlFlow("switch (fieldName)");

    for (final Field field : serializedType.getOrderedValues()) {
      b.addCode("case $S:\n", field.getName());
      b.addCode("$>");

      b.beginControlFlow("try (final $T r = $N.scope())", utils.serialReader(), buffer);
      b.addStatement("$N = $N.deserialize(r)", field.getVariableName(),
        field.getType().getFieldSpec());
      b.endControlFlow();

      if (!field.getType().isOptional()) {
        b.addStatement("$N = true", field.getIsSetVariableName());
      }

      b.addStatement("break");
      b.addCode("$<");
    }

    b.addCode("default:\n");
    b.addCode("$>");

    if (failOnMissing) {
      b.addStatement("throw new $T($S + fieldName)", utils.ioException(),
        "Attempting to assign non-existing field: ");
    } else {
      b.addStatement("$N.skip()", buffer);
      b.addStatement("break");
    }

    b.addCode("$<");

    b.endControlFlow();

    b.endControlFlow();

    for (final Field field : serializedType.getOrderedValues()) {
      if (!field.getType().isOptional()) {
        b.beginControlFlow("if (!$N)", field.getIsSetVariableName());
        b.addStatement("throw new $T($S)", utils.ioException(),
          "Missing required field: " + field.getName());
        b.endControlFlow();
      }
    }

    if (typeBuilder.isPresent()) {
      typeBuilder.get().writeTo(returnType, b, serializedType.getFields());
    } else {
      b.addStatement("return new $T($L)", returnType,
        parameterJoiner.join(serializedType.getConstructorVariables()));
    }

    return b.build();
  }

  MethodSpec serialSerializeMethod(final TypeName valueType, final FieldSet serialized) {
    final ParameterSpec buffer = utils.parameter(utils.serialWriter(), "buffer");
    final ParameterSpec value = utils.parameter(valueType, "value");
    final MethodSpec.Builder b = utils.serializeMethod(buffer, value);

    for (final Field field : serialized.getOrderedValues()) {
      b.addStatement("$N.serialize($N, $N.$L())", field.getType().getFieldSpec(), buffer, value,
        field.getAccessor());
    }

    return b.build();
  }

  MethodSpec serialDeserializeMethod(
    ClassName returnType, FieldSet serializedType, Optional<FieldTypeBuilder> typeBuilder
  ) {
    final ParameterSpec buffer = utils.parameter(utils.serialReader(), "buffer");
    final MethodSpec.Builder b = utils.deserializeMethod(returnType, buffer);

    for (final Field field : serializedType.getOrderedValues()) {
      final TypeName fieldType = field.getType().getTypeName();
      final FieldSpec fieldSpec = field.getType().getFieldSpec();
      b.addStatement("final $T $L = $N.deserialize($N)", fieldType, field.getVariableName(),
        fieldSpec, buffer);
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
