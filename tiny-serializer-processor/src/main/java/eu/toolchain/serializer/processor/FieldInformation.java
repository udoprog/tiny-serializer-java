package eu.toolchain.serializer.processor;

import java.util.Optional;
import java.util.function.Supplier;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.TypeName;

import eu.toolchain.serializer.processor.annotation.FieldMirror;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;

@Data
public class FieldInformation {
    final Element element;
    final TypeMirror fieldType;
    final String fieldName;
    final boolean provided;
    final String accessor;
    final Optional<Integer> constructorOrder;
    final Optional<Integer> id;
    final Optional<String> providerName;

    public static Unverified<FieldInformation> build(AutoSerializeUtils utils, final Element parent, final Element element, boolean defaultUseGetter) {
        final TypeMirror fieldType;
        final boolean useGetter;

        final Optional<FieldMirror> field = utils.field(element);

        /**
         * If method, the desired type is the return type.
         */
        if (element instanceof ExecutableElement) {
            final ExecutableElement executable = (ExecutableElement)element;
            fieldType = executable.getReturnType();
            // methods are direct accessors, should never use getters.
            useGetter = false;
        } else {
            fieldType = element.asType();
            useGetter = field.map(FieldMirror::isUseGetter).orElse(defaultUseGetter);
        }

        final String fieldName = element.getSimpleName().toString();

        final boolean provided = field.map(FieldMirror::isProvided).orElse(false);
        final String accessor = field.map(FieldMirror::getAccessor).filter((a) -> !a.isEmpty())
                .orElseGet(getDefaultAccessor(fieldName, useGetter));
        final Optional<String> providerName = field.map(FieldMirror::getProviderName).filter((p) -> !p.isEmpty());
        final Optional<Integer> constructorOrder = field.map(FieldMirror::getConstructorOrder).filter((o) -> o >= 0);
        final Optional<Integer> id = field.map(FieldMirror::getId).filter((o) -> o >= 0);

        if (!accessorMethodExists(parent, accessor, TypeName.get(fieldType))) {
            final String message = String.format(String.format("Missing accessor %s %s()", fieldType, accessor));
            return field.map((f) -> Unverified.<FieldInformation> brokenAnnotation(message, element, f.getAnnotation()))
                    .orElseGet(() -> Unverified.brokenElement(message, element));
        }

        return Unverified.verified(new FieldInformation(element, fieldType, fieldName, provided, accessor,
                constructorOrder, id, providerName));
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

    static Supplier<String> getDefaultAccessor(final String fieldName, final boolean useGetter) {
        return () -> {
            if (!useGetter) {
                return fieldName;
            }

            return "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
        };
    }
}