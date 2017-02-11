package eu.toolchain.serializer.processor.field;

import static eu.toolchain.serializer.processor.Exceptions.brokenAnnotation;
import static eu.toolchain.serializer.processor.Exceptions.brokenElement;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.ClassProcessor;
import eu.toolchain.serializer.processor.FrameworkStatements;
import eu.toolchain.serializer.processor.Naming;
import eu.toolchain.serializer.processor.annotation.FieldMirror;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FieldSet {
  private final Naming fieldNaming = new Naming("s_");
  private final Naming providerNaming = new Naming("p_");
  private final Naming variableNaming = new Naming("v_");
  private final Naming isSetVariableNaming = new Naming("i_");

  @Getter
  private final LinkedHashMap<FieldTypeId, Field> fields = new LinkedHashMap<>();
  @Getter
  private final List<Value> values = new ArrayList<>();
  private int externalCount = 0;

  private final ClassProcessor processor;
  private final AutoSerializeUtils utils;
  private final FrameworkStatements statements;
  private final Element parent;
  private final Set<ElementKind> fieldKinds;
  private final boolean defaultUseGetter;

  public void add(final Element element) {
    if (!isSerializableField(element)) {
      return;
    }

    final boolean ignored = utils.ignore(element).isPresent();

    if (ignored) {
      return;
    }

    final TypeMirror typeMirror;
    final boolean useGetter;

    final Optional<FieldMirror> field = utils.field(element);

    /* If method, the value type is the return type. */
    if (element instanceof ExecutableElement) {
      final ExecutableElement executable = (ExecutableElement) element;
      typeMirror = executable.getReturnType();
      // methods are direct accessors, should never use getters.
      useGetter = false;
    } else {
      typeMirror = element.asType();
      useGetter = field.map(FieldMirror::isUseGetter).orElse(defaultUseGetter);
    }

    final boolean provided = field.map(FieldMirror::isProvided).orElse(false);

    final String fieldName = field
      .map(FieldMirror::getFieldName)
      .filter(n -> !n.trim().isEmpty())
      .orElse(element.getSimpleName().toString());

    final String originalName =
      field.map(FieldMirror::getName).filter(n -> !n.trim().isEmpty()).orElse(fieldName);

    final String accessor = field
      .map(FieldMirror::getAccessor)
      .filter(a -> !a.trim().isEmpty())
      .orElseGet(getDefaultAccessor(typeMirror, fieldName, useGetter));

    final Optional<String> providerName =
      field.map(FieldMirror::getProviderName).filter(p -> !p.trim().isEmpty());

    final boolean external = field.map(FieldMirror::isExternal).orElse(false);

    if (!external) {
      if (!accessorMethodExists(parent, accessor, TypeName.get(typeMirror))) {
        final String message = String.format("Missing accessor %s %s()", typeMirror, accessor);

        throw field
          .map(f -> brokenAnnotation(message, element, f.getAnnotation()))
          .orElseGet(() -> brokenElement(message, element));
      }
    }

    final TypeName valueTypeName = TypeName.get(typeMirror);

    final boolean custom = statements.resolveStatement(typeMirror).isCustom();

    final FieldTypeId fieldTypeId;

    if (external || custom) {
      fieldTypeId =
        new FieldTypeId(valueTypeName, provided, providerName, Optional.of(externalCount++));
    } else {
      fieldTypeId = new FieldTypeId(valueTypeName, provided, providerName, Optional.empty());
    }

    final String variableName = variableNaming.forName(originalName);

    // each field type should only be declared once
    final Field type = fields.computeIfAbsent(fieldTypeId, key -> {
      final FieldSpec fieldSpec;
      final Optional<ParameterSpec> providedParameter;

      if (key.isProvided()) {
        final String typeFieldName = fieldNaming.forType(key.getValueType(), key.isProvided());

        final TypeName serializerType = TypeName.get(utils.serializerFor(typeMirror));

        providedParameter =
          Optional.of(buildProvidedSpec(serializerType, key.getProviderName(), key.getValueType()));

        fieldSpec =
          FieldSpec.builder(serializerType, typeFieldName).addModifiers(Modifier.FINAL).build();
      } else if (external) {
        providedParameter = Optional.of(
          ParameterSpec.builder(key.getValueType(), variableName, Modifier.FINAL).build());

        fieldSpec =
          FieldSpec.builder(key.getValueType(), variableName).addModifiers(Modifier.FINAL).build();
      } else {
        final String typeFieldName = fieldNaming.forType(key.getValueType(), key.isProvided());

        final TypeName serializerType = TypeName.get(utils.serializerFor(typeMirror));

        providedParameter = Optional.empty();

        fieldSpec =
          FieldSpec.builder(serializerType, typeFieldName).addModifiers(Modifier.FINAL).build();
      }

      final List<Field> subFields = new ArrayList<>();

      if (statements.resolveStatement(typeMirror).isCustom()) {
        processor.buildSpec(((DeclaredType) typeMirror).asElement()).ifPresent(spec -> {
          spec.getFields().stream().map(this::convertSubField).forEach(subFields::add);
        });
      }

      return new Field(originalName, providerName, provided, external, typeMirror, fieldSpec,
        providedParameter, utils.isOptional(typeMirror), subFields);
    });

    final String isSetVariableName = isSetVariableNaming.forName(originalName);

    values.add(new Value(type, originalName, accessor, variableName, isSetVariableName, external));
  }

  private Field convertSubField(final Field field) {
    if (field.isProvided()) {
      final TypeName serializerType = TypeName.get(utils.serializerFor(field.getType()));

      return field.withProvidedParameter(field.getProvidedParameter().map(s -> {
        return buildProvidedSpec(serializerType, field.getProviderName(),
          TypeName.get(field.getType()));
      }));
    }

    if (field.isExternal()) {
      return field.withProvidedParameter(field.getProvidedParameter().map(s -> {
        return ParameterSpec
          .builder(TypeName.get(field.getType()), providerNaming.forName(field.getOriginalName()),
            Modifier.FINAL)
          .build();
      }));
    }

    return field;
  }

  private ParameterSpec buildProvidedSpec(
    final TypeName serializerType, final Optional<String> providerName, final TypeName fieldType
  ) {
    final String uniqueProviderName;

    if (providerName.isPresent()) {
      uniqueProviderName = providerNaming.forName(providerName.get());
    } else {
      uniqueProviderName = providerNaming.forType(fieldType, false);
    }

    return ParameterSpec.builder(serializerType, uniqueProviderName, Modifier.FINAL).build();
  }

  private boolean isSerializableField(final Element element) {
    if (!fieldKinds.contains(element.getKind())) {
      return false;
    }

    // skip static fields/methods.
    if (element.getModifiers().contains(Modifier.STATIC)) {
      return false;
    }

    // ignore final field with constant value.
    if (element instanceof VariableElement &&
      ((VariableElement) element).getConstantValue() != null) {
      return false;
    }

    if (element instanceof ExecutableElement) {
      final ExecutableElement e = (ExecutableElement) element;

      if (!e.getModifiers().contains(Modifier.ABSTRACT)) {
        return false;
      }
    }

    return true;
  }

  static boolean accessorMethodExists(
    final Element root, final String accessor, final TypeName serializedType
  ) {
    for (final Element enclosed : root.getEnclosedElements()) {
      if (enclosed.getKind() != ElementKind.METHOD) {
        continue;
      }

      final ExecutableElement executable = (ExecutableElement) enclosed;

      if (!executable.getSimpleName().toString().equals(accessor)) {
        continue;
      }

      if (!executable.getParameters().isEmpty()) {
        continue;
      }

      final TypeName returnType = TypeName.get(executable.getReturnType());

      return serializedType.equals(returnType);
    }

    return false;
  }

  static Supplier<String> getDefaultAccessor(
    final TypeMirror fieldType, final String fieldName, final boolean useGetter
  ) {
    return () -> {
      if (!useGetter) {
        return fieldName;
      }

      if (fieldType.getKind() == TypeKind.BOOLEAN) {
        return "is" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
      }

      return "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
    };
  }
}
