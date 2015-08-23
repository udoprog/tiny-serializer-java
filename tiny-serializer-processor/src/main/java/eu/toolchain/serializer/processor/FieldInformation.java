package eu.toolchain.serializer.processor;

import java.util.Optional;
import java.util.function.Supplier;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.CaseFormat;

import eu.toolchain.serializer.processor.annotation.FieldMirror;
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

    public static FieldInformation build(AutoSerializeUtils utils, Element element, boolean defaultUseGetter) {
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

        return new FieldInformation(element, fieldType, fieldName, provided, accessor, constructorOrder, id,
                providerName);
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