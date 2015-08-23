package eu.toolchain.serializer.processor.annotation;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.google.common.collect.ImmutableList;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;

@Data
public class SubTypesMirror {
    private final AnnotationMirror annotation;
    private final List<SubTypeMirror> subTypes;

    public static Unverified<SubTypesMirror> getFor(final AutoSerializeUtils utils, final Element element, final AnnotationMirror a) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(element, a);

        final ImmutableList.Builder<Unverified<SubTypeMirror>> unverifiedSubTypes = ImmutableList.builder();

        for (final AnnotationMirror subType : values.getAnnotationValue("value").get()) {
            unverifiedSubTypes.add(SubTypeMirror.getFor(utils, element, subType));
        }

        return Unverified.combine(unverifiedSubTypes.build()).map((subTypes) -> {
            return new SubTypesMirror(a, subTypes);
        });
    }
}