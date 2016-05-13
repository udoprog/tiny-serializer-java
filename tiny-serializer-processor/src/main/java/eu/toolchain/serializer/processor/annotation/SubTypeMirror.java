package eu.toolchain.serializer.processor.annotation;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

@Data
public class SubTypeMirror {
    private final AnnotationMirror annotation;

    private final AnnotationValues.Value<TypeMirror> value;
    private final Optional<Short> id;

    public static Unverified<SubTypeMirror> getFor(
        final AutoSerializeUtils utils, final Element element, final AnnotationMirror a
    ) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(element, a);

        final Unverified<AnnotationValues.Value<TypeMirror>> unverifiedValue =
            values.getTypeMirror("value");
        final Optional<Short> id = Optional.of(values.getShort("id").get()).filter(i -> i >= 0);

        return unverifiedValue.map((value) -> {
            return new SubTypeMirror(a, value, id);
        });
    }
}
