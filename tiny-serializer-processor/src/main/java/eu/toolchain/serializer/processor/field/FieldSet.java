package eu.toolchain.serializer.processor.field;

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
import javax.lang.model.element.TypeElement;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FieldSet {
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

  final static Ordering<Field> orderingById = integerOrdering.onResultOf(Field::getId);

  final static Ordering<Field> orderingByConstructor =
    integerOrdering.onResultOf(Field::getConstructorOrder);

  final static Ordering<FieldType> orderingTypesById = integerOrdering.onResultOf(FieldType::getId);

  private final boolean orderById;
  private final boolean orderConstructorById;
  private final List<FieldType> types;
  private final List<Field> fields;
  private final List<Field> ignored;

  public static FieldSet build(
    final AutoSerializeUtils utils, final TypeElement parent, final Set<ElementKind> kinds,
    final AutoSerializeMirror autoSerialize
  ) {
    final FieldSetBuilder fieldSet =
      new FieldSetBuilder(utils, parent, kinds, autoSerialize.isUseGetter());

    for (final Element element : parent.getEnclosedElements()) {
      fieldSet.add(element);
    }

    return fieldSet.build(autoSerialize.isOrderById(), autoSerialize.isOrderConstructorById());
  }

  public Iterable<FieldType> getOrderedTypes() {
    if (orderById) {
      return orderingTypesById.sortedCopy(types);
    }

    return types;
  }

  public Iterable<Field> getOrderedValues() {
    if (orderById) {
      return orderingById.sortedCopy(fields);
    }

    return fields;
  }

  public Iterable<String> getConstructorVariables() {
    final Ordering<Field> ordering;

    if (orderById) {
      ordering = orderingByConstructor.compound(orderingById);
    } else {
      ordering = orderingByConstructor;
    }

    return ImmutableList.copyOf(ordering
      .sortedCopy(fields)
      .stream()
      .map(Field::getVariableName)
      .collect(Collectors.toList()));
  }
}
