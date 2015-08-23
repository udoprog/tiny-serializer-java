package eu.toolchain.serializer.processor.annotation;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;

@Data
public class SubTypeMirror {
    private final AnnotationMirror annotation;

    private final AnnotationValues.Value<TypeMirror> value;
    private final Optional<Integer> id;

    public static Unverified<SubTypeMirror> getFor(final AutoSerializeUtils utils, final Element element, final AnnotationMirror a) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(element, a);

        final Unverified<AnnotationValues.Value<TypeMirror>> unverifiedValue = values.getTypeMirror("value");
        final Optional<Integer> id = Optional.of(values.getInteger("id").get()).filter(i -> i >= 0);

        return unverifiedValue.map((value) -> {
            return new SubTypeMirror(a, value, id);
        });
    }
}