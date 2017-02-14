package eu.toolchain.serializer.processor.field;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.ClassProcessor;
import eu.toolchain.serializer.processor.Naming;
import eu.toolchain.serializer.processor.annotation.FieldMirror;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FieldSet {
  private final Naming fieldNaming = new Naming("s_");
  private final Naming providerNaming = new Naming("p_");
  private final Naming variableNaming = new Naming("v_");
  private final Naming isSetVariableNaming = new Naming("i_");

  private final LinkedHashMap<FieldTypeId, Field> commonFields = new LinkedHashMap<>();

  @Getter
  private final List<Field> fields = new ArrayList<>();
  @Getter
  private final List<Value> values = new ArrayList<>();

  private final ClassProcessor processor;
  private final AutoSerializeUtils utils;
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

    final FieldMirror fieldMirror =
      utils.field(element).orElseGet(() -> FieldMirror.defaultInstance(defaultUseGetter));

    /* If method, the value type is the return type. */
    if (element instanceof ExecutableElement) {
      final ExecutableElement executable = (ExecutableElement) element;
      typeMirror = executable.getReturnType();
      // methods are direct accessors, should never use getters.
      useGetter = false;
    } else {
      typeMirror = element.asType();
      useGetter = fieldMirror.isUseGetter();
    }

    final boolean provided = fieldMirror.isProvided();

    final String fieldName = fieldMirror.getFieldName().orElse(element.getSimpleName().toString());

    final String originalName = fieldMirror.getName().orElse(fieldName);

    final String accessor = fieldMirror
      .getAccessor()
      .orElseGet(() -> getDefaultAccessor(typeMirror, fieldName, useGetter));

    final Optional<String> providerName = fieldMirror.getProviderName();

    final boolean external = fieldMirror.isExternal();

    final TypeName fieldType = TypeName.get(typeMirror);

    final String variableName = variableNaming.forName(originalName);

    final Optional<ParameterSpec> providedParameter =
      fieldMirror.buildProvidedParameter(utils, typeMirror, providerNaming, variableName);

    final FieldSpec fieldSpec =
      fieldMirror.buildField(utils, typeMirror, fieldNaming, variableName);

    final Field valueField;

    if (external) {
      valueField = new Field(originalName, providerName, provided, external, typeMirror, fieldSpec,
        providedParameter, utils.isOptional(typeMirror));

      fields.add(valueField);
    } else {
      final FieldTypeId key = new FieldTypeId(fieldType, providerName, provided);

      valueField = commonFields.computeIfAbsent(key, ignore -> {
        final Field newField =
          new Field(originalName, providerName, provided, external, typeMirror, fieldSpec,
            providedParameter, utils.isOptional(typeMirror));
        fields.add(newField);
        return newField;
      });
    }

    final String isSetVariableName = isSetVariableNaming.forName(originalName);

    values.add(
      new Value(valueField, originalName, accessor, variableName, isSetVariableName, external));
  }

  private Field convertSubField(final Field field) {
    if (field.isProvided()) {
      final TypeName serializerType = TypeName.get(utils.serializerFor(field.getType()));

      return field.withProvidedParameter(field.getProvidedParameter().map(s -> {
        return FieldMirror.buildProvidedSpec(serializerType, field.getProviderName(),
          field.getType(), providerNaming);
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

  static String getDefaultAccessor(
    final TypeMirror fieldType, final String fieldName, final boolean useGetter
  ) {
    if (!useGetter) {
      return fieldName;
    }

    if (fieldType.getKind() == TypeKind.BOOLEAN) {
      return "is" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
    }

    return "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
  }

  @Data
  private static class FieldTypeId {
    private final TypeName type;
    private final Optional<String> providedName;
    private final boolean provided;
  }
}
