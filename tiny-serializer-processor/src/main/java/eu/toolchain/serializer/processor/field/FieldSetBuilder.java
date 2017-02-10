package eu.toolchain.serializer.processor.field;

import static eu.toolchain.serializer.processor.Exceptions.brokenAnnotation;
import static eu.toolchain.serializer.processor.Exceptions.brokenElement;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FieldSetBuilder {
  private final Naming fieldNaming = new Naming("s_");
  private final Naming providerNaming = new Naming("p_");
  private final Naming variableNaming = new Naming("v_");
  private final Naming isSetVariableNaming = new Naming("i_");

  private final LinkedHashMap<FieldTypeId, FieldType> fieldTypes = new LinkedHashMap<>();
  private final List<Field> fields = new ArrayList<>();
  private int valueProvidedCount = 0;

  final AutoSerializeUtils utils;
  final Element parent;
  final Set<ElementKind> kinds;
  final boolean defaultUseGetter;

  public void add(final Element element) {
    if (!isSerializableField(element)) {
      return;
    }

    final boolean ignored = utils.ignore(element).isPresent();

    if (ignored) {
      return;
    }

    final boolean valueProvided = utils.provided(element).isPresent();

    final TypeMirror valueType;
    final boolean useGetter;

    final Optional<FieldMirror> field = utils.field(element);

    /* If method, the value type is the return type. */
    if (element instanceof ExecutableElement) {
      final ExecutableElement executable = (ExecutableElement) element;
      valueType = executable.getReturnType();
      // methods are direct accessors, should never use getters.
      useGetter = false;
    } else {
      valueType = element.asType();
      useGetter = field.map(FieldMirror::isUseGetter).orElse(defaultUseGetter);
    }

    final boolean provided = field.map(FieldMirror::isProvided).orElse(false);
    final boolean optional = utils.isOptional(valueType);

    final String fieldName = field
      .map(FieldMirror::getFieldName)
      .filter(n -> !n.trim().isEmpty())
      .orElse(element.getSimpleName().toString());

    final String name =
      field.map(FieldMirror::getName).filter(n -> !n.trim().isEmpty()).orElse(fieldName);

    final String accessor = field
      .map(FieldMirror::getAccessor)
      .filter(a -> !a.trim().isEmpty())
      .orElseGet(getDefaultAccessor(valueType, fieldName, useGetter));

    final Optional<String> providerName =
      field.map(FieldMirror::getProviderName).filter(p -> !p.trim().isEmpty());

    final Optional<Integer> constructorOrder =
      field.map(FieldMirror::getConstructorOrder).filter(o -> o >= 0);

    final Optional<Integer> id = field.map(FieldMirror::getId).filter(o -> o >= 0);

    if (!valueProvided) {
      if (!accessorMethodExists(parent, accessor, TypeName.get(valueType))) {
        final String message = String.format("Missing accessor %s %s()", valueType, accessor);

        throw field
          .map(f -> brokenAnnotation(message, element, f.getAnnotation()))
          .orElseGet(() -> brokenElement(message, element));
      }
    }

    final TypeName valueTypeName = TypeName.get(valueType);
    final TypeName serializerType = TypeName.get(utils.serializerFor(valueType));

    final FieldTypeId fieldTypeId;

    if (valueProvided) {
      fieldTypeId =
        new FieldTypeId(valueTypeName, provided, providerName, Optional.of(valueProvidedCount++));
    } else {
      fieldTypeId = new FieldTypeId(valueTypeName, provided, providerName, Optional.empty());
    }

    final String variableName = variableNaming.forName(name);

    // each field type should only be declared once
    final FieldType type = fieldTypes.computeIfAbsent(fieldTypeId, key -> {
      final FieldSpec fieldSpec;
      final Optional<ParameterSpec> providedParameterSpec;

      if (key.isProvided()) {
        final String typeFieldName = fieldNaming.forType(key.getFieldType(), key.isProvided());

        final String uniqueProviderName;

        if (key.getProviderName().isPresent()) {
          uniqueProviderName = providerNaming.forName(key.getProviderName().get());
        } else {
          uniqueProviderName = providerNaming.forType(key.getFieldType(), false);
        }

        providedParameterSpec = Optional.of(
          ParameterSpec.builder(serializerType, uniqueProviderName, Modifier.FINAL).build());

        fieldSpec =
          FieldSpec.builder(serializerType, typeFieldName).addModifiers(Modifier.FINAL).build();
      } else if (valueProvided) {
        providedParameterSpec = Optional.of(
          ParameterSpec.builder(key.getFieldType(), variableName, Modifier.FINAL).build());

        fieldSpec =
          FieldSpec.builder(key.getFieldType(), variableName).addModifiers(Modifier.FINAL).build();
      } else {
        final String typeFieldName = fieldNaming.forType(key.getFieldType(), key.isProvided());

        providedParameterSpec = Optional.empty();

        fieldSpec =
          FieldSpec.builder(serializerType, typeFieldName).addModifiers(Modifier.FINAL).build();
      }

      return new FieldType(key, valueType, valueTypeName, fieldSpec, providedParameterSpec,
        optional, id);
    });

    final String isSetVariableName = isSetVariableNaming.forName(name);

    fields.add(
      new Field(type, name, accessor, variableName, isSetVariableName, id, constructorOrder,
        valueProvided));
  }

  private boolean isSerializableField(final Element element) {
    if (!kinds.contains(element.getKind())) {
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

  public FieldSet build(boolean isOrdereById, boolean isOrderConstructorById) {
    return new FieldSet(isOrdereById, isOrderConstructorById,
      ImmutableList.copyOf(fieldTypes.values()), ImmutableList.copyOf(fields));
  }
}
