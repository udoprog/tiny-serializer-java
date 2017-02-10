package eu.toolchain.serializer.processor.value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ValueSet {
  final static Ordering<Optional<Integer>> integerOrdering = Ordering.from((a, b) -> {
    if (a.isPresent() && !b.isPresent()) {
      return -1;
    }

    if (!a.isPresent() && b.isPresent()) {
      return 1;
    }

    if (!a.isPresent() && !b.isPresent()) {
      return 0;
    }

    return Integer.compare(a.get(), b.get());
  });

  final static Ordering<Value> orderingById = integerOrdering.onResultOf((f) -> f.getId());

  final static Ordering<Value> orderingByCtorOrder =
    integerOrdering.onResultOf(Value::getConstructorOrder);

  final static Ordering<ValueType> orderingTypesById = integerOrdering.onResultOf(ValueType::getId);

  private final boolean orderById;
  private final boolean orderConstructorById;
  private final List<ValueType> types;
  private final List<Value> values;
  private final List<Value> ignored;

  public static ValueSet build(
    final AutoSerializeUtils utils, final TypeElement element, final Set<ElementKind> kinds,
    final AutoSerializeMirror autoSerialize
  ) {
    final ValueSetBuilder valueSet = new ValueSetBuilder(utils);

    for (final ValueSpecification value : parseValues(utils, element, kinds,
      autoSerialize.isUseGetter())) {
      valueSet.add(value);
    }

    return valueSet.build(autoSerialize.isOrderById(), autoSerialize.isOrderConstructorById());
  }

  static Iterable<ValueSpecification> parseValues(
    final AutoSerializeUtils utils, final TypeElement enclosing, final Set<ElementKind> kinds,
    final boolean defaultUseGetter
  ) {
    final ImmutableList.Builder<ValueSpecification> builder = ImmutableList.builder();

    for (final Element element : enclosing.getEnclosedElements()) {
      if (!kinds.contains(element.getKind())) {
        continue;
      }

      // skip static fields/methods.
      if (element.getModifiers().contains(Modifier.STATIC)) {
        continue;
      }

      // ignore final field with constant value.
      if (element instanceof VariableElement &&
        ((VariableElement) element).getConstantValue() != null) {
        continue;
      }

      if (element instanceof ExecutableElement) {
        final ExecutableElement e = (ExecutableElement) element;

        if (!e.getModifiers().contains(Modifier.ABSTRACT)) {
          continue;
        }
      }

      final ValueSpecification value =
        ValueSpecification.build(utils, enclosing, element, defaultUseGetter);

      if (value.isIgnored()) {
        continue;
      }

      builder.add(value);
    }

    return builder.build();
  }

  public Iterable<ValueType> getOrderedTypes() {
    if (orderById) {
      return orderingTypesById.sortedCopy(types);
    }

    return types;
  }

  public Iterable<Value> getOrderedValues() {
    if (orderById) {
      return orderingById.sortedCopy(values);
    }

    return values;
  }

  public Iterable<String> getConstructorVariables() {
    final Ordering<Value> ordering;

    if (orderById) {
      ordering = orderingByCtorOrder.compound(orderingById);
    } else {
      ordering = orderingByCtorOrder;
    }

    return ImmutableList.copyOf(ordering
      .sortedCopy(values)
      .stream()
      .map((f) -> f.getVariableName())
      .collect(Collectors.toList()));
  }
}
