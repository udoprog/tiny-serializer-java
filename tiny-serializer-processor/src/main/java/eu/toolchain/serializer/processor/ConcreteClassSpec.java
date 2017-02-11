package eu.toolchain.serializer.processor;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.toolchain.serializer.processor.field.Field;
import eu.toolchain.serializer.processor.field.FieldBuilder;
import eu.toolchain.serializer.processor.field.Value;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import lombok.Data;

@Data
public class ConcreteClassSpec implements ClassSpec {
  private static final Joiner PARAMETER_JOINER = Joiner.on(", ");

  private final AutoSerializeUtils utils;
  private final Elements elements;
  private final FrameworkStatements statements;

  private final String packageName;
  private final List<Field> fields;
  private final List<Value> values;
  private final ClassName elementType;
  private final TypeName superType;
  private final String serializerName;
  private final boolean fieldBased;
  private final boolean failOnMissing;
  private final Optional<FieldBuilder> fieldTypeBuilder;

  @Override
  public JavaFile toSerializer() {
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

    for (final Field t : fields) {
      generated.addField(t.getField());
    }

    generated.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    generated.addSuperinterface(superType);

    if (fieldBased) {
      generated.addMethod(fieldConstructor(count, name));
      generated.addMethod(fieldSerializeMethod(count, name));
      generated.addMethod(fieldDeserializeMethod(count, name));
    } else {
      generated.addMethod(serialConstructor());
      generated.addMethod(serialSerializeMethod());
      generated.addMethod(serialDeserializeMethod());
    }

    return JavaFile
      .builder(packageName, generated.build())
      .skipJavaLangImports(true)
      .indent("    ")
      .build();
  }

  MethodSpec serialConstructor() {
    final ParameterSpec framework = ParameterSpec
      .builder(utils.serializerFramework(), "framework")
      .addModifiers(Modifier.FINAL)
      .build();

    final MethodSpec.Builder b = MethodSpec.constructorBuilder();
    b.addModifiers(Modifier.PUBLIC);
    b.addParameter(framework);

    List<Field> orderedTypes = fields;
    buildConstructorParameters(b, orderedTypes);

    for (final Field field : fields) {
      if (field.getProvidedParameter().isPresent()) {
        b.addStatement("this.$N = $N", field.getField(), field.getProvidedParameter().get());
        continue;
      }

      final FrameworkMethodBuilder builder = (statement, arguments) -> {
        final ImmutableList.Builder<Object> argumentBuilders =
          ImmutableList.builder().add(field.getField()).addAll(arguments);

        b.addStatement(String.format("this.$N = %s", statement),
          argumentBuilders.build().toArray());
      };

      statements
        .resolveStatement(field.getType())
        .build(field.getSubFields(), framework)
        .writeTo(builder);
    }

    return b.build();
  }

  private void buildConstructorParameters(
    final MethodSpec.Builder b, final List<Field> fields
  ) {
    for (final Field t : fields) {
      if (t.getProvidedParameter().isPresent()) {
        b.addParameter(t.getProvidedParameter().get());
      }

      buildConstructorParameters(b, t.getSubFields());
    }
  }

  MethodSpec fieldConstructor(final FieldSpec count, final FieldSpec name) {
    final ParameterSpec framework = ParameterSpec
      .builder(utils.serializerFramework(), "framework")
      .addModifiers(Modifier.FINAL)
      .build();

    final MethodSpec.Builder b = MethodSpec.constructorBuilder();
    b.addModifiers(Modifier.PUBLIC);
    b.addParameter(framework);

    b.addStatement("this.$N = $N.variableInteger()", count, framework);
    b.addStatement("this.$N = $N.string()", name, framework);

    buildConstructorParameters(b, fields);

    for (final Field field : fields) {
      if (field.getProvidedParameter().isPresent()) {
        b.addStatement("this.$N = $N", field.getField(), field.getProvidedParameter().get());
        continue;
      }

      final FrameworkMethodBuilder builder =
        (statement, arguments) -> b.addStatement(String.format("this.$N = %s", statement),
          ImmutableList.builder().add(field.getField()).addAll(arguments).build().toArray());

      statements
        .resolveStatement(field.getType())
        .build(field.getSubFields(), framework)
        .writeTo(builder);
    }

    return b.build();
  }

