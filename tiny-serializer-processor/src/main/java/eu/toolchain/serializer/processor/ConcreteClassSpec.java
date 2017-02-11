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
import eu.toolchain.serializer.processor.field.FieldSet;
import eu.toolchain.serializer.processor.field.FieldType;
import eu.toolchain.serializer.processor.field.FieldTypeBuilder;
import java.util.Optional;
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
  private final FieldSet fields;
  private final ClassName elementType;
  private final TypeName superType;
  private final String serializerName;
  private final boolean fieldBased;
  private final boolean failOnMissing;
  private final Optional<FieldTypeBuilder> fieldTypeBuilder;

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

    for (final FieldType t : fields.getTypes()) {
      generated.addField(t.getFieldSpec());
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

  @Override
  public FieldSet getFieldSet() {
    return fields;
  }

  MethodSpec serialConstructor() {
    final ParameterSpec framework = ParameterSpec
      .builder(utils.serializerFramework(), "framework")
      .addModifiers(Modifier.FINAL)
      .build();

    final MethodSpec.Builder b = MethodSpec.constructorBuilder();
    b.addModifiers(Modifier.PUBLIC);
    b.addParameter(framework);

    for (final FieldType t : fields.getOrderedTypes()) {
      if (t.getProvidedParameterSpec().isPresent()) {
        b.addParameter(t.getProvidedParameterSpec().get());
      }
    }

    for (final FieldType fieldType : fields.getOrderedTypes()) {
      if (fieldType.getProvidedParameterSpec().isPresent()) {
        b.addStatement("this.$N = $N", fieldType.getFieldSpec(),
          fieldType.getProvidedParameterSpec().get());
        continue;
      }

      final FrameworkMethodBuilder builder =
        (statement, arguments) -> b.addStatement(String.format("this.$N = %s", statement),
          ImmutableList
            .builder()
            .add(fieldType.getFieldSpec())
            .addAll(arguments)
            .build()
            .toArray());

      statements.resolveStatement(fieldType.getTypeMirror()).build(framework).writeTo(builder);
    }

    return b.build();
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

    for (final FieldType t : fields.getOrderedTypes()) {
      if (t.getProvidedParameterSpec().isPresent()) {
        b.addParameter(t.getProvidedParameterSpec().get());
      }
    }

    for (final FieldType fieldType : fields.getOrderedTypes()) {
      if (fieldType.getProvidedParameterSpec().isPresent()) {
        b.addStatement("this.$N = $N", fieldType.getFieldSpec(),
          fieldType.getProvidedParameterSpec().get());
        continue;
      }

      final FrameworkMethodBuilder builder =
        (statement, arguments) -> b.addStatement(String.format("this.$N = %s", statement),
          ImmutableList
            .builder()
            .add(fieldType.getFieldSpec())
            .addAll(arguments)
            .build()
            .toArray());

      statements.resolveStatement(fieldType.getTypeMirror()).build(framework).writeTo(builder);
    }

    return b.build();
  }

  MethodSpec fieldSerializeMethod(final FieldSpec count, final FieldSpec name) {
    final ParameterSpec buffer = utils.parameter(utils.serialWriter(), "buffer");
    final ParameterSpec value = utils.parameter(elementType, "value");
    final MethodSpec.Builder b = utils.serializeMethod(buffer, value);

    b.addStatement("$N.serialize($N, $L)", count, buffer, fields.getFields().size());

    for (final Field field : fields.getOrderedValues()) {
      if (field.isValueProvided()) {
        continue;
      }

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

  MethodSpec fieldDeserializeMethod(final FieldSpec count, final FieldSpec name) {
    final ParameterSpec buffer = utils.parameter(utils.serialReader(), "buffer");
    final MethodSpec.Builder b = utils.deserializeMethod(elementType, buffer);

    for (final Field field : fields.getOrderedValues()) {
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

    for (final Field field : fields.getOrderedValues()) {
      if (field.isValueProvided()) {
        continue;
      }

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

    for (final Field field : fields.getOrderedValues()) {
      if (!field.getType().isOptional()) {
        b.beginControlFlow("if (!$N)", field.getIsSetVariableName());
        b.addStatement("throw new $T($S)", utils.ioException(),
          "Missing required field: " + field.getName());
        b.endControlFlow();
      }
    }

    if (fieldTypeBuilder.isPresent()) {
      fieldTypeBuilder.get().writeTo(elementType, b, fields.getOrderedValues());
    } else {
      b.addStatement("return new $T($L)", elementType,
        PARAMETER_JOINER.join(fields.getConstructorVariables()));
    }

    return b.build();
  }

  MethodSpec serialSerializeMethod() {
    final ParameterSpec buffer = utils.parameter(utils.serialWriter(), "buffer");
    final ParameterSpec value = utils.parameter(elementType, "value");
    final MethodSpec.Builder b = utils.serializeMethod(buffer, value);

    for (final Field field : fields.getOrderedValues()) {
      if (field.isValueProvided()) {
        continue;
      }

      b.addStatement("$N.serialize($N, $N.$L())", field.getType().getFieldSpec(), buffer, value,
        field.getAccessor());
    }

    return b.build();
  }

  MethodSpec serialDeserializeMethod() {
    final ParameterSpec buffer = utils.parameter(utils.serialReader(), "buffer");
    final MethodSpec.Builder b = utils.deserializeMethod(elementType, buffer);

    for (final Field field : fields.getOrderedValues()) {
      if (field.isValueProvided()) {
        continue;
      }

      final TypeName fieldType = field.getType().getTypeName();
      final FieldSpec fieldSpec = field.getType().getFieldSpec();
      b.addStatement("final $T $L = $N.deserialize($N)", fieldType, field.getVariableName(),
        fieldSpec, buffer);
    }

    if (fieldTypeBuilder.isPresent()) {
      fieldTypeBuilder.get().writeTo(elementType, b, fields.getOrderedValues());
    } else {
      b.addStatement("return new $T($L)", elementType,
        PARAMETER_JOINER.join(fields.getConstructorVariables()));
    }

    return b.build();
  }
}
