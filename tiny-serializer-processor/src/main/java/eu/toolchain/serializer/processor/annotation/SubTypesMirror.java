package eu.toolchain.serializer.processor.annotation;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import com.google.common.collect.ImmutableList;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.processor.AnnotationValues;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import lombok.Data;

@Data
public class SubTypesMirror {
    private final AnnotationMirror annotation;
    private final List<SubTypeMirror> subTypes;

    public static Optional<SubTypesMirror> getFor(final AutoSerializeUtils utils, final TypeElement element) {
        for (final AnnotationMirror a : utils.getAnnotations(element, AutoSerialize.Builder.class)) {
            return Optional.of(getFor(utils, a));
        }

        return Optional.empty();
    }

    public static SubTypesMirror getFor(final AutoSerializeUtils utils, final AnnotationMirror a) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(a);

        return new SubTypesMirror(a, ImmutableList.of());
    }
}