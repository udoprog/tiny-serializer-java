package eu.toolchain.serializer.processor.value;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor8;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ValueSet {
    final static Ordering<Optional<Integer>> integerOrdering = Ordering.from(new Comparator<Optional<Integer>>() {
        @Override
        public int compare(Optional<Integer> a, Optional<Integer> b) {
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
        }
    });

    final static Ordering<Value> orderingById = integerOrdering
            .onResultOf((f) -> f.getId());

    final static Ordering<Value> orderingByCtorOrder = integerOrdering
            .onResultOf((f) -> f.getConstructorOrder());

    final static Ordering<ValueType> orderingTypesById = integerOrdering
            .onResultOf((ValueType f) -> f.getId());

    private final boolean orderById;
    private final boolean orderConstructorById;
    private final List<ValueType> types;
    private final List<Value> values;

    public ValueSet(final boolean orderById, final boolean orderConstructorById) {
        this(orderById, orderConstructorById, ImmutableList.of(), ImmutableList.of());
    }

    public static Unverified<ValueSet> build(final AutoSerializeUtils utils, final TypeElement element,
            final Set<ElementKind> kinds, final AutoSerializeMirror autoSerialize) {
        final ValueSetBuilder valueSet = new ValueSetBuilder(utils);

        for (final Unverified<ValueSpecification> value : parseVAlueSgetValues(utils, element, kinds,
                autoSerialize.isUseGetter())) {
            valueSet.add(value);
        }

        return valueSet.build(autoSerialize.isOrderById(), autoSerialize.isOrderConstructorById());
    }

    static Iterable<Unverified<ValueSpecification>> parseVAlueSgetValues(final AutoSerializeUtils utils,
            final TypeElement enclosing, final Set<ElementKind> kinds, final boolean defaultUseGetter) {
        final ImmutableList.Builder<Unverified<ValueSpecification>> builder = ImmutableList.builder();

        for (final Element element : enclosing.getEnclosedElements()) {
            if (!kinds.contains(element.getKind())) {
                continue;
            }

            // skip static fields.
            if (element.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }

            // ignore final field with constant value.
            if (element instanceof VariableElement && ((VariableElement)element).getConstantValue() != null) {
                continue;
            }

            if (element.getAnnotation(AutoSerialize.Ignore.class) != null) {
                continue;
            }

            builder.add(ValueSpecification.build(utils, enclosing, element, defaultUseGetter));
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

        return ImmutableList.copyOf(
                ordering.sortedCopy(values).stream().map((f) -> f.getVariableName()).collect(Collectors.toList()));
    }
}