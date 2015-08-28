package eu.toolchain.serializer.processor.annotation;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;

@Data
public class BuilderMirror {
    private final AnnotationMirror annotation;

    private final boolean useSetter;
    private final boolean useMethod;
    private final boolean useConstructor;
    private final Optional<AnnotationValues.Value<TypeMirror>> type;
    private final String methodName;

    public static Unverified<BuilderMirror> getFor(final AutoSerializeUtils utils, final Element element, final AnnotationMirror a) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(element, a);

        final boolean useSetter = values.getBoolean("useSetter").get();
        final boolean useMethod = values.getBoolean("useMethod").get();
        final boolean useConstructor = values.getBoolean("useConstructor").get();
        final String methodName = values.getString("methodName").get();

        final Unverified<AnnotationValues.Value<TypeMirror>> unverifiedType = values.getTypeMirror("type");

        return unverifiedType.map((type) -> {
            final Optional<AnnotationValues.Value<TypeMirror>> typeMirror = Optional.of(type).filter((t) -> !t.get().toString().equals(AutoSerializeUtils.DEFAULT_BUILDER_TYPE));
            return new BuilderMirror(a, useSetter, useMethod, useConstructor, typeMirror, methodName);
        });
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
}