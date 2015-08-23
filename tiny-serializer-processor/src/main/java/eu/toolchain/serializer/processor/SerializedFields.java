package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
class SerializedFields {
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

    final static Ordering<SerializedField> orderingById = integerOrdering
            .onResultOf((f) -> f.getId());

    final static Ordering<SerializedField> orderingByCtorOrder = integerOrdering
            .onResultOf((f) -> f.getConstructorOrder());

    final static Ordering<SerializedFieldType> orderingTypesById = integerOrdering
            .onResultOf((SerializedFieldType f) -> f.getId());

    private final boolean orderById;
    private final boolean orderConstructorById;
    private final List<SerializedField> fields;
    private final List<SerializedFieldType> fieldTypes;

    public SerializedFields(final boolean orderById, final boolean orderConstructorById) {
        this(orderById, orderConstructorById, ImmutableList.of(), ImmutableList.of());
    }

    public static Unverified<SerializedFields> build(final AutoSerializeUtils utils, final TypeElement element, final Set<ElementKind> kinds, final AutoSerializeMirror autoSerialize) {
        final List<Unverified<SerializedField>> unverifiedFields = new ArrayList<>();

        final SerializedFieldsBuilder fieldsBuilder = new SerializedFieldsBuilder(utils, element);

        for (final Unverified<FieldInformation> unverifiedField : getFields(utils, element, kinds,
                autoSerialize.isUseGetter())) {
            unverifiedFields.add(unverifiedField.transform(fieldsBuilder::buildField));
        }

        return Unverified.combine(unverifiedFields)
                .map((fields) -> new SerializedFields(autoSerialize.isOrderById(),
                        autoSerialize.isOrderConstructorById(), ImmutableList.copyOf(fields),
                        ImmutableList.copyOf(fieldsBuilder.getTypes())));
    }

    static Iterable<Unverified<FieldInformation>> getFields(final AutoSerializeUtils utils, final TypeElement element, final Set<ElementKind> kinds, final boolean defaultUseGetter) {
        final ImmutableList.Builder<Unverified<FieldInformation>> builder = ImmutableList.builder();

        for (final Element e : element.getEnclosedElements()) {
            if (!kinds.contains(e.getKind())) {
                continue;
            }

            // skip static fields.
            if (e.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }

            if (e.getAnnotation(AutoSerialize.Ignore.class) != null) {
                continue;
            }

            builder.add(FieldInformation.build(utils, element, e, defaultUseGetter));
        }

        return builder.build();
    }

    public Iterable<SerializedFieldType> getOrderedFieldTypes() {
        if (orderById) {
            return orderingTypesById.sortedCopy(fieldTypes);
        }

        return fieldTypes;
    }

    public Iterable<SerializedField> getOrderedFields() {
        if (orderById) {
            return orderingById.sortedCopy(fields);
        }

        return fields;
    }

    public Iterable<String> getConstructorVariables() {
        final Ordering<SerializedField> ordering;

        if (orderById) {
            ordering = orderingByCtorOrder.compound(orderingById);
        } else {
            ordering = orderingByCtorOrder;
        }

        return ImmutableList.copyOf(ordering.sortedCopy(fields).stream().map((f) -> f.getVariableName())
                .collect(Collectors.toList()));
    }
}