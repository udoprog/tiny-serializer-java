package eu.toolchain.serializer.processor.annotation;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import lombok.Data;

@Data
public class SubTypeMirror {
    private final AnnotationMirror annotation;
    private final AnnotationValues.Value<TypeMirror> value;
    private final Optional<Short> id;

    public static SubTypeMirror getFor(
        final AutoSerializeUtils utils, final Element element, final AnnotationMirror a
    ) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(element, a);

        final AnnotationValues.Value<TypeMirror> value = values.getTypeMirror("value");
        final Optional<Short> id = Optional.of(values.getShort("id").get()).filter(i -> i >= 0);

        return new SubTypeMirror(a, value, id);
    }
}
