package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import eu.toolchain.serializer.AutoSerialize;

@Data
@RequiredArgsConstructor
class SerializedTypeFields {
    private final List<SerializedField> fields;
    private final List<SerializedFieldType> fieldTypes;

    public SerializedTypeFields() {
        this(ImmutableList.of(), ImmutableList.of());
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

    public static SerializedTypeFields build(final Element root, final AutoSerialize autoSerialize) {
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

            final boolean provided = isParameterProvided(e);

            final Optional<String> providerName = getProviderName(e);
            final SerializedFieldTypeIdentifier identifier = new SerializedFieldTypeIdentifier(fieldType, provided,
                    providerName);

            final SerializedFieldType type;
            final Optional<SerializedFieldType> found;

            if ((found = types.stream().filter((t) -> t.getIdentifier().equals(identifier)).findFirst()).isPresent()) {
                type = found.get();
            } else {
                final String typeFieldName = fieldNaming.forType(fieldType, provided);

                final FieldSpec fieldSpec = FieldSpec
                        .builder(AutoSerializerProcessor.serializerFor(fieldType), typeFieldName)
                        .addModifiers(Modifier.FINAL).build();

                final Optional<ParameterSpec> providedParameterSpec;

                if (provided) {
                    final String uniqueProviderName;

                    if (providerName.isPresent()) {
                        uniqueProviderName = providerNaming.forName(providerName.get());
                    } else {
                        uniqueProviderName = providerNaming.forType(fieldType, false);
                    }

                    providedParameterSpec = Optional.of(ParameterSpec.builder(
                            AutoSerializerProcessor.serializerFor(fieldType), uniqueProviderName,
                            Modifier.FINAL).build());
                } else {
                    providedParameterSpec = Optional.empty();
                }

                type = new SerializedFieldType(identifier, fieldType, fieldSpec, providedParameterSpec);
                types.add(type);
            }

            final String fieldName = e.getSimpleName().toString();
            final boolean useGetter = isParameterUsingGetter(e, autoSerialize);
            final String accessor = accessorForField(e, useGetter);

            fields.add(new SerializedField(type, fieldName, accessor));
        }

        return new SerializedTypeFields(ImmutableList.copyOf(fields), ImmutableList.copyOf(types));
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
}