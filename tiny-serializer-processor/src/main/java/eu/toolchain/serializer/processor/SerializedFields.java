package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
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

    final static Ordering<SerializedField> orderingById = integerOrdering.onResultOf((SerializedField f) -> f.getId());

    final static Ordering<SerializedField> orderingByCtorOrder = integerOrdering
            .onResultOf((SerializedField f) -> f.getConstructorOrder());

    final static Ordering<SerializedFieldType> orderingTypesById = integerOrdering
            .onResultOf((SerializedFieldType f) -> f.getId());

    private final boolean orderById;
    private final boolean orderConstructorById;
    private final List<SerializedField> fields;
    private final List<SerializedFieldType> fieldTypes;

    public SerializedFields(final boolean orderById, final boolean orderConstructorById) {
        this(orderById, orderConstructorById, ImmutableList.of(), ImmutableList.of());
    }

    /**
     * Optionally verify the structure of this SerializedType.
     * @param messager
     *
     * @return This instance.
     */
    public List<SerializedTypeError> validate(final Element root) {
        final ImmutableList.Builder<SerializedTypeError> errors = ImmutableList.builder();

        for (final SerializedField field : fields) {
            if (!accessorMethodExists(root, field.getAccessor(), field.getType().getTypeName())) {
                errors.add();
                final String message = String.format("No matching accessor found, expected %s %s()",
                        field.getType().getTypeName(), field.getAccessor());
                errors.add(new SerializedTypeError(message, Optional.of(field.getElement())));
            }
        }

        return errors.build();
    }

    public static SerializedFields build(final AutoSerializeUtils utils, final TypeElement element, final Set<ElementKind> kinds) throws ElementException {
        final Optional<AutoSerializeMirror> a = utils.autoSerialize(element);

        if (!a.isPresent()) {
            throw new ElementException("@AutoSerialize annotaiton not present", element);
        }

        final AutoSerializeMirror autoSerialize = a.get();
        // final AutoSerialize autoSerialize = utils.requireAnnotation(element, AutoSerialize.class);

        final List<SerializedField> fields = new ArrayList<>();

        final List<SerializedFieldType> types = new ArrayList<>();

        final Naming fieldNaming = new Naming("s_");
        final Naming providerNaming = new Naming("p_");

        for (final FieldInformation f : getFields(utils, element, kinds, autoSerialize.isUseGetter())) {
            final TypeName fieldType = TypeName.get(f.getFieldType());
            final TypeName serializerType = TypeName.get(utils.serializerFor(f.getFieldType()));

            final SerializedFieldTypeIdentifier identifier = new SerializedFieldTypeIdentifier(fieldType, f.isProvided(),
                    f.getProviderName());

            final SerializedFieldType type;
            final Optional<SerializedFieldType> found;

            if ((found = types.stream().filter((t) -> t.getIdentifier().equals(identifier)).findFirst()).isPresent()) {
                type = found.get();
            } else {
                final String typeFieldName = fieldNaming.forType(fieldType, f.isProvided());

                final FieldSpec fieldSpec = FieldSpec
                        .builder(serializerType, typeFieldName)
                        .addModifiers(Modifier.FINAL).build();

                final Optional<ParameterSpec> providedParameterSpec;

                if (f.isProvided()) {
                    final String uniqueProviderName;

                    if (f.getProviderName().isPresent()) {
                        uniqueProviderName = providerNaming.forName(f.getProviderName().get());
                    } else {
                        uniqueProviderName = providerNaming.forType(fieldType, false);
                    }

                    providedParameterSpec = Optional.of(ParameterSpec.builder(serializerType, uniqueProviderName,
                            Modifier.FINAL).build());
                } else {
                    providedParameterSpec = Optional.empty();
                }

                type = new SerializedFieldType(identifier, f.getFieldType(), fieldType, fieldSpec, providedParameterSpec, f.getId());
                types.add(type);
            }

            fields.add(new SerializedField(f.getElement(), type, f.getFieldName(), f.getAccessor(), variableName(f.getFieldName()),
                    f.getId(), f.getConstructorOrder()));
        }

        return new SerializedFields(autoSerialize.isOrderById(), autoSerialize.isOrderConstructorById(),
                ImmutableList.copyOf(fields), ImmutableList.copyOf(types));
    }

    static Iterable<FieldInformation> getFields(final AutoSerializeUtils utils, final TypeElement element, final Set<ElementKind> kinds, final boolean defaultUseGetter) {
        final ImmutableList.Builder<FieldInformation> builder = ImmutableList.builder();

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

            builder.add(FieldInformation.build(utils, e, defaultUseGetter));
        }

        return builder.build();
    }

    static boolean accessorMethodExists(final Element root, final String accessor, final TypeName serializedType) {
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

    static String variableName(String fieldName) {
        return String.format("v_%s", fieldName);
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