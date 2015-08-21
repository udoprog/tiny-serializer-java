package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import eu.toolchain.serializer.AutoSerialize;

@Data
@RequiredArgsConstructor
class SerializedTypeFields {
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

    public SerializedTypeFields(final boolean orderById, final boolean orderConstructorById) {
        this(orderById, orderConstructorById, ImmutableList.of(), ImmutableList.of());
    }

    /**
     * Optionally verify the structure of this SerializedType.
     * @param messager
     *
     * @return This instance.
     */
    boolean isValid(final Element root, final Messager messager) {
        boolean valid = true;

        for (final SerializedField field : fields) {
            if (!accessorMethodExists(root, field.getAccessor(), field.getFieldType().getFieldType())) {
                messager.printMessage(Diagnostic.Kind.WARNING, String.format("No accessor found for field: %s", field));
                valid = false;
            }
        }

        return valid;
    }

    public static SerializedTypeFields build(final AutoSerializeUtils utils, final Element root,
            final AutoSerialize autoSerialize) {
        final List<SerializedField> fields = new ArrayList<>();

        final List<SerializedFieldType> types = new ArrayList<>();

        final Naming fieldNaming = new Naming("s_");
        final Naming providerNaming = new Naming("p_");

        for (final Element e : root.getEnclosedElements()) {
            if (e.getKind() != ElementKind.FIELD) {
                continue;
            }

            // skip static fields.
            if (e.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }

            if (e.getAnnotation(AutoSerialize.Ignore.class) != null) {
                continue;
            }

            final TypeName fieldType = TypeName.get(e.asType());
            final TypeName serializerType = TypeName.get(utils.serializerFor(e.asType()));

            final boolean provided = isParameterProvided(e);

            final Optional<String> providerName = getProviderName(e);
            final SerializedFieldTypeIdentifier identifier = new SerializedFieldTypeIdentifier(fieldType, provided,
                    providerName);
            final Optional<Integer> id = getId(e);

            final SerializedFieldType type;
            final Optional<SerializedFieldType> found;

            if ((found = types.stream().filter((t) -> t.getIdentifier().equals(identifier)).findFirst()).isPresent()) {
                type = found.get();
            } else {
                final String typeFieldName = fieldNaming.forType(fieldType, provided);

                final FieldSpec fieldSpec = FieldSpec
                        .builder(serializerType, typeFieldName)
                        .addModifiers(Modifier.FINAL).build();

                final Optional<ParameterSpec> providedParameterSpec;

                if (provided) {
                    final String uniqueProviderName;

                    if (providerName.isPresent()) {
                        uniqueProviderName = providerNaming.forName(providerName.get());
                    } else {
                        uniqueProviderName = providerNaming.forType(fieldType, false);
                    }

                    providedParameterSpec = Optional.of(ParameterSpec.builder(serializerType, uniqueProviderName,
                            Modifier.FINAL).build());
                } else {
                    providedParameterSpec = Optional.empty();
                }

                type = new SerializedFieldType(identifier, e, fieldType, fieldSpec, providedParameterSpec, id);
                types.add(type);
            }

            final String fieldName = e.getSimpleName().toString();
            final boolean useGetter = isParameterUsingGetter(e, autoSerialize);
            final String accessor = accessorForField(e, useGetter);

            fields.add(new SerializedField(type, fieldName, accessor, variableName(fieldName), id,
                    getConstructorOrder(e)));
        }

        return new SerializedTypeFields(autoSerialize.orderById(), autoSerialize.orderConstructorById(),
                ImmutableList.copyOf(fields), ImmutableList.copyOf(types));
    }

    static Optional<Integer> getId(Element e) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null) {
            if (field.id() >= 0) {
                return Optional.of(field.id());
            }
        }

        return Optional.empty();
    }

    static Optional<Integer> getConstructorOrder(Element e) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null) {
            if (field.constructorOrder() >= 0) {
                return Optional.of(field.constructorOrder());
            }
        }

        return Optional.empty();
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

    static boolean isParameterProvided(Element e) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null) {
            return field.provided();
        }

        return false;
    }

    static Optional<String> getProviderName(Element e) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null && !"".equals(field.providerName())) {
            return Optional.of(field.providerName());
        }

        return Optional.empty();
    }

    static boolean isParameterUsingGetter(Element e, AutoSerialize autoSerialize) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null) {
            return field.useGetter();
        }

        return autoSerialize.useGetter();
    }

    static String variableName(String fieldName) {
        return String.format("v_%s", fieldName);
    }

    static String accessorForField(Element e, final boolean useGetter) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null && !"".equals(field.accessor())) {
            return field.accessor();
        }

        final String accessor = e.getSimpleName().toString();

        if (!useGetter) {
            return accessor;
        }

        return "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, accessor);
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