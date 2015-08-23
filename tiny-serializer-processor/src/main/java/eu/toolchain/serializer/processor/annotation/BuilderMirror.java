package eu.toolchain.serializer.processor.annotation;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;

import eu.toolchain.serializer.DefaultBuilderType;
import eu.toolchain.serializer.processor.AnnotationValues;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import lombok.Data;

@Data
public class BuilderMirror {
    private static final String defaultBuilderType = DefaultBuilderType.class.getCanonicalName();
    private final AnnotationMirror annotation;

    private final boolean useSetter;
    private final boolean useMethod;
    private final boolean useConstructor;
    private final Optional<TypeMirror> type;
    private final String methodName;

    public static BuilderMirror getFor(final AutoSerializeUtils utils, final AnnotationMirror a) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(a);

        final boolean useSetter = values.getBoolean("useSetter");
        final boolean useMethod = values.getBoolean("useMethod");
        final boolean useConstructor = values.getBoolean("useConstructor");
        final Optional<TypeMirror> type = Optional.of(values.getTypeMirror("type")).filter((t) -> !t.toString().equals(defaultBuilderType));
        final String methodName = values.getString("methodName");

        return new BuilderMirror(a, useSetter, useMethod, useConstructor, type, methodName);
    }

    public boolean shouldUseConstructor() {
        // use explicitly ask to use method.
        if (useMethod) {
            return false;
        }

        // use explicitly ask to use constructor.
        if (useConstructor) {
            return true;
        }

        // by policy, if a type is specified, the constructor should be used.
        if (type.isPresent()) {
            return true;
        }

        return false;
    }

    public boolean isErrorType() {
        return type.map((t) -> t instanceof ErrorType).orElse(false);
    }
}