  MethodSpec fieldSerializeMethod(final FieldSpec count, final FieldSpec name) {
    final ParameterSpec buffer = utils.parameter(utils.serialWriter(), "buffer");
    final ParameterSpec value = utils.parameter(elementType, "value");
    final MethodSpec.Builder b = utils.serializeMethod(buffer, value);

    b.addStatement("$N.serialize($N, $L)", count, buffer, values.size());

    for (final Value field : values) {
      if (field.isValueProvided()) {
        continue;
      }

      if (field.getType().isOptional()) {
        b.addStatement("final $T $N = $N.$L()", field.getType().getType(), field.getVariableName(),
          value, field.getAccessor());
        b.beginControlFlow("if ($N.isPresent())", field.getVariableName());

        b.addStatement("$N.serialize($N, $S)", name, buffer, field.getName());

        b.beginControlFlow("try (final $T w = $N.scope())", utils.serialWriter(), buffer);
        b.addStatement("$N.serialize(w, $N)", field.getType().getField(), field.getVariableName());
        b.endControlFlow();

        b.endControlFlow();
      } else {
        b.addStatement("$N.serialize($N, $S)", name, buffer, field.getName());

        b.beginControlFlow("try (final $T w = $N.scope())", utils.serialWriter(), buffer);
        b.addStatement("$N.serialize(w, $N.$L())", field.getType().getField(), value,
          field.getAccessor());
        b.endControlFlow();
      }
    }

    return b.build();
  }

  MethodSpec fieldDeserializeMethod(final FieldSpec count, final FieldSpec name) {
    final ParameterSpec buffer = utils.parameter(utils.serialReader(), "buffer");
    final MethodSpec.Builder b = utils.deserializeMethod(elementType, buffer);

    for (final Value value : values) {
      if (value.getType().isOptional()) {
        b.addStatement("$T $L = $T.empty()", value.getType().getType(), value.getVariableName(),
          utils.optional());
      } else {
        b.addStatement("$T $L = $L", value.getType().getType(), value.getVariableName(),
          utils.initLiteral(value.getType().getType()));
        b.addStatement("boolean $L = false", value.getIsSetVariableName());
      }
    }

    b.addStatement("final int total = $N.deserialize($N)", count, buffer);

    b.addStatement("int i = 0");

    b.beginControlFlow("while (i++ < total)");

    b.addStatement("final String fieldName = $N.deserialize($N)", name, buffer);

    b.beginControlFlow("switch (fieldName)");

    for (final Value value : values) {
      if (value.isValueProvided()) {
        continue;
      }

      b.addCode("case $S:\n", value.getName());
      b.addCode("$>");

      b.beginControlFlow("try (final $T r = $N.scope())", utils.serialReader(), buffer);
      b.addStatement("$N = $N.deserialize(r)", value.getVariableName(), value.getType().getField());
      b.endControlFlow();

      if (!value.getType().isOptional()) {
        b.addStatement("$N = true", value.getIsSetVariableName());
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

    for (final Value value : values) {
      if (!value.getType().isOptional()) {
        b.beginControlFlow("if (!$N)", value.getIsSetVariableName());
        b.addStatement("throw new $T($S)", utils.ioException(),
          "Missing required field: " + value.getName());
        b.endControlFlow();
      }
    }

    if (fieldTypeBuilder.isPresent()) {
      fieldTypeBuilder.get().writeTo(elementType, b, values);
    } else {
      b.addStatement("return new $T($L)", elementType, PARAMETER_JOINER.join(getVariableNames()));
    }

    return b.build();
  }

  MethodSpec serialSerializeMethod() {
    final ParameterSpec buffer = utils.parameter(utils.serialWriter(), "buffer");
    final ParameterSpec value = utils.parameter(elementType, "value");
    final MethodSpec.Builder b = utils.serializeMethod(buffer, value);

    for (final Value field : values) {
      if (field.isValueProvided()) {
        continue;
      }

      b.addStatement("$N.serialize($N, $N.$L())", field.getType().getField(), buffer, value,
        field.getAccessor());
    }

    return b.build();
  }

  MethodSpec serialDeserializeMethod() {
    final ParameterSpec buffer = utils.parameter(utils.serialReader(), "buffer");
    final MethodSpec.Builder b = utils.deserializeMethod(elementType, buffer);

    for (final Value value : values) {
      if (value.isValueProvided()) {
        continue;
      }

      final FieldSpec fieldSpec = value.getType().getField();
      b.addStatement("final $T $L = $N.deserialize($N)", value.getType().getType(),
        value.getVariableName(), fieldSpec, buffer);
    }

    if (fieldTypeBuilder.isPresent()) {
      fieldTypeBuilder.get().writeTo(elementType, b, values);
    } else {
      b.addStatement("return new $T($L)", elementType, PARAMETER_JOINER.join(getVariableNames()));
    }

    return b.build();
  }

  public List<String> getVariableNames() {
    return values.stream().map(Value::getVariableName).collect(Collectors.toList());
  }
}
