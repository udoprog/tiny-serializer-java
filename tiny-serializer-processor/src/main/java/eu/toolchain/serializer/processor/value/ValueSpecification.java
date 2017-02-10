package eu.toolchain.serializer.processor.value;

import static eu.toolchain.serializer.processor.Exceptions.brokenAnnotation;
import static eu.toolchain.serializer.processor.Exceptions.brokenElement;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.TypeName;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.annotation.FieldMirror;
import java.util.Optional;
import java.util.function.Supplier;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import lombok.Data;

@Data
public class ValueSpecification {
  private final Element element;
  private final TypeMirror valueType;
  private final String valueName;
  private final boolean provided;
  private final boolean ignored;
  private final boolean optional;
  private final String accessor;
  private final Optional<Integer> constructorOrder;
  private final Optional<Integer> id;
  private final Optional<String> providerName;

  public static ValueSpecification build(
    final AutoSerializeUtils utils, final Element parent, final Element element,
    boolean defaultUseGetter
  ) {
    final TypeMirror valueType;
    final boolean useGetter;

    final Optional<FieldMirror> field = utils.field(element);

    /**
     * If method, the value type is the return type.
     */
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
    final boolean ignored = utils.ignore(element).isPresent();
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

    if (!ignored) {
      if (!accessorMethodExists(parent, accessor, TypeName.get(valueType))) {
        final String message = String.format("Missing accessor %s %s()", valueType, accessor);

        throw field
          .map(f -> brokenAnnotation(message, element, f.getAnnotation()))
          .orElseGet(() -> brokenElement(message, element));
      }
    }

    return new ValueSpecification(element, valueType, name, provided, ignored, optional, accessor,
      constructorOrder, id, providerName);
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
