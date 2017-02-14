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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Generated;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor6;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class ConcreteClassSpec implements ClassSpec {
  private static final Joiner ARGUMENT_JOINER = Joiner.on(", ");
  private static final Map<TypeName, String> DIRECT = new HashMap<>();

  static {
    DIRECT.put(ClassName.get(String.class), "$N.string()");
    DIRECT.put(ClassName.get(Byte.class), "$N.fixedByte()");
    DIRECT.put(ClassName.get(Short.class), "$N.fixedShort()");
    DIRECT.put(ClassName.get(Integer.class), "$N.fixedInteger()");
    DIRECT.put(ClassName.get(Long.class), "$N.fixedLong()");
    DIRECT.put(ClassName.get(Float.class), "$N.fixedFloat()");
    DIRECT.put(ClassName.get(Double.class), "$N.fixedDouble()");
    DIRECT.put(ClassName.get(Character.class), "$N.fixedCharacter()");
    DIRECT.put(ClassName.get(Boolean.class), "$N.fixedBoolean()");
    DIRECT.put(ClassName.get(UUID.class), "$N.uuid()");
    DIRECT.put(TypeName.get(boolean[].class), "$N.booleanArray()");
    DIRECT.put(TypeName.get(byte[].class), "$N.byteArray()");
    DIRECT.put(TypeName.get(short[].class), "$N.shortArray()");
    DIRECT.put(TypeName.get(int[].class), "$N.intArray()");
    DIRECT.put(TypeName.get(long[].class), "$N.longArray()");
    DIRECT.put(TypeName.get(float[].class), "$N.floatArray()");
    DIRECT.put(TypeName.get(double[].class), "$N.doubleArray()");
    DIRECT.put(TypeName.get(char[].class), "$N.charArray()");
  }

  static final Map<TypeName, Parameterized> PARAMETERIZED = new HashMap<>();

  static {
    PARAMETERIZED.put(ClassName.get(List.class), new Parameterized("$N.list({0})", 1));
    PARAMETERIZED.put(ClassName.get(Map.class), new Parameterized("$N.map({0}, {1})", 2));
    PARAMETERIZED.put(ClassName.get(SortedMap.class),
      new Parameterized("$N.sortedMap({0}, {1})", 2, 2));
    PARAMETERIZED.put(ClassName.get(NavigableMap.class),
      new Parameterized("$N.navigableMap({0}, {1})", 2, 2));
    PARAMETERIZED.put(ClassName.get(Set.class), new Parameterized("$N.set({0})", 1));
    PARAMETERIZED.put(ClassName.get(SortedSet.class), new Parameterized("$N.sortedSet({0})", 1, 2));
    PARAMETERIZED.put(ClassName.get(NavigableSet.class),
      new Parameterized("$N.navigableSet({0})", 1, 2));
    PARAMETERIZED.put(ClassName.get(Optional.class), new Parameterized("$N.optional({0})", 1));
  }

  private final ClassProcessor processor;
  private final AutoSerializeUtils utils;
  private final Elements elements;

  private final String packageName;
  private final List<Field> fields;
  private final List<Value> values;
  private final ClassName elementType;
  private final TypeName superType;
  private final String serializerName;
  private final boolean fieldBased;
  private final boolean failOnMissing;
  private final Optional<FieldBuilder> fieldTypeBuilder;

  private final Naming providedParameter = new Naming("p_");

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

      final FrameworkStatement statement = FrameworkStatement.create("this.$N = {0}",
        ImmutableList.of(field.getField(), resolveStatement(field.getType(), framework)));
      b.addStatement(statement.getFormat(), statement.getArguments().toArray());

      statement.getProvidedFields().forEach(b::addParameter);
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

      final FrameworkStatement statement = FrameworkStatement.create("this.$N = {0}",
        ImmutableList.of(field.getField(), resolveStatement(field.getType(), framework)));
      b.addStatement(statement.getFormat(), statement.getArguments().toArray());

      statement.getProvidedFields().forEach(b::addParameter);
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
      b.addStatement("return new $T($L)", elementType, ARGUMENT_JOINER.join(getVariableNames()));
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
      b.addStatement("return new $T($L)", elementType, ARGUMENT_JOINER.join(getVariableNames()));
    }

    return b.build();
  }

  public List<String> getVariableNames() {
    return values.stream().map(Value::getVariableName).collect(Collectors.toList());
  }

  public FrameworkStatement resolveStatement(TypeMirror type, final Object framework) {
    type = utils.boxedIfNeeded(type);

    final String statement = DIRECT.get(TypeName.get(type));

    if (statement != null) {
      return FrameworkStatement.create(statement, ImmutableList.of(framework));
    }

    if (type instanceof ArrayType) {
      final ArrayType a = (ArrayType) type;
      return resolveArrayType(a, framework);
    }

    if (type.getKind() != TypeKind.DECLARED) {
      throw new IllegalArgumentException("Cannot handle type: " + type);
    }

    final DeclaredType d = (DeclaredType) type;

    if (d.asElement().getKind() == ElementKind.ENUM) {
      return resolveEnum((TypeElement) d.asElement(), framework);
    }

    if (!d.getTypeArguments().isEmpty()) {
      return resolveParameterizedType(d, framework);
    }

    return resolveCustomSerializer(d, framework);
  }

  private FrameworkStatement resolveArrayType(final ArrayType a, final Object framework) {
    final TypeMirror componentType = a.getComponentType();

    if (utils.isPrimitive(componentType)) {
      throw new IllegalArgumentException(
        "Cannot serialize array with a primitive component type: " + a);
    }

    final FrameworkStatement component =
      resolveStatement(utils.boxedIfNeeded(componentType), framework);

    final TypeName innerMost = TypeName.get(arrayInnerMost(componentType));
    // Get parenthesis combination after the size parameter.
    final String parens = arrayParensAfterSize(componentType);

    final List<Object> arguments = new ArrayList<>();

    arguments.add(framework);
    arguments.add(componentType);
    arguments.add(component);
    arguments.add(innerMost);
    arguments.add(FrameworkStatement.create(parens));

    return FrameworkStatement.create("$N.<$T>array({0}, (s) -> new $T[s]{1})", arguments);
  }

  private String arrayParensAfterSize(TypeMirror t) {
    return t.accept(new SimpleTypeVisitor6<String, Void>() {
      @Override
      public String visitArray(ArrayType t, Void p) {
        return "[]" + arrayParensAfterSize(t.getComponentType());
      }

      @Override
      protected String defaultAction(TypeMirror e, Void p) {
        return "";
      }
    }, null);
  }

  private TypeMirror arrayInnerMost(TypeMirror t) {
    return t.accept(new SimpleTypeVisitor6<TypeMirror, Void>() {
      @Override
      public TypeMirror visitArray(ArrayType t, Void p) {
        return arrayInnerMost(t.getComponentType());
      }

      @Override
      protected TypeMirror defaultAction(TypeMirror e, Void p) {
        return e;
      }
    }, null);
  }

  private FrameworkStatement resolveEnum(final TypeElement element, final Object framework) {
    final ClassName enumType = ClassName.get(element);
    return FrameworkStatement.create("$N.forEnum($T.values())",
      ImmutableList.of(framework, enumType));
  }

  FrameworkStatement resolveCustomSerializer(final DeclaredType type, final Object framework) {
    final List<String> parameterFormat = new ArrayList<>();
    parameterFormat.add("$N");

    final List<Object> arguments = new ArrayList<>();
    arguments.add(utils.serializerClassFor(type));
    arguments.add(framework);

    final List<ParameterSpec> providedFields = new ArrayList<>();

    processor.buildSpec(type.asElement()).ifPresent(spec -> {
      spec.getFields().forEach(p -> {
        p.getProvidedParameter().ifPresent(s -> {
          final ParameterSpec parameter = ParameterSpec
            .builder(s.type, providedParameter.forName(p.getOriginalName()))
            .addModifiers(Modifier.FINAL)
            .build();

          providedFields.add(parameter);
          parameterFormat.add("$N");
          arguments.add(parameter);
        });
      });
    });

    final String format =
      MessageFormat.format("new $T({0})", ARGUMENT_JOINER.join(parameterFormat));

    return FrameworkStatement.create(format, arguments, providedFields);
  }

  private ParameterizedMatch findBestMatch(DeclaredType type) {
    final Queue<TypeMirror> types = new LinkedList<>();

    types.add(type);

    final SortedSet<ParameterizedMatch> matches = new TreeSet<>();

    while (!types.isEmpty()) {
      final DeclaredType t = (DeclaredType) types.poll();

      final TypeElement e = (TypeElement) t.asElement();
      final ClassName c = ClassName.get(e);

      final Parameterized p = PARAMETERIZED.get(c);

      if (p == null) {
        break;
      }

      matches.add(new ParameterizedMatch(p, t));
    }

    if (matches.isEmpty()) {
      throw new IllegalArgumentException("Type not supported: " + type);
    }

    return matches.last();
  }

  private FrameworkStatement resolveParameterizedType(
    final DeclaredType type, final Object framework
  ) {
    final ParameterizedMatch p = findBestMatch(type);

    final Iterator<? extends TypeMirror> typeArguments = p.type.getTypeArguments().iterator();

    final List<Object> arguments = new ArrayList<>();

    arguments.add(framework);

    for (int i = 0; i < p.parameterized.parameterCount; i++) {
      arguments.add(resolveStatement(typeArguments.next(), framework));
    }

    return FrameworkStatement.create(p.parameterized.statement, arguments);
  }

  static class Parameterized implements Comparable<Parameterized> {
    final String statement;
    final int parameterCount;
    final int priority;

    public Parameterized(String statement, int parameterCount) {
      this(statement, parameterCount, 0);
    }

    public Parameterized(String statement, int parameterCount, int priority) {
      this.statement = statement;
      this.parameterCount = parameterCount;
      this.priority = priority;
    }

    @Override
    public int compareTo(Parameterized o) {
      return Integer.compare(priority, o.priority);
    }
  }

  @RequiredArgsConstructor
  static class ParameterizedMatch implements Comparable<ParameterizedMatch> {
    final Parameterized parameterized;
    final DeclaredType type;

    @Override
    public int compareTo(ParameterizedMatch o) {
      return parameterized.compareTo(o.parameterized);
    }
  }
}
