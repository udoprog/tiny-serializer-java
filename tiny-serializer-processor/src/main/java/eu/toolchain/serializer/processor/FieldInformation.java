package eu.toolchain.serializer.processor;

import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.CaseFormat;

import eu.toolchain.serializer.AutoSerialize;
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

    public static FieldInformation build(Element e, boolean defaultUseGetter) {
        final TypeMirror fieldType;
        final boolean useGetter;

        /**
         * If method, the desired type is the return type.
         */
        if (e instanceof ExecutableElement) {
            final ExecutableElement executable = (ExecutableElement)e;
            fieldType = executable.getReturnType();
            // methods are direct accessors, should never use getters.
            useGetter = false;
        } else {
            fieldType = e.asType();
            useGetter = isFieldUsingGetter(e, defaultUseGetter);
        }

        final boolean provided = isProvided(e);
        final String accessor = getAccessor(e, useGetter);
        final Optional<Integer> constructorOrder = getConstructorOrder(e);
        final Optional<Integer> id = getId(e);
        final Optional<String> providerName = getProviderName(e);

        return new FieldInformation(e, fieldType, e.getSimpleName().toString(), provided, accessor, constructorOrder, id, providerName);
    }

    static boolean isProvided(Element e) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null) {
            return field.provided();
        }

        return false;
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

    static boolean isFieldUsingGetter(Element e, boolean defaultUseGetter) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null) {
            return field.useGetter();
        }

        return defaultUseGetter;
    }

    static Optional<String> getProviderName(Element e) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null && !"".equals(field.providerName())) {
            return Optional.of(field.providerName());
        }

        return Optional.empty();
    }

    static String getAccessor(Element e, final boolean useGetter) {
        final AutoSerialize.Field field;

        // explicit name through annotation.
        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null && !"".equals(field.accessor())) {
            return field.accessor();
        }

        final String accessor = e.getSimpleName().toString();

        if (!useGetter) {
            return accessor;
        }

        return "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, accessor);
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
}