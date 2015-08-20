package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import eu.toolchain.serializer.AutoSerialize;

@Data
@RequiredArgsConstructor
class SerializedType {
    private final Element root;
    private final TypeName type;
    private final List<SerializedField> fields;
    private final List<SerializedFieldType> fieldTypes;

    public SerializedType(Element root, TypeName type) {
        this(root, type, ImmutableList.of(), ImmutableList.of());
    }

    /**
     * Optionally verify the structure of this SerializedType.
     *
     * @return This instance.
     */
    SerializedType verify() {
        for (final SerializedField field : fields) {
            if (!accessorMethodExists(root, field.getAccessor(), field.getFieldType().getFieldType())) {
                throw new IllegalStateException(String.format("Accessor #%s() does not exist on type %s",
                        field.getAccessor(),
                        root.asType()));
            }
        }

        return this;
    }

    public static SerializedType build(final Element root, final AutoSerialize autoSerialize) {
        final TypeName elementType = TypeName.get(root.asType());

        final List<SerializedField> fields = new ArrayList<>();

        final Set<String> seenNames = new HashSet<>();
        final LinkedHashMap<SerializedFieldTypeKey, SerializedFieldType> types = new LinkedHashMap<>();

        int providedIndex = 0;

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

            final boolean provided = isParameterProvided(e);

            final SerializedFieldTypeKey key = new SerializedFieldTypeKey(fieldType, provided);

            final SerializedFieldType type;
            final SerializedFieldType found;

            if ((found = types.get(key)) != null) {
                type = found;
            } else {
                final String typeFieldName = uniqueName(seenNames, fieldType, provided);

                final FieldSpec fieldSpec = FieldSpec
                        .builder(AutoSerializerProcessor.serializerFor(fieldType), typeFieldName)
                        .addModifiers(Modifier.FINAL).build();

                final Optional<ParameterSpec> providedParameterSpec;

                if (provided) {
                    providedParameterSpec = Optional.of(ParameterSpec.builder(
                            AutoSerializerProcessor.serializerFor(fieldType), String.format("p%d", providedIndex++),
                            Modifier.FINAL).build());
                } else {
                    providedParameterSpec = Optional.empty();
                }

                type = new SerializedFieldType(fieldType, fieldSpec, providedParameterSpec);
                types.put(key, type);
            }

            final String fieldName = e.getSimpleName().toString();
            final boolean useGetter = isParameterUsingGetter(e, autoSerialize);
            final String accessor = accessorForField(e, useGetter);

            fields.add(new SerializedField(type, fieldName, accessor));
        }

        return new SerializedType(root, elementType, ImmutableList.copyOf(fields), ImmutableList.copyOf(types.values()));
    }

    static String uniqueName(Set<String> seenNames, TypeName fieldType, boolean provided) {
        final String base = String.format(provided ? "s_Provided%s" : "s_%s", composeName(fieldType));

        int index = 1;

        String candidate = base;

        while (seenNames.contains(candidate)) {
            candidate = String.format("%s%d", base, index++);
        }

        seenNames.add(candidate);
        return candidate;
    }

    final static ClassName optionalType = ClassName.get(Optional.class);

    static String composeName(TypeName fieldType) {
        if (fieldType instanceof ClassName) {
            final ClassName c = (ClassName) fieldType;
            return c.simpleName();
        }

        if (fieldType instanceof ParameterizedTypeName) {
            final ParameterizedTypeName p = (ParameterizedTypeName) fieldType;

            if (p.rawType.equals(optionalType)) {
                return "Optional" + composeName(p.typeArguments.iterator().next());
            }

            return p.rawType.simpleName();
        }

        if (fieldType instanceof ArrayTypeName) {
            final ArrayTypeName a = (ArrayTypeName) fieldType;
            return composeName(a.componentType) + "Array";
        }

        if (fieldType.isPrimitive()) {
            return composeName(AutoSerializerProcessor.primitiveType(fieldType));
        }

        throw new IllegalArgumentException("Cannot get raw type for " + fieldType.toString());
    }

    static boolean isOptional(TypeName fieldType) {
        if (fieldType instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) fieldType).rawType.equals(optionalType);
        }

        return false;
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

    static boolean isParameterUsingGetter(Element e, AutoSerialize autoSerialize) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null) {
            return field.useGetter();
        }

        return autoSerialize.useGetter();
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

    @Data
    static class SerializedFieldTypeKey {
        private final TypeName fieldType;
        private final boolean provided;
    }
